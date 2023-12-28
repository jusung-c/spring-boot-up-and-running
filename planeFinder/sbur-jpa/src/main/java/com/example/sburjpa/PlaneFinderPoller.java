package com.example.sburjpa;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class PlaneFinderPoller {
    /*
        Lombok의 @NonNull
            - 해당 필드가 Null이 될 수 없다는 뜻
            - Lombok이 자동으로 AircraftRepository를 매개변수로 갖는 생성자를 생성한다.
     */
    @NonNull
    private final AircraftRepository repository;

    private WebClient client = WebClient.create("http://localhost:7634/aircraft");

    @Scheduled(fixedRate = 1000)
    private void pollPlanes() {
         repository.deleteAll();

        client.get()
                .retrieve()
                .bodyToFlux(Aircraft.class)
                .filter(plane -> !plane.getReg().isEmpty())
                .toStream().forEach(repository::save);

        repository.findAll().forEach(System.out::println);
    }
}
