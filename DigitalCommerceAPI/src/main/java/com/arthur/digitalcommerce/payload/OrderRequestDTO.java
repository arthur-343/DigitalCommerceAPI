package com.arthur.digitalcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Long addressId;
    private String pgName;       // Nome do gateway: "MercadoPago"
    private String pgPaymentId;  // ID retornado pelo provedor (depois da criação do PIX)
}
