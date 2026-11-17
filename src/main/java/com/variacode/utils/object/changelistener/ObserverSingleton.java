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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ObserverSingleton {

    INSTANCE;

    private final List<ChangeListener> globalListeners = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, List<ChangeListener>> streamListeners = new ConcurrentHashMap<>();

    public void change(Change<?> change) {
        Objects.requireNonNull(change, "change must not be null");
        List<ChangeListener> scoped = streamListeners.get(change.changeStream());
        if (scoped != null) {
            for (ChangeListener listener : scoped) {
                listener.change(change);
            }
        }
        for (ChangeListener listener : globalListeners) {
            listener.change(change);
        }
    }

    /**
     * Subscribes a listener to all streams and returns an {@link AutoCloseable} handle to unsubscribe.
     */
    public AutoCloseable subscribe(ChangeListener listener) {
        addChangeListener(listener);
        return () -> removeChangeListener(listener);
    }

    /**
     * Subscribes a listener to a specific stream and returns an {@link AutoCloseable} handle to unsubscribe.
     */
    public AutoCloseable subscribe(String stream, ChangeListener listener) {
        addChangeListener(stream, listener);
        return () -> removeChangeListener(stream, listener);
    }

    public void addChangeListener(ChangeListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        this.globalListeners.add(listener);
    }

    public void addChangeListener(String stream, ChangeListener listener) {
        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        this.streamListeners.computeIfAbsent(stream, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        this.globalListeners.remove(listener);
    }

    public void removeChangeListener(String stream, ChangeListener listener) {
        if (stream == null || listener == null) {
            return;
        }
        List<ChangeListener> scoped = this.streamListeners.get(stream);
        if (scoped != null) {
            scoped.remove(listener);
        }
    }

    public void removeAllChangeListeners() {
        this.globalListeners.clear();
        this.streamListeners.clear();
    }
}
