package com.arthur.digitalcommerce.payload.mercadopago;

import lombok.Data;

@Data
public class MercadoPagoNotificationDTO {
    private String action;
    private DataDTO data;

    @Data
    public static class DataDTO {
        private String id;
    }
}