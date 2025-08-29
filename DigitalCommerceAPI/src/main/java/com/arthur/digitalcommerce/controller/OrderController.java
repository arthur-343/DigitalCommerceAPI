package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import com.arthur.digitalcommerce.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/pay/pix")
    public ResponseEntity<PixPaymentResponse> processOrderPayment(HttpServletRequest request) {
        PixPaymentResponse pixResponse = orderService.processOrderPayment(request);
        return ResponseEntity.ok(pixResponse);
    }
}