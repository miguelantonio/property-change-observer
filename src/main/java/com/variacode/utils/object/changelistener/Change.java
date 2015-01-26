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

public class Change {
    
    private final String changeStream;
    private final String fieldName;
    private final Object fieldValue;
    private final Object object;

    Change(String changeStream, String fieldName, Object fieldValue, Object object) {
        this.changeStream = changeStream;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.object = object;
    }

    public String getChangeStream() {
        return changeStream;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "Change{" + "changeStream=" + changeStream + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", object=" + object + '}';
    }
    
}
