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

import static com.variacode.utils.object.changelistener.DogTest.STREAM;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class ObservableObjectAspectTest {

    @After
    public void tearDown() {
        ObserverSingleton.INSTANCE.removeAllChangeListeners();
    }

    @Test
    public void testDaysOldStream() {
        final int trufitaDaysOld = 64;
        int[] result = new int[trufitaDaysOld];
        int[] expResult = new int[trufitaDaysOld];
        for (int i = 0; i < expResult.length; i++) {
            expResult[i] = i + 1;
        }

        DogTest trufa = new ObservableDogTest("Trufa", "Boston Terrier", 0);

        ObserverSingleton.INSTANCE.addChangeListener(STREAM, new ChangeListener() {
            int i = 0;

            @Override
            public void change(Change<?> change) {
                result[i] = (Integer) change.getFieldValue();
                i++;
            }
        });

        for (int input : expResult) {
            trufa.setDaysOld(input);
        }

        assertArrayEquals(expResult, result);
    }

    @Test
    public void testConstructionAndOldNewValueAndDeduplication() throws Exception {
        List<Change<?>> events = new ArrayList<>();
        try (AutoCloseable sub = ObserverSingleton.INSTANCE.subscribe(STREAM, events::add)) {
            DogTest trufa = new ObservableDogTest("Trufa", "Boston Terrier", 10);

            // 1. Object construction emits an <init> event
            assertEquals(1, events.size());
            Change<?> initEvent = events.get(0);
            assertEquals(STREAM, initEvent.changeStream());
            assertEquals(Observe.INIT_FIELD_NAME, initEvent.fieldName());
            assertSame(trufa, initEvent.fieldValue());
            assertSame(trufa, initEvent.object());

            // 2. Setting the exact same value should not emit a change event
            trufa.setName("Trufa");
            assertEquals(1, events.size());

            // 3. Changing the value emits an event with both oldValue and newValue AFTER state mutation
            trufa.setName("Trufita");
            assertEquals(2, events.size());

            Change<?> change = events.get(1);
            assertEquals(STREAM, change.changeStream());
            assertEquals("name", change.fieldName());
            assertEquals("Trufa", change.oldValue());
            assertEquals("Trufita", change.fieldValue());
            assertSame(trufa, change.object());
            assertEquals("Trufita", ((DogTest) change.object()).getName());
        }

        // After closing the subscription, further changes are not received
        DogTest anotherDog = new ObservableDogTest("Max", "Pug", 5);
        anotherDog.setDaysOld(6);
        assertEquals(2, events.size());
    }
}
