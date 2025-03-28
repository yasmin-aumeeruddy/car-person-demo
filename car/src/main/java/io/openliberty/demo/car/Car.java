/*
 * Copyright (c) 2025 IBM Corporation and others
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openliberty.demo.car;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class Car {

    private static final Random r = new Random();

    @NotNull
    public final Long id;

    @NotNull
    @Size(min = 2, max = 50)
    public final String make;

    public final UUID personId;

    public Car(String make, UUID personId) {
        this(make, null, personId);
    }

    @JsonbCreator
    public Car(@JsonbProperty("make") String make,
            @JsonbProperty("id") Long id, 
            @JsonbProperty("personId") UUID personId) {
        this.make = make;
        this.id = id == null ? r.nextLong() : id;
        this.personId = personId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Car))
            return false;
        Car other = (Car) obj;
        return Objects.equals(id, other.id) &&
               Objects.equals(make, other.make);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, make);
    }
}
