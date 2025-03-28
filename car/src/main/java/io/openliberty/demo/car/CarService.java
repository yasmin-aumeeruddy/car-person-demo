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
import java.util.UUID;

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
    Tracer tracer;

    @Inject
    Span carNotFoundSpan;

    @Inject
    Span findingCarSpan;

    @Inject
    Meter meter;

    LongCounter carCreatedCounter;

    @PostConstruct
    public void init(){
        carCreatedCounter = meter.counterBuilder("car.created").build();
    }

    public String get(UUID personId) {
        personClient.init(PERSON_HOST, PERSON_PORT, personId);
        String person = personClient.getPerson();
        return person;
    }
    @GET
    public Collection<Car> getAllCars() {
        System.out.println(System.getProperties());
        return carRepo.values();
    }

    @GET
    @Path("/getCar/{carId}")
    public String getCar(@PathParam("carId") @NotEmpty Long id) throws InterruptedException {
        Car foundCar = null;
        findingCarSpan = tracer.spanBuilder("findingCar").startSpan();
        try(Scope scope = findingCarSpan.makeCurrent()){
            foundCar = carRepo.get(id);
            if (foundCar == null){
                carNotFoundSpan = tracer.spanBuilder("carNotFound").startSpan();
                try(Scope subScope = carNotFoundSpan.makeCurrent()){
                    Thread.sleep(3000);
                    return "Car with id " + id + " not found.";
                }
                finally{
                    carNotFoundSpan.end();
                }
            }
        }
        finally{
            findingCarSpan.end();
        }
        return foundCar.toString();
    }

    @GET
    @Path("/createCar")
    @Produces(MediaType.APPLICATION_JSON)
    public Car createCar(@QueryParam("make") @NotEmpty @Size(min=2, max=50) String make, @QueryParam("personId") UUID personId) {
        carCreatedCounter.add(1);
        Car c = new Car(make, personId);
        carRepo.put(c.id, c);
        return c;
    }

}
