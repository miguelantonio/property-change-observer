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

@Observe(stream = STREAM)
public class DogTest {

    public static final String STREAM = "animals";

    private String name;
    private String breed;
    private int daysOld;

    public DogTest(String name, String breed, int daysOld) {
        this.name = name;
        this.breed = breed;
        this.daysOld = daysOld;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getDaysOld() {
        return daysOld;
    }

    public void setDaysOld(int daysOld) {
        this.daysOld = daysOld;
    }

    @Override
    public String toString() {
        return "DogTest{" + "name=" + name + ", breed=" + breed + ", daysOld=" + daysOld + '}';
    }

}
