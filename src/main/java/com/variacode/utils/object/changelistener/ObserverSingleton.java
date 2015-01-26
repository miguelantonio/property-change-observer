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

import java.util.ArrayList;
import java.util.List;

public enum ObserverSingleton {

    INSTANCE;

    private final List<ChangeListener> listeners = new ArrayList<>();

    protected void change(Change change) {
        listeners.stream().forEach((c) -> {
            c.change(change);
        });
    }

    public void addChangeListener(ChangeListener listener) {
        this.listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        this.listeners.remove(listener);
    }

    public void removeAllChangeListeners() {
        this.listeners.removeAll(this.listeners);
    }
}
