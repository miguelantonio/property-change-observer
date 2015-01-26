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
import org.junit.Test;
import static org.junit.Assert.*;

public class ObservableObjectAspectTest {

    /**
     * Test of class ObservableObjectAspect.
     */
    @Test
    public void test() {
        System.out.println("ObservableObjectAspect");
        final int trufitaDaysOld = 64;
        int[] result = new int[trufitaDaysOld];
        int[] expResult = new int[trufitaDaysOld];
        for (int i = 0; i < expResult.length; i++) {
            expResult[i] = i + 1;
        }

        ObserverSingleton.INSTANCE.addChangeListener(new ChangeListener() {
            int i = 0;

            @Override
            public void change(Change change) {
                if (change.getChangeStream().equals(STREAM)) {
                    result[i] = (Integer) change.getFieldValue();
                    i++;
                }
            }
        });
        DogTest trufa = new DogTest("Trufa", "Boston Terrier", 0);
        for (int input : expResult) {
            trufa.setDaysOld(input);
        }

        assertArrayEquals(expResult, result);
    }

}
