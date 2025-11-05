package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.service.OrderService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @PostMapping("/create-preference/{addressId}")
    public ResponseEntity<?> createPreference(@PathVariable Long addressId) {
        try {
            Preference preference = orderService.createPaymentPreference(addressId);
            return ResponseEntity.ok(preference);

        } catch (MPException | MPApiException e) {
            logger.error("Error creating Mercado Pago preference: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error communicating with payment gateway.");
        } catch (Exception e) {
            logger.error("Unexpected error in createPreference", e);
            return ResponseEntity.status(500).body("An internal server error occurred.");
        }
    }
}