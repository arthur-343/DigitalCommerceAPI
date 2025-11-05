package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.mercadopago.MercadoPagoNotificationDTO;
import com.arthur.digitalcommerce.service.WebhookService;
import lombok.RequiredArgsConstructor; // Importar
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor // Adicionado para injeção de construtor
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService; // Adicionado 'final'

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoNotification(@RequestBody MercadoPagoNotificationDTO notification) {
        logger.info("Received Mercado Pago notification: {}", notification);

        // Ação "payment.updated" ou "payment.created" são as mais comuns
        if (notification != null && notification.getData() != null &&
                ( "payment.updated".equals(notification.getAction()) || "payment.created".equals(notification.getAction()) )) {

            String paymentId = notification.getData().getId();
            webhookService.processPaymentNotification(paymentId);
        }

        return ResponseEntity.ok().build();
    }
}