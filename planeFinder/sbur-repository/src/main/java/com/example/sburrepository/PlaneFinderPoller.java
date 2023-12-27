package com.example.sburrepository;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@EnableScheduling
@Component
public class PlaneFinderPoller {
    private WebClient client = WebClient.create("http://localhost:7634/aircraft");
    private final RedisConnectionFactory connectionFactory;

    // RedisOperations -> AircraftRepository
    private final AircraftRepository repository;

    public PlaneFinderPoller(RedisConnectionFactory connectionFactory, AircraftRepository repository) {
        this.connectionFactory = connectionFactory;
        this.repository = repository;
    }

    // pollPlanes 메서드를 1000ms 당 한 번(초당 1회) 실행
    @Scheduled(fixedRate = 1000)
    private void pollPlanes() {
        // 기존 데이터 삭제
        connectionFactory.getConnection().serverCommands().flushDb();

        // 새로운 데이터 가져와 레디스에 저장
        client.get()    // 지정된 URL에 HTTP GET 요청 생성
                .retrieve() // GET 요청 후 응답 받아오기
                .bodyToFlux(Aircraft.class) // 응답받은 값을 Aircraft 클래스의 Flux로 변환
                                            // Flux는 Reactor 라이브러리가 제공하는 비동기 스트림으로 여러 개의 항공기 다루기 적합
                .filter(plane -> !plane.getReg().isEmpty())     // Flux에서 각 항공기를 가져와 등록번호가 비어있지 않은 것만 필터링
                .toStream()     // Flux를 자바의 스트림으로 변환
                .forEach(repository::save);     // Redis에 각 항공기 저장

        // Redis 모든 데이터 출력
        repository.findAll().forEach(System.out::println);
    }
}
