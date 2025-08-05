package com.arthur.digitalcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPixResponse {
    private String id;
    private String transactionId;
    private String qrCode;
    private String qrCodeBase64;
}
