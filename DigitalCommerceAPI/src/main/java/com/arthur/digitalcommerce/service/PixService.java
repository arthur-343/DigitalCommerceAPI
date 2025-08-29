package com.arthur.digitalcommerce.service;


import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentRequest;
import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;

public interface PixService {

    /**
     * Simula a criação de um pagamento PIX.
     * @param request Os dados da requisição de pagamento.
     * @return A resposta simulada com os dados do PIX.
     */
    PixPaymentResponse createPixPayment(PixPaymentRequest request);
}