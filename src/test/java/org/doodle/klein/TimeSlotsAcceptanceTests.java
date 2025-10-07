package org.doodle.klein;

import org.doodle.klein.dtos.TimeSlotDto;
import org.doodle.klein.dtos.TimeSlotResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TimeSlotsAcceptanceTests {

    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateATimeSlot() throws InterruptedException {
        int amountOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(amountOfThreads);

        CountDownLatch latch = new CountDownLatch(1);
        List<Future<TimeSlotResponse>> results = new ArrayList<>();

        for (int i = 0; i< amountOfThreads; i++) {
            latch.await();
            results.add(executorService.submit(() -> {
                TimeSlotDto timeSlotDto = new TimeSlotDto();
                timeSlotDto.setStartTime("12:00");
                timeSlotDto.setEndTime("12:15");
                    return restTemplate.postForEntity("/timeSlots/create", timeSlotDto, TimeSlotResponse.class).getBody();
            }));
        }

        latch.countDown();
        results.forEach(future -> {
            try {
                TimeSlotResponse timeSlotResponse = future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });

    }

    void shouldNotCreateATimeSlot() {

    }

    void shouldUpdateATimeSlot() {

    }
    void shouldDeleteATimeSlot() {

    }
}
