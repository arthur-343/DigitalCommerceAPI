package com.arthur.digitalcommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Order order;

    @NotBlank
    @Size(min = 3, message = "Payment method must contain at least 4 characters")
    private String paymentMethod; // Ex: "credit_card"

    private String pgPaymentId;
    private String pgStatus;
    private String pgStatusDetail;
    private String pgResponseMessage;
    private String pgName;

    private BigDecimal amount;

    private LocalDateTime confirmedAt;
}
