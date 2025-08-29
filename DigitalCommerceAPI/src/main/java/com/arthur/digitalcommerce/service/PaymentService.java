package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import com.arthur.digitalcommerce.model.Order;
import com.arthur.digitalcommerce.model.Payment;

public interface PaymentService {

    /**
     * Cria e persiste uma entidade de Pagamento a partir da resposta da API de pagamento.
     *
     * @param order A ordem à qual este pagamento está associado.
     * @param response O DTO (Data Transfer Object) com a resposta da API externa.
     * @return A entidade Payment que foi salva no banco de dados.
     */
    Payment createPaymentFromResponse(Order order, PixPaymentResponse response);

}