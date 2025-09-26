package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.model.*;
import com.arthur.digitalcommerce.repository.*;
import com.arthur.digitalcommerce.util.AuthUtil;
/*import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;*/
import org.modelmapper.ModelMapper;
import com.mercadopago.client.preference.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {


//usar     Cart cart = getOrCreateCartForCurrentUser(); // Supondo que você mova este método para cá
    /*
// Exemplo de como ficaria seu OrderServiceImpl.java

@Service
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final CartRepository cartRepository;
    // ... outros repositórios como OrderRepository

    public OrderServiceImpl(CartService cartService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @Override
    @Transactional
    public OrderDTO createOrder() {
        // 1. Pega o carrinho do usuário atual.
        String email = authUtil.loggedInEmail(); // Supondo que você tenha AuthUtil aqui
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseThrow(() -> new APIException("Cannot create order from an empty cart."));

        // 2. CHAMA A VALIDAÇÃO DO CARRINHO (O PASSO DE SEGURANÇA)
        // Se houver qualquer problema, uma exceção será lançada aqui e o método irá parar.
        ((CartServiceImpl) cartService).validateCartForCheckout(cart);

        // 3. Se a validação passou, prossiga com a lógica de criar o pedido...
        // - Criar um novo objeto Order
        // - Mover os CartItems para OrderItems
        // - Deduzir o estoque dos produtos
        // - Limpar o carrinho
        // - Salvar o pedido e retornar o OrderDTO

        // ...
    }
}    */

/*    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired private CartRepository cartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private AuthUtil authUtil;
    @Autowired private MercadoPagoService mercadoPagoService;

    @Value("${config.integrations.webhook.base-url}")
    private String webhookBaseUrl;

    @Override
    @Transactional
    public Preference createPaymentPreference() {
        // 1. Obter o usuário e seu carrinho
        User user = authUtil.loggedInUser();
        Cart userCart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado para o utilizador: " + user.getUserName()));

        if (userCart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Não é possível criar uma preferência de pagamento para um carrinho vazio.");
        }

        // 2. Criar o pedido (Order) no banco de dados com status pendente
        Order newOrder = createOrderInDatabase(user, userCart);

        try {
            // 3. Montar a requisição de preferência para o Mercado Pago
            PreferenceRequest preferenceRequest = buildPreferenceRequest(newOrder, user);

            // 4. Chamar o serviço do Mercado Pago para criar a preferência
            logger.info("Enviando solicitação de preferência para o MercadoPagoService.");
            return mercadoPagoService.createPaymentPreference(preferenceRequest);

        } catch (MPException | MPApiException e) {
            logger.error("Erro ao criar preferência no Mercado Pago", e);

            // ADICIONE ESTE TRECHO PARA VER O ERRO DETALHADO
            if (e instanceof MPApiException) {
                MPApiException apiException = (MPApiException) e;
                logger.error("Detalhes da API do Mercado Pago: {}", apiException.getApiResponse().getContent());
            }

            throw new RuntimeException("Falha ao se comunicar com o Mercado Pago.", e);
        }

    }

    private Order createOrderInDatabase(User user, Cart userCart) {
        Order newOrder = new Order();
        newOrder.setTotalAmount(userCart.getTotalPrice());
        newOrder.setEmail(user.getEmail());
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setOrderStatus("PENDING_PAYMENT");

        List<OrderItem> orderItems = userCart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setOrderedProductPrice(cartItem.getProductPrice());
                    orderItem.setOrder(newOrder);

                    // LINHA ADICIONADA PARA A CORREÇÃO
                    orderItem.setDiscount(BigDecimal.ZERO); // Define o desconto como 0

                    return orderItem;
                }).collect(Collectors.toList());

        newOrder.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(newOrder);
        orderItemRepository.saveAll(orderItems);

        logger.info("Pedido {} criado no banco de dados com status PENDING_PAYMENT.", savedOrder.getOrderId());

        return savedOrder;
    }
    private PreferenceRequest buildPreferenceRequest(Order order, User user) {
        // Mapear itens do pedido para itens da preferência
        List<PreferenceItemRequest> items = order.getOrderItems().stream()
                .map(orderItem -> PreferenceItemRequest.builder()
                        .id(orderItem.getProduct().getProductId().toString())
                        .title(orderItem.getProduct().getProductName())
                        .quantity(orderItem.getQuantity())
                        .currencyId("BRL")
                        .unitPrice(orderItem.getOrderedProductPrice())
                        .build())
                .collect(Collectors.toList());

        // Mapear dados do pagador
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(user.getUserName())
                .email(user.getEmail())
                .identification(IdentificationRequest.builder()
                        .type("CPF")
                        .number(user.getCpf())
                        .build())
                .build();

        // URLs de Retorno - este bloco já está correto, mas vamos confirmá-lo.
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:5173/payment/success")
                .failure("http://localhost:5173/payment/failure")
                .pending("http://localhost:5173/payment/pending")
                .build();

        String notificationUrl = this.webhookBaseUrl + "/api/webhooks/mercadopago";

        return PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                //.autoReturn("approved")
                .externalReference(order.getOrderId().toString())
                .notificationUrl(notificationUrl)
                .build();
    }

*/
}