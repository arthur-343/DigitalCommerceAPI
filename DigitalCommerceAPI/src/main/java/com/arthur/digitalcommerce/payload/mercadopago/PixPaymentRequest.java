package com.arthur.digitalcommerce.payload.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PixPaymentRequest {

    private String accessToken;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("payment_method_id")
    private String paymentMethodId;

    @JsonProperty("payer")
    private Payer payer;
}