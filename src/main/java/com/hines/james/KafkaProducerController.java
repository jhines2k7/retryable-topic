package com.hines.james;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    private final KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        sendMessage(String.format(template, name));

        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    private void sendMessage(String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("greetings", message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=["
                        + message + "] due to : " + ex.getMessage());
            }
        });
    }

//    @RetryableTopic(
//            attempts = "3",
//            backoff = @Backoff(delay = 1000, multiplier = 2.0),
//            autoCreateTopics = "false",
//            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = "greetings", groupId = "greetingGroup")
    public void listenGreetingGroup(String message) throws Exception{
        if(message.contains("Casey")) {
            throw new Exception("Exception thrown while processing message: " + message);
        }

        log.info("Received message in group greetingGroup: {}", message);
    }

    @KafkaListener(topics = "greetings-retry-0", groupId = "greetingGroup")
    public void listenGreetingsRetry0(String message) throws Exception{
        log.info("Unprocessable message sent to retry topic greetings-retry-0: {}", message);
    }

    @KafkaListener(topics = "greetings-retry-1", groupId = "greetingGroup")
    public void listenGreetingsRetry1(String message) throws Exception{
        log.info("Unprocessable message sent to retry topic greetings-retry-1: {}", message);
    }

    @DltHandler
    public void processMessage(String message) {
        log.error("Received unprocessable message from greetingGroup: {}", message);
    }
}
