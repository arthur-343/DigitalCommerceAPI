package com.arthur.digitalcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long paymentId;
    private Long orderId; // Representa a relação com a Ordem
    private String paymentMethod;
    private String pgPaymentId;
    private String pgStatus;
    private String pgStatusDetail;
    private String pgResponseMessage;
    private String pgName;
    private BigDecimal amount;
    private LocalDateTime confirmedAt;

}