package com.example.sburmongo

import org.springframework.data.repository.CrudRepository

interface AircraftRepository: CrudRepository<Aircraft.Aircraft, String>