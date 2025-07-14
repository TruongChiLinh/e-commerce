package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    private static final String ORDER_SERVICE_WEBHOOK_URL = "http://localhost:8083/api/webhooks/payment-status";

    @Autowired
    private RestTemplate restTemplate;

    public void notifyOrderStatusChange(Long orderId, String paymentStatus) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", orderId);
            payload.put("status", paymentStatus);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(ORDER_SERVICE_WEBHOOK_URL, entity, Object.class);
            logger.info("Sent payment status webhook for order {}: {}", orderId, paymentStatus);

        } catch (Exception e) {
            logger.error("Failed to send payment status webhook for order {}: {}", orderId, paymentStatus, e);
        }
    }
}