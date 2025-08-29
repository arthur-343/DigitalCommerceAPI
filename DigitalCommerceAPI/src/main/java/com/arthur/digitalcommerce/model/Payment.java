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
    private String paymentMethod; // PIX, CARD, BOLETO

    private String pgPaymentId;       // ID no provedor
    private String pgStatus;          // approved, pending, rejected
    private String pgStatusDetail;    // Detalhe: accredited, pending_contingency
    private String pgResponseMessage; // Mensagem do PSP
    private String pgName;            // Nome do provedor (ex.: MercadoPago)

    private BigDecimal amount;        // Valor do pagamento

    private String transactionId;     // TxId ou referência
    @Column(columnDefinition = "TEXT")
    private String qrCode;            // Código EMV
    @Column(columnDefinition = "TEXT")
    private String qrCodeBase64;      // Imagem do QR Code

    private LocalDateTime confirmedAt; // Quando foi aprovado
}
