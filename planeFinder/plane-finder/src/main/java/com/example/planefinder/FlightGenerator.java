package com.example.planefinder;

import com.example.planefinder.entity.Aircraft;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

// 샘플 데이터 생성기
@Component
public class FlightGenerator {
    private final Random rnd = new Random();

    List<String> typeList = List.of("A319", "A320", "A321", // Airbus
            "BE33", "BE36", // Beechcraft
            "B737", "B739", "B763", // Boeing
            "C172", "C402", "C560", // Cessna
            "E50P", "E75L", // Embraer
            "MD11", // McDonnell Douglas!
            "PA28", "PA32", "PA46"); // Piper

    public Aircraft generate() {
        String csfn = "SAL" + rnd.nextInt(1000);

        return new Aircraft(csfn,
                "N" + String.format("%1$5s", rnd.nextInt(10000)).replace(' ', '0'),
                csfn,
                typeList.get(rnd.nextInt(typeList.size())),
                rnd.nextInt(40000),
                rnd.nextInt(369),
                rnd.ints(1, 100, 500).iterator().next(),
                rnd.doubles(1, 35d, 42d).iterator().next().floatValue(),
                rnd.doubles(1, -115d, -85d).iterator().next().floatValue());
    }
}
