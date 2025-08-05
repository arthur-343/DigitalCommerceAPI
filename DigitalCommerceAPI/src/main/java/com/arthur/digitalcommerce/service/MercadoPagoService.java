package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.MercadoPagoPixResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MercadoPagoService {
    public MercadoPagoPixResponse createPixPayment(BigDecimal amount) {
        // Chamada real seria via HTTP para Mercado Pago API com token
        MercadoPagoPixResponse response = new MercadoPagoPixResponse();
        response.setId("123456789");
        response.setTransactionId("TX123456789");
        response.setQrCode("0002012658...520400005303986540..."); // EMV
        response.setQrCodeBase64("data:image/png;base64,iVBORw0KGgoAAAANS...");
        return response;
    }
}
