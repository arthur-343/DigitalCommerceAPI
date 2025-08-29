package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.model.*;
import com.arthur.digitalcommerce.payload.mercadopago.Payer;
import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentRequest;
import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import com.arthur.digitalcommerce.repository.CartRepository;
import com.arthur.digitalcommerce.repository.OrderItemRepository;
import com.arthur.digitalcommerce.repository.OrderRepository;
import com.arthur.digitalcommerce.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    //--- DEPENDÊNCIAS ---
    @Autowired private CartRepository cartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private AuthUtil authUtil;
    @Autowired private PaymentService paymentService;
    @Autowired private PixService pixService; // <-- INJETA O NOVO SERVIÇO DE PIX

    // Injetamos o token apenas para passá-lo para a requisição do PixService
    @Value("${mercadopago.access.token}")
    private String mercadopagoAccessToken;

    @Override
    @Transactional
    public PixPaymentResponse processOrderPayment(HttpServletRequest request) {
        Order order = createOrderFromCart();

        // 1. Prepara a requisição para o nosso PixService interno
        PixPaymentRequest pixRequest = new PixPaymentRequest();
        pixRequest.setAccessToken(this.mercadopagoAccessToken); // Passa o token
        pixRequest.setTransactionAmount(order.getTotalAmount());
        pixRequest.setDescription("Pagamento para o pedido #" + order.getOrderId());
        pixRequest.setPayer(new Payer(authUtil.loggedInEmail()));

        // 2. CHAMA DIRETAMENTE O SERVIÇO, SEM RESTTEMPLATE!
        PixPaymentResponse pixResponse = pixService.createPixPayment(pixRequest);

        // 3. Salva os detalhes do pagamento e retorna a resposta
        paymentService.createPaymentFromResponse(order, pixResponse);

        return pixResponse;
    }

    private Order createOrderFromCart() {
        User user = authUtil.loggedInUser();
        Cart userCart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado para o usuário: " + user.getUserName()));

        if (userCart.getCartItems().isEmpty()) {
            throw new RuntimeException("O carrinho está vazio. Não é possível criar um pedido.");
        }

        Order newOrder = new Order();
        newOrder.setTotalAmount(userCart.getTotalPrice());
        newOrder.setEmail(user.getEmail());
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setOrderStatus("PENDING_PAYMENT");

        List<OrderItem> orderItems = userCart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = modelMapper.map(cartItem, OrderItem.class);
                    orderItem.setOrder(newOrder);
                    orderItem.setOrderedProductPrice(cartItem.getProductPrice());
                    return orderItem;
                }).collect(Collectors.toList());

        newOrder.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(newOrder);
        orderItemRepository.saveAll(orderItems);

        userCart.getCartItems().clear();
        userCart.setTotalPrice(java.math.BigDecimal.ZERO);
        cartRepository.save(userCart);

        return savedOrder;
    }
}