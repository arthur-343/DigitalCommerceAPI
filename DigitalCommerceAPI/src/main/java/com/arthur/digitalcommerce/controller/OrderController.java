package com.arthur.digitalcommerce.controller;

import com.mercadopago.resources.preference.Preference;
import com.arthur.digitalcommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Você pode usar o seu controller existente ou criar um novo
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference() {
        try {
            Preference preference = orderService.createPaymentPreference();
            // Retornamos o objeto de preferência completo. O frontend usará o 'id' ou 'initPoint'.
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}