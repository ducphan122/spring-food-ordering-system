package com.spring.food.ordering.system.payment.service.application.rest;

import com.spring.food.ordering.system.payment.service.domain.dto.TopUpRequest;
import com.spring.food.ordering.system.payment.service.domain.ports.input.service.PaymentApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/payments", produces = "application/vnd.api.v1+json")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/top-up")
    public ResponseEntity<Void> topUp(@RequestBody TopUpRequest topUpRequest) {
        log.info("Processing top-up request for customer: {}", topUpRequest.getCustomerId());
        paymentApplicationService.topUp(topUpRequest);
        return ResponseEntity.ok().build();
    }
}
