/*
 * Copyright 2015 Variacode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.variacode.utils.object.changelistener;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Compile-time annotation processor for {@link Observe}.
 * <p>
 * For any extensible class annotated with {@code @Observe} (or containing methods annotated
 * with {@code @Observe}), generates an {@code Observable<ClassName>} subclass in the same
 * package that intercepts setter mutations and publishes {@link Change} events through
 * {@link ObserverSingleton#INSTANCE}.
 */
@SupportedAnnotationTypes("com.variacode.utils.object.changelistener.Observe")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ObserveProcessor extends AbstractProcessor {

    public static final String GENERATED_PREFIX = "Observable";
    private final Set<String> processedClasses = new LinkedHashSet<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        Set<TypeElement> targetClasses = new LinkedHashSet<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Observe.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                if (typeElement.getAnnotation( javax.annotation.processing.Generated.class ) == null) {
                    targetClasses.add(typeElement);
                }
            } else if (element.getKind() == ElementKind.METHOD) {
                Element enclosing = element.getEnclosingElement();
                if (enclosing instanceof TypeElement typeElement
                        && enclosing.getKind() == ElementKind.CLASS
                        && typeElement.getAnnotation(javax.annotation.processing.Generated.class) == null) {
                    targetClasses.add(typeElement);
                }
            }
        }

        for (TypeElement classElement : targetClasses) {
            String qualifiedName = classElement.getQualifiedName().toString();
            if (processedClasses.add(qualifiedName) && validateClass(classElement)) {
                try {
                    generateObservableSubclass(classElement);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Failed to generate observable subclass for " + qualifiedName + ": " + e.getMessage(),
                            classElement
                    );
                }
            }
        }
        return true;
    }

    private boolean validateClass(TypeElement classElement) {
        if (classElement.getModifiers().contains(Modifier.FINAL)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "@Observe cannot be applied to final class " + classElement.getQualifiedName(),
                    classElement
            );
            return false;
        }
        if (classElement.getModifiers().contains(Modifier.PRIVATE)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "@Observe cannot be applied to private class " + classElement.getQualifiedName(),
                    classElement
            );
            return false;
        }
        return true;
    }

    private void generateObservableSubclass(TypeElement classElement) throws IOException {
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(classElement);
        String packageName = packageElement.isUnnamed() ? "" : packageElement.getQualifiedName().toString();
        String originalSimpleName = classElement.getSimpleName().toString();
        String generatedSimpleName = GENERATED_PREFIX + originalSimpleName;
        String qualifiedGeneratedName = packageName.isEmpty()
                ? generatedSimpleName
                : packageName + "." + generatedSimpleName;

        Observe classObserve = classElement.getAnnotation(Observe.class);

        Map<String, ExecutableElement> gettersByProperty = findAccessibleGetters(classElement, packageElement);
        List<SetterInfo> settersToOverride = findSettersToOverride(classElement, packageElement, classObserve, gettersByProperty);
        List<ExecutableElement> constructors = findAccessibleConstructors(classElement);

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGeneratedName, classElement);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            if (!packageName.isEmpty()) {
                out.println("package " + packageName + ";");
                out.println();
            }

            out.println("import com.variacode.utils.object.changelistener.Change;");
            out.println("import com.variacode.utils.object.changelistener.ObserverSingleton;");
            out.println("import java.util.Objects;");
            out.println("import javax.annotation.processing.Generated;");
            out.println();
            out.println("@Generated(\"" + ObserveProcessor.class.getName() + "\")");
            out.println("public class " + generatedSimpleName + " extends " + classElement.getQualifiedName() + " {");
            out.println();

            for (ExecutableElement constructor : constructors) {
                writeConstructor(out, generatedSimpleName, constructor);
            }

            for (SetterInfo setter : settersToOverride) {
                writeSetterOverride(out, setter);
            }

            out.println("}");
        }
    }

    private List<ExecutableElement> findAccessibleConstructors(TypeElement classElement) {
        List<ExecutableElement> result = new ArrayList<>();
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) enclosed;
                if (!constructor.getModifiers().contains(Modifier.PRIVATE)) {
                    result.add(constructor);
                }
            }
        }
        return result;
    }

    private Map<String, ExecutableElement> findAccessibleGetters(TypeElement classElement, PackageElement targetPackage) {
        Map<String, ExecutableElement> getters = new HashMap<>();
        for (Element member : processingEnv.getElementUtils().getAllMembers(classElement)) {
            if (member.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) member;
            if (!isAccessibleFromSubclass(method, targetPackage)
                    || method.getModifiers().contains(Modifier.STATIC)
                    || !method.getParameters().isEmpty()
                    || method.getReturnType().getKind() == TypeKind.VOID) {
                continue;
            }
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith("get") && methodName.length() > 3 && !methodName.equals("getClass")) {
                String prop = decapitalize(methodName.substring(3));
                getters.putIfAbsent(prop, method);
            } else if (methodName.startsWith("is") && methodName.length() > 2
                    && (method.getReturnType().getKind() == TypeKind.BOOLEAN
                    || "java.lang.Boolean".equals(method.getReturnType().toString()))) {
                String prop = decapitalize(methodName.substring(2));
                getters.putIfAbsent(prop, method);
            }
        }
        return getters;
    }

    private List<SetterInfo> findSettersToOverride(
            TypeElement classElement,
            PackageElement targetPackage,
            Observe classObserve,
            Map<String, ExecutableElement> gettersByProperty
    ) {
        Map<String, SetterInfo> setters = new LinkedHashMap<>();
        for (Element member : processingEnv.getElementUtils().getAllMembers(classElement)) {
            if (member.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) member;
            String methodName = method.getSimpleName().toString();
            if (!methodName.startsWith("set")
                    || methodName.length() <= 3
                    || method.getParameters().size() != 1
                    || method.getModifiers().contains(Modifier.STATIC)
                    || method.getModifiers().contains(Modifier.FINAL)
                    || method.getModifiers().contains(Modifier.ABSTRACT)
                    || !isAccessibleFromSubclass(method, targetPackage)) {
                continue;
            }

            Observe methodObserve = method.getAnnotation(Observe.class);
            if (classObserve == null && methodObserve == null) {
                continue;
            }

            String stream = methodObserve != null ? methodObserve.stream() : classObserve.stream();
            String propertyName = decapitalize(methodName.substring(3));
            ExecutableElement getter = gettersByProperty.get(propertyName);
            String signatureKey = methodName + "(" + method.getParameters().get(0).asType().toString() + ")";
            setters.put(signatureKey, new SetterInfo(method, propertyName, stream, getter));
        }
        return new ArrayList<>(setters.values());
    }

    private boolean isAccessibleFromSubclass(Element member, PackageElement targetPackage) {
        Set<Modifier> modifiers = member.getModifiers();
        if (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED)) {
            return true;
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            return false;
        }
        PackageElement memberPackage = processingEnv.getElementUtils().getPackageOf(member);
        return memberPackage.equals(targetPackage);
    }

    private void writeConstructor(PrintWriter out, String generatedSimpleName, ExecutableElement constructor) {
        String visibility = getVisibilityPrefix(constructor.getModifiers());
        String params = formatParameters(constructor.getParameters());
        String args = constructor.getParameters().stream()
                .map(p -> p.getSimpleName().toString())
                .collect(Collectors.joining(", "));
        String throwsClause = formatThrows(constructor.getThrownTypes());

        out.println("    " + visibility + generatedSimpleName + "(" + params + ")" + throwsClause + " {");
        out.println("        super(" + args + ");");
        out.println("    }");
        out.println();
    }

    private void writeSetterOverride(PrintWriter out, SetterInfo setterInfo) {
        ExecutableElement method = setterInfo.method();
        String visibility = getVisibilityPrefix(method.getModifiers());
        String returnType = method.getReturnType().toString();
        boolean isVoid = method.getReturnType().getKind() == TypeKind.VOID;
        String methodName = method.getSimpleName().toString();
        VariableElement param = method.getParameters().get(0);
        String paramType = param.asType().toString();
        String paramName = param.getSimpleName().toString();
        String throwsClause = formatThrows(method.getThrownTypes());

        out.println("    @Override");
        out.println("    " + visibility + returnType + " " + methodName + "(" + paramType + " " + paramName + ")" + throwsClause + " {");

        if (setterInfo.getter() != null) {
            String getterCall = "this." + setterInfo.getter().getSimpleName() + "()";
            out.println("        Object oldValue = " + getterCall + ";");
            if (isVoid) {
                out.println("        super." + methodName + "(" + paramName + ");");
            } else {
                out.println("        " + returnType + " result = super." + methodName + "(" + paramName + ");");
            }
            out.println("        if (!Objects.equals(oldValue, " + paramName + ")) {");
            out.println("            ObserverSingleton.INSTANCE.change(new Change<>(\""
                    + escapeJavaString(setterInfo.stream()) + "\", \""
                    + escapeJavaString(setterInfo.propertyName()) + "\", oldValue, "
                    + paramName + ", this));");
            out.println("        }");
        } else {
            if (isVoid) {
                out.println("        super." + methodName + "(" + paramName + ");");
            } else {
                out.println("        " + returnType + " result = super." + methodName + "(" + paramName + ");");
            }
            out.println("        ObserverSingleton.INSTANCE.change(new Change<>(\""
                    + escapeJavaString(setterInfo.stream()) + "\", \""
                    + escapeJavaString(setterInfo.propertyName()) + "\", null, "
                    + paramName + ", this));");
        }

        if (!isVoid) {
            out.println("        return result;");
        }
        out.println("    }");
        out.println();
    }

    private static String getVisibilityPrefix(Set<Modifier> modifiers) {
        if (modifiers.contains(Modifier.PUBLIC)) {
            return "public ";
        }
        if (modifiers.contains(Modifier.PROTECTED)) {
            return "protected ";
        }
        return "";
    }

    private static String formatParameters(List<? extends VariableElement> params) {
        return params.stream()
                .map(p -> p.asType().toString() + " " + p.getSimpleName().toString())
                .collect(Collectors.joining(", "));
    }

    private static String formatThrows(List<? extends TypeMirror> thrownTypes) {
        if (thrownTypes.isEmpty()) {
            return "";
        }
        return " throws " + thrownTypes.stream()
                .map(TypeMirror::toString)
                .collect(Collectors.joining(", "));
    }

    static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private static String escapeJavaString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record SetterInfo(
            ExecutableElement method,
            String propertyName,
            String stream,
            ExecutableElement getter
    ) {}
}
