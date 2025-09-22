package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.mercadopago.MercadoPagoNotificationDTO;
import com.arthur.digitalcommerce.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private WebhookService webhookService;

   /* @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoNotification(@RequestBody MercadoPagoNotificationDTO notification) {
        logger.info("Recebida notificação do Mercado Pago: {}", notification);

        // Ação "payment.updated" é a mais comum agora
        if (notification != null && ( "payment.updated".equals(notification.getAction()) || "payment.created".equals(notification.getAction()) )) {
            String paymentId = notification.getData().getId();
            webhookService.processPaymentNotification(paymentId);
        }

        return ResponseEntity.ok().build();
    }*/
}