package com.ticket.app.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/auth")
public class AuthController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

}
