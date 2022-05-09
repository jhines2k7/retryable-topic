# retryable-topic

Simple Spring Boot application to demonstrate the use of the ***RetryTopicConfigurer*** available in spring-kafka:2.7+.
The application demonstrates using the ***@RetryableTopic*** annotation on a consumer listener method as well as 
configuring the ***RetryTopicConfigurationBuilder*** so that any method that uses the ***@KafkaListener*** annotation will use 
the settings defined in the configuration bean. In order to run the application, you will need to connect to 
a Kafka cluster and create a topic called "greetings"
```
bin/kafka-topics.sh --create --topic greetings --bootstrap-server localhost:9092
```
This application exposes a GET endpoint /greeting that takes a single query parameter "name".
Sending a GET request to 
```
localhost/greeting?name=James 
```
(for example) will write a message to a topic called "greetings".
Sending a GET request to 
```
localhost/greeting?name=Casey 
```
will throw an exception and that will trigger the retry logic. Spring Kafka will attempt to reprocess the 
message 3 times (this is configurable) at a fixed interval of 3 seconds (also configurable) after which it will write
the message to a "dead letter" topic.
