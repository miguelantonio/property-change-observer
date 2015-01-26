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

import java.beans.Introspector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ObservableObjectAspect {

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
        //public Method
    }

    @Around("execution(* *.set*(..)) && @within(annotation)")
    public Object setMethod(ProceedingJoinPoint joinPoint, Observe annotation) throws Throwable {
        ObserverSingleton.INSTANCE.change(new Change(annotation.stream(),
                Introspector.decapitalize(
                        joinPoint.toShortString().replaceFirst("execution\\(" + joinPoint.getThis().getClass().getSimpleName() + ".set", "")
                        .replaceAll("\\.", "").replaceAll("\\)", "").replaceAll("\\(", "")
                ), joinPoint.getArgs()[0], joinPoint.getThis()));
        return joinPoint.proceed();
    }
}
