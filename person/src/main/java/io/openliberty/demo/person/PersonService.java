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

package io.openliberty.demo.person;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/people")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonService {

    private final Map<Long, Person> personRepo = new HashMap<>();

    @GET
    @Path("/")
    public Collection<Person> getAllPeople() {
        return personRepo.values();
    }

    @GET
    @Path("/getPerson/{personId}")
    public Person getPerson(@PathParam("personId") @NotEmpty Long id) {
        Person foundPerson = personRepo.get(id);
        if (foundPerson == null)
            personNotFound(id);
        return foundPerson;
    }

    @GET
    @Path("/createPerson")
    public String createPerson(@QueryParam("name") @NotEmpty @Size(min=2, max=50) String name,
                             @QueryParam("age") @NotEmpty @PositiveOrZero int age) {
        Person p = new Person(name, age);
        personRepo.put(p.id, p);
        return "Person created with id " + p.id;
    }

    private void personNotFound(Long id) {
        throw new NotFoundException("Person with id " + id + " not found.");
    }

}