package org.doodle.klein;

import org.doodle.klein.dtos.TimeSlotDto;
import org.doodle.klein.dtos.TimeSlotResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TimeSlotsAcceptanceTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateATimeSlot() throws InterruptedException {
        int amountOfThreads = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(amountOfThreads);

        CountDownLatch latch = new CountDownLatch(1);
        List<Future<TimeSlotResponse>> results = new ArrayList<>();

        for (int i = 0; i < amountOfThreads; i++) {

            int finalI = i;
            results.add(executorService.submit(() -> {

                latch.await();
                TimeSlotDto timeSlotDto = new TimeSlotDto();
                var stratTime = "0" + finalI + ":00";
                var finalTime = "0" + finalI + ":15";

                timeSlotDto.setStartTime(stratTime);
                timeSlotDto.setEndTime(finalTime);
                return restTemplate.postForEntity("/timeSlots/create", timeSlotDto, TimeSlotResponse.class).getBody();
            }));
        }

        latch.countDown();
        List<TimeSlotResponse> timeSlotResponses = new ArrayList<>();
        results.forEach(future -> {
            try {
                TimeSlotResponse timeSlotResponse = future.get(5, TimeUnit.SECONDS);
                timeSlotResponses.add(timeSlotResponse);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.shutdown();
        timeSlotResponses.forEach(timeSlotResponse -> {
            assertThat(timeSlotResponse.getStatus()).isEqualTo("OK");
        });
        assertThat(timeSlotResponses).hasSize(amountOfThreads);

    }

    @Test
    void shouldNotCreateATimeSlotInvalidDteFormat() {
        TimeSlotDto timeSlotDto = new TimeSlotDto();
        timeSlotDto.setStartTime("25:00");
        timeSlotDto.setEndTime("25:15");
        TimeSlotResponse response = restTemplate.postForEntity("/timeSlots/create", timeSlotDto, TimeSlotResponse.class).getBody();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ERROR");
    }

    @Test
    void shouldUpdateATimeSlot() {
        TimeSlotDto createDto = new TimeSlotDto();
        createDto.setStartTime("08:00");
        createDto.setEndTime("08:30");
        TimeSlotResponse created = restTemplate.postForEntity("/timeSlots/create", createDto, TimeSlotResponse.class).getBody();
        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo("OK");
        String id = created.getTimeSlot().getId();

        TimeSlotDto updateDto = new TimeSlotDto();
        updateDto.setId(id);
        updateDto.setStartTime("09:00");
        updateDto.setEndTime("09:45");
        TimeSlotResponse updated = restTemplate.postForEntity("/timeSlots/update", updateDto, TimeSlotResponse.class).getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo("OK");
        assertThat(updated.getTimeSlot().getStartTime()).isEqualTo("9:00");
        assertThat(updated.getTimeSlot().getEndTime()).isEqualTo("9:45");
    }

    @Test
    void shouldDeleteATimeSlot() {
        TimeSlotDto createDto = new TimeSlotDto();
        createDto.setStartTime("10:00");
        createDto.setEndTime("10:15");
        TimeSlotResponse created = restTemplate.postForEntity("/timeSlots/create", createDto, TimeSlotResponse.class).getBody();
        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo("OK");
        String id = created.getTimeSlot().getId();

        TimeSlotDto deleteDto = new TimeSlotDto();
        deleteDto.setId(id);
        TimeSlotResponse deleted = restTemplate.postForEntity("/timeSlots/delete", deleteDto, TimeSlotResponse.class).getBody();
        assertThat(deleted).isNotNull();
        assertThat(deleted.getStatus()).isEqualTo("OK");
    }

    @Test
    void shouldUpdateATimeSlotStatus() {
        TimeSlotDto createDto = new TimeSlotDto();
        createDto.setStartTime("07:00");
        createDto.setEndTime("07:15");
        TimeSlotResponse created = restTemplate.postForEntity("/timeSlots/create", createDto, TimeSlotResponse.class).getBody();
        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo("OK");
        String id = created.getTimeSlot().getId();

        TimeSlotDto toggleDto1 = new TimeSlotDto();
        toggleDto1.setId(id);
        TimeSlotResponse toggled1 = restTemplate.postForEntity("/timeSlots/statusToggle", toggleDto1, TimeSlotResponse.class).getBody();
        assertThat(toggled1).isNotNull();
        assertThat(toggled1.getStatus()).isEqualTo("OK");
        assertThat(toggled1.getTimeSlot().getStatusId()).isTrue();

        TimeSlotDto toggleDto2 = new TimeSlotDto();
        toggleDto2.setId(id);
        TimeSlotResponse toggled2 = restTemplate.postForEntity("/timeSlots/statusToggle", toggleDto2, TimeSlotResponse.class).getBody();
        assertThat(toggled2).isNotNull();
        assertThat(toggled2.getStatus()).isEqualTo("OK");
        assertThat(toggled2.getTimeSlot().getStatusId()).isFalse();
    }
}
