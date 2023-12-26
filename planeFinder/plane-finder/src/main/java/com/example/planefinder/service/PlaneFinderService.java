package com.example.planefinder.service;

import com.example.planefinder.FlightGenerator;
import com.example.planefinder.entity.Aircraft;
import com.example.planefinder.repository.PlaneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PlaneFinderService {
    private final PlaneRepository repo;
    private final FlightGenerator generator;
    // 항공기 정보를 가져올 외부 URL
    private final URL acURL;
    // JSON 데이터를 객체로 변환하기 위한 Jackson ObjectMapper
    private final ObjectMapper om;

    @SneakyThrows   // 예외를 감추고 RuntimeException으로 변환
    public PlaneFinderService(PlaneRepository repo, FlightGenerator generator) {
        this.repo = repo;
        this.generator = generator;

        // 외부 URL 설정
        acURL = new URL("http://192.168.1.139/ajax/aircraft");
        om = new ObjectMapper();
    }

    // 외부 URL에서 항공기 정보를 가져와서 처리
    public Iterable<Aircraft> getAircraft() throws IOException {
        List<Aircraft> positions = new ArrayList<>();

        JsonNode aircraftNodes = null;

        try {
            // ObjectMapper를 사용해 외부 URL에서 JSON 데이터를 읽어와서 JsonNode 객체로 변환
            aircraftNodes = om.readTree(acURL)
                    .get("aircraft");     // JsonNode에서 키 "aircraft"에 해당하는 값(배열) 가져옴

            // forEachRemaining()은 기존의 while(iterator.hasNext()) { iterator.next() }을 한 메서드로 표현한 것 (JAVA 8)
            // "aircraft" 배열의 각각의 JSON 노드에 대해 동작 수행
            aircraftNodes.iterator().forEachRemaining(node -> {
                try {
                    // 각 JSON 노드를 Aircraft 객체로 변환해 positions 리스트에 추가
                    positions.add(om.treeToValue(node, Aircraft.class));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.out.println("\n>>>> IO Exception: " + e.getLocalizedMessage() + ", generating and providing sample data.\n");

            // 예외 발생시 샘플 데이터를 생성해서 반환
            return saveSamplePositions();
        }

        // 수신한 항공기 리스트가 존재한다면
        if (!positions.isEmpty()) {
            positions.forEach(System.out::println);

            // 기존 데이터 삭제 및 새 데이터 저장 후 반환
            repo.deleteAll();
            Iterable<Aircraft> aircrafts = repo.saveAll(positions);
            return aircrafts;
        } else {
            System.out.println("\n>>>> No positions to report, generating and providing sample data.\n");

            // 수신한 항공기 정보가 없다면 샘플 데이터 생성해서 반환
            return saveSamplePositions();
        }
    }

    // 샘플 데이터 생성 로직
    private Iterable<Aircraft> saveSamplePositions() {
        final Random rnd = new Random();

        // 기존 데이터 삭제
        repo.deleteAll();

        // 항공기 랜덤 개수 생성 후 저장
        for (int i = 0; i < rnd.nextInt(10); i++) {
            repo.save(generator.generate());
        }

        // 저장한 데이터 조회 후 반환
        return repo.findAll();
    }
}
