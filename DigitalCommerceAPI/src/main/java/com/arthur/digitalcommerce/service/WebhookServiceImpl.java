package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.model.Order;
import com.arthur.digitalcommerce.model.Payment;
import com.arthur.digitalcommerce.repository.OrderRepository;
import com.arthur.digitalcommerce.repository.PaymentRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WebhookServiceImpl implements WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookServiceImpl.class);

    private final String mercadoPagoAccessToken;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;

    @Autowired
    public WebhookServiceImpl(
            @Value("${config.integrations.mercadopago.access-token}") String mercadoPagoAccessToken,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            CartService cartService
    ) {
        this.mercadoPagoAccessToken = mercadoPagoAccessToken;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public void processPaymentNotification(String paymentIdStr) {
        try {
            Long paymentId = Long.parseLong(paymentIdStr);
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment paymentInfo = client.get(paymentId);

            if (paymentInfo == null) {
                logger.warn("Payment with ID {} not found in Mercado Pago.", paymentId);
                return;
            }

            Long orderId = Long.parseLong(paymentInfo.getExternalReference());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found."));

            if ("PAID".equals(order.getOrderStatus())) {
                logger.info("Order {} is already PAID. No action needed.", orderId);
                return;
            }

            Payment paymentEntity = order.getPayment() != null ? order.getPayment() : new Payment();
            paymentEntity.setPgPaymentId(paymentInfo.getId().toString());
            paymentEntity.setPgStatus(paymentInfo.getStatus().toString());
            paymentEntity.setPgStatusDetail(paymentInfo.getStatusDetail());
            paymentEntity.setAmount(paymentInfo.getTransactionAmount());
            paymentEntity.setOrder(order);
            paymentEntity.setPaymentMethod(paymentInfo.getPaymentTypeId());
            paymentEntity.setPgName("MercadoPago");

            if ("approved".equals(paymentInfo.getStatus().toString())) {
                order.setOrderStatus("PAID");
                paymentEntity.setConfirmedAt(LocalDateTime.now());
                logger.info("Order {} updated to PAID.", orderId);

                cartService.clearCartByUserEmail(order.getEmail());
                logger.info("Requested cart clearance for user {}.", order.getEmail());

            } else {
                order.setOrderStatus("PAYMENT_FAILED");
                logger.info("Payment for order {} failed with status: {}", orderId, paymentInfo.getStatus());
            }

            paymentRepository.save(paymentEntity);
            order.setPayment(paymentEntity);
            orderRepository.save(order);

        } catch (MPException | MPApiException e) {
            logger.error("Error processing Mercado Pago notification: ", e);
        } catch (NumberFormatException e) {
            logger.error("Invalid payment ID received from webhook: {}", paymentIdStr);
        }
    }
}