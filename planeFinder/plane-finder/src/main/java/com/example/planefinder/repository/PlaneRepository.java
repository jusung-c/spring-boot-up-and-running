package com.example.planefinder.repository;

import com.example.planefinder.entity.Aircraft;
import org.springframework.data.repository.CrudRepository;

public interface PlaneRepository extends CrudRepository<Aircraft, Long> {

}
