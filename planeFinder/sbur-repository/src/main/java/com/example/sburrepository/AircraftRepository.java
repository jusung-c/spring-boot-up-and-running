package com.example.sburrepository;

import org.springframework.data.repository.CrudRepository;

// 스프링 데이터의 CrudRepository 상속
public interface AircraftRepository extends CrudRepository<Aircraft, Long> {

}
