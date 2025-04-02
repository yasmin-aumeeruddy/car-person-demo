# Car and Person demo
Two Open Liberty services running MicroProfile Telemetry 2.0 to demonstrate metrics, logs and traces

Before running this demo, it is recommended to set up a a backend system to collect the telemetry data from the application. 

## Running the backend exporters
See the [docker-otel-lgtm](https://github.com/grafana/docker-otel-lgtm) repository and clone the repo:

```
git clone https://github.com/grafana/docker-otel-lgtm.git 
cd docker-otel-lgtm/docker
```

Build and run the image containing all services required for gathering traces, metrics and logs:

```
podman build . -t grafana/otel-lgtm 
podman run -p 3000:3000 -p 4317:4317 -p 4318:4318 --rm -ti localhost/grafana/otel-lgtm
```
## Person Service:

This service allows users to create records of people, given their name and age.

Start the `Person` service: 

```
cd person 
mvn liberty:run
```

To create a new person with the name Bob and age 21, navigate to:

http://localhost:9080/people/createPerson?name=bob&age=21

You will see confirmation that a person has been created with a unique id:

`Person created with id 5859008369859564999`

To view all of the people that you have created, navigate to:

http://localhost:9080/people

You should see a result like the following, with the unique id shown previously: 

`[{"age":21,"id":5859008369859564999,"name":"bob"}]`

## Car Service:

This service allows users to create records of cars, given the make of the car and the id of the person who owns the car. 

Start the `Car` service:

```
cd car 
mvn liberty:run
```

Create a car associated with the person that you just created using the `Person` service. To do this, navgiate to the following endpoint replacing the personId with the `id` shown in your pevious result: 

`http://localhost:9081/cars/createCar?make=skoda&personId=5859008369859564999`

When the car has been created, a HTTP request is sent to the Person service to return the details of the person with the given id. Consequently, You should see a result like the following 

`Car created with id 3644955172396212837 owned by {"age":21,"id":5859008369859564999,"name":"bob"}`

To view all of the cars you have created, navigate to: 

http://localhost:9081/cars/

When you are done checking out the services, stop the Liberty instances by pressing CTRL+C in the command-line sessions where you ran Liberty.
