package com.example.redis;

import com.example.redis.entity.Aircraft;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootApplication
public class RedisApplication {

    /*
        redisOperations(빈 생성 메서드)
            - Redis와 상호작용하기 위한 RedisTemplate 빈을 설정(반환)
            - RedisConnectionFactory를 매개변수로 받아서 Redis 연결을 구성
     */
    @Bean
    public RedisTemplate<String, Aircraft> redisOperations(RedisConnectionFactory factory) {
        // Jackson 라이브러리를 사용해 Aircraft 클래스를 JSON 형식으로 직렬화/역직렬화
        Jackson2JsonRedisSerializer<Aircraft> serializer = new Jackson2JsonRedisSerializer<>(Aircraft.class);

        // Redis와 상호작용하는데 사용되는 스프링이 지원하는 클래스로 키는 문자열이고 값은 Aircraft 객체이다.
        RedisTemplate<String, Aircraft> template = new RedisTemplate<>();

        // Redis 연결 팩토리 설정
        template.setConnectionFactory(factory);
        // 기본 serializer 설정
        template.setDefaultSerializer(serializer);
        // 키에 대한 직렬화기 설정(String 타입)
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }
}
