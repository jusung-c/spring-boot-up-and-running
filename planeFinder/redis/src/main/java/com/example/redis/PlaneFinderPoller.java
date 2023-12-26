package com.example.redis;

import com.example.redis.entity.Aircraft;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@EnableScheduling   // 스케줄링 기능 활성화 -> @Scheduled를 사용하면 메서드가 주기적으로 실행된다.
@Component          // 빈으로 등록해서 사용
public class PlaneFinderPoller {
    /*
        WebClient
            - 비동기적인 방식으로 HTTP 클라이언트 제공 (스프링 웹 리액티브에서 제공하는 기능)
            - http://localhost:7634/aircraft 주소로부터 데이터를 수신하기 위해 사용
     */
    private WebClient client = WebClient.create("http://localhost:7634/aircraft");

    // Redis 연결하기 위한 필수 빈
    private final RedisConnectionFactory connectionFactory;

    // Redis 연결 및 작업을 위한 필수 빈
    private final RedisOperations<String, Aircraft> redisOperations;

    // 필수 빈 2개 생성자 주입
    public PlaneFinderPoller(RedisConnectionFactory connectionFactory,
                             RedisOperations<String, Aircraft> redisOperations) {
        this.connectionFactory = connectionFactory;
        this.redisOperations = redisOperations;
    }


    // pollPlanes 메서드를 1000ms 당 한 번(초당 1회) 실행
    @Scheduled(fixedRate = 1000)
    private void pollPlanes() {
        // 자동연결된 ConnectionFactory로 DB에 연결 후 해당 연결로 서버 명령 flushDb()를 통해 존재하는 모든 키 삭제
        // 새로운 항공기 정보를 가져오기 전 이전 항공기 정보 지우는 역할
        connectionFactory.getConnection().serverCommands().flushDb();

        // WebClinet를 사용해 항공기 정보들 가져와 레디스에 저장하는 로직
        client.get()    // 지정된 URL에 HTTP GET 요청 생성
                .retrieve() // GET 요청 후 응답 받아오기
                .bodyToFlux(Aircraft.class) // 응답받은 값을 Aircraft 클래스의 Flux로 변환
                                            // Flux는 Reactor 라이브러리가 제공하는 비동기 스트림으로 여러 개의 항공기 다루기 적합
                .filter(plane -> !plane.getReg().isEmpty())     // Flux에서 각 항공기를 가져와 등록번호가 비어있지 않은 것만 필터링
                .toStream()     // Flux를 자바의 스트림으로 변환
                .forEach(ac -> redisOperations.opsForValue().set(ac.getReg(), ac));     // Redis에 각 항공기 저장 - key: 등록번호, value: 항공기

        // Redis에서 모든 키를 가져와서 각 키에 해당하는 값 출력하는 로직
        redisOperations.opsForValue()   // RedisTemplate에서 값 연산을 수행하기 위한 ValueOperation 인터페이스 얻기
                                        // ValueOperation는 Redis에서 단일 값을 다루기 위한 메서드 제공
                .getOperations()    // ValueOperations에서 추가적인 고급 연산을 위해 Operations 인터페이스 얻기
                .keys("*")  // "*"는 와일드 카드로 모든 키를 선택해 가져온다.
                .forEach(ac -> System.out.println(redisOperations.opsForValue().get(ac)));  // 각 키에 대한 값을 가져와 출력
    }
}
