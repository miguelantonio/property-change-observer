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

/**
 * Immutable event record representing a property mutation on an observed object.
 *
 * @param <T>          the type of the observed object
 * @param changeStream the stream identifier configured in {@link Observe#stream()}
 * @param fieldName    the decapitalized property name
 * @param oldValue     the previous value of the property (or {@code null} if unavailable)
 * @param fieldValue   the new value assigned to the property
 * @param object       the observed instance that was mutated
 */
public record Change<T>(
        String changeStream,
        String fieldName,
        Object oldValue,
        Object fieldValue,
        T object
) {

    public Change(String changeStream, String fieldName, Object fieldValue, T object) {
        this(changeStream, fieldName, null, fieldValue, object);
    }

    public String getChangeStream() {
        return changeStream;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public T getObject() {
        return object;
    }
}
