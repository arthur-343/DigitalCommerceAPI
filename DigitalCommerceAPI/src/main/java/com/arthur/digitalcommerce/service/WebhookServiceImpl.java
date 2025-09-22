package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.model.Order;
import com.arthur.digitalcommerce.model.Payment;
import com.arthur.digitalcommerce.repository.OrderRepository;
import com.arthur.digitalcommerce.repository.PaymentRepository;
/*import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WebhookServiceImpl implements WebhookService {
/*
    private static final Logger logger = LoggerFactory.getLogger(WebhookServiceImpl.class);

    @Value("${config.integrations.mercadopago.access-token}")
    private String mercadoPagoAccessToken;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Override
    @Transactional
    public void processPaymentNotification(String paymentIdStr) {
        try {
            Long paymentId = Long.parseLong(paymentIdStr);
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            // 1. Buscar os detalhes completos do pagamento na API do Mercado Pago
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment paymentInfo = client.get(paymentId);

            if (paymentInfo == null) {
                logger.warn("Pagamento com ID {} não encontrado no Mercado Pago.", paymentId);
                return;
            }

            // 2. Encontrar o pedido correspondente no nosso banco de dados
            Long orderId = Long.parseLong(paymentInfo.getExternalReference());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Pedido com ID " + orderId + " não encontrado."));

            // 3. (IMPORTANTE) Verificar se o pedido já foi processado para evitar duplicidade
            if ("PAID".equals(order.getOrderStatus())) {
                logger.info("Pedido {} já está com status PAID. Nenhuma ação necessária.", orderId);
                return;
            }

            // 4. Criar ou atualizar a entidade de Pagamento com os dados recebidos
            Payment paymentEntity = order.getPayment() != null ? order.getPayment() : new Payment();
            paymentEntity.setPgPaymentId(paymentInfo.getId().toString());
            paymentEntity.setPgStatus(paymentInfo.getStatus().toString());
            paymentEntity.setPgStatusDetail(paymentInfo.getStatusDetail());
            paymentEntity.setAmount(paymentInfo.getTransactionAmount());
            paymentEntity.setOrder(order);
            paymentEntity.setPaymentMethod(paymentInfo.getPaymentTypeId()); // CORREÇÃO APLICADA
            paymentEntity.setPgName("MercadoPago"); // Define o nome do provedor de pagamento

            // 5. Atualizar o status do pedido com base no status do pagamento
            if ("approved".equals(paymentInfo.getStatus().toString())) {
                order.setOrderStatus("PAID");
                paymentEntity.setConfirmedAt(LocalDateTime.now());
                logger.info("Pedido {} atualizado para PAID.", orderId);

                // 6. Se aprovado, limpar o carrinho do usuário
                cartService.clearCartByUserEmail(order.getEmail());
                logger.info("Solicitada limpeza do carrinho para o usuário {}.", order.getEmail());

            } else {
                order.setOrderStatus("PAYMENT_FAILED");
                logger.info("Pagamento para o pedido {} falhou com status: {}", orderId, paymentInfo.getStatus());
            }

            // 7. Salvar as entidades atualizadas no banco de dados
            paymentRepository.save(paymentEntity);
            order.setPayment(paymentEntity);
            orderRepository.save(order);

        } catch (MPException | MPApiException e) {
            logger.error("Erro ao processar notificação do Mercado Pago: ", e);
        } catch (NumberFormatException e) {
            logger.error("ID de pagamento inválido recebido do webhook: {}", paymentIdStr);
        }
    }*/
}