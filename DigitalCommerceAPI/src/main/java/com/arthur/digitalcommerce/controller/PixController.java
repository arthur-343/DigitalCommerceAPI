package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentRequest;
import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import com.arthur.digitalcommerce.service.PixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PixController {

    @Autowired
    private PixService pixService; // Injete o novo serviço

    @PostMapping("/api/pix/payments")
    public PixPaymentResponse createPixPayment(@RequestBody PixPaymentRequest request) {
        // A única responsabilidade do controller é delegar a chamada para o serviço
        return pixService.createPixPayment(request);
    }
}