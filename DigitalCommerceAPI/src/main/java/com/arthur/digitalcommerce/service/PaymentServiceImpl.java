package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import com.arthur.digitalcommerce.model.Order;
import com.arthur.digitalcommerce.model.Payment;
import com.arthur.digitalcommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Import necessário

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPaymentFromResponse(Order order, PixPaymentResponse response) {
        if (response == null || response.getPointOfInteraction() == null) {
            throw new IllegalArgumentException("Resposta de pagamento inválida.");
        }

        PixPaymentResponse.TransactionData transactionData = response.getPointOfInteraction().getTransactionData();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod("PIX");
        payment.setPgName("MercadoPago");
        payment.setPgPaymentId(String.valueOf(response.getId()));

        payment.setPgStatus(response.getStatus());
        payment.setQrCode(transactionData.getQrCode());
        payment.setQrCodeBase64(transactionData.getQrCodeBase64());

        // Adiciona a data/hora da confirmação se o pagamento for aprovado
        if ("approved".equalsIgnoreCase(response.getStatus())) {
            payment.setConfirmedAt(LocalDateTime.now());
        }

        // Associa o pagamento ao pedido (importante para a relação bidirecional)
        order.setPayment(payment);

        return paymentRepository.save(payment);
    }
}