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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
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
import io.openliberty.demo.client.PersonClient;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.context.Scope;

@Path("/cars")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarService {

    private final Map<Long, Car> carRepo = new HashMap<>();
    private PersonClient personClient = new PersonClient();
    @Inject
    @ConfigProperty(name = "person.http.port")
    int PERSON_PORT;

    @Inject
    @ConfigProperty(name = "person.host")
    String PERSON_HOST;

    @Inject
    OpenTelemetry openTelemetry;

    @Inject
    Tracer tracer;

    @Inject
    Span carNotFoundSpan;
    
    @Inject
    Meter meter;

    LongCounter carCreatedCounter;

    @PostConstruct
    public void init(){
        carCreatedCounter = meter.counterBuilder("car.created").build();
    }

    public String get(Long personId) {
        personClient.init(PERSON_HOST, PERSON_PORT, personId);
        String person = personClient.getPerson();
        return person;
    }
    @GET
    public Collection<Car> getAllCars() {
        return carRepo.values();
    }

    @GET
    @Path("/getCar/{carId}")
    public Car getCar(@PathParam("carId") @NotEmpty Long id) {
        Car foundCar = carRepo.get(id);
        if (foundCar == null)
            carNotFoundSpan = tracer.spanBuilder("GettingPropertiesForHost").startSpan();
            try(Scope scope = carNotFoundSpan.makeCurrent()){
                carNotFound(id);
            }
            finally{
                carNotFoundSpan.end();
            }    
        return foundCar;
    }

    @GET
    @Path("/createCar")
    public String createCar(@QueryParam("make") @NotEmpty @Size(min=2, max=50) String make, @QueryParam("personId") Long personId) {
        carCreatedCounter.add(1);
        Car c = new Car(make, personId);
        carRepo.put(c.id, c);
        //Get personId
        String person = get(personId);
        return "Car created with id " + c.id + " owned by " + person;
    }

    private void carNotFound(Long id) {
        throw new NotFoundException("Car with id " + id + " not found.");
    }

}
