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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Observe {
    String DEFAULT_STREAM = "class";
    String INIT_FIELD_NAME = "<init>";

    String stream() default DEFAULT_STREAM;

    /**
     * When {@code true} on a class-level {@code @Observe} (or when {@code @Observe} is placed
     * directly on a constructor), emits a {@link Change} event with {@code fieldName = "<init>"}
     * upon object construction.
     */
    boolean observeConstruction() default false;
}
