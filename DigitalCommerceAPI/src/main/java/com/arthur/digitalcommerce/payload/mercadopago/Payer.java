package com.arthur.digitalcommerce.payload.mercadopago;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Payer {
    @JsonProperty("email")
    private String email;
}