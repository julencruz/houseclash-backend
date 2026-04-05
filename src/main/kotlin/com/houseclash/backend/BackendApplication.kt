package com.houseclash.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.resilience.annotation.EnableResilientMethods
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableResilientMethods
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
