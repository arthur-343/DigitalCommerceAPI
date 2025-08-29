package com.arthur.digitalcommerce.payload.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixPaymentResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("point_of_interaction")
    private PointOfInteraction pointOfInteraction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointOfInteraction {
        @JsonProperty("transaction_data")
        private TransactionData transactionData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionData {
        @JsonProperty("qr_code")
        private String qrCode;

        @JsonProperty("qr_code_base64")
        private String qrCodeBase64;
    }
}