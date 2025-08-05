package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.*;
import com.arthur.digitalcommerce.payload.OrderDTO;
import com.arthur.digitalcommerce.payload.OrderItemDTO;
import com.arthur.digitalcommerce.payload.OrderRequestDTO;
import com.arthur.digitalcommerce.payload.PaymentDTO;
import com.arthur.digitalcommerce.repository.*;
import com.arthur.digitalcommerce.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    @Transactional
    public OrderDTO processOrder(String paymentMethod, OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();

        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        if (cart.getCartItems().isEmpty()) {
            throw new APIException("Cart is empty");
        }

        Address address = addressRepository.findById(orderRequestDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", orderRequestDTO.getAddressId()));

        // Criar pedido
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        // Criar pagamento inicial (status pendente)
        Payment payment = new Payment();
        payment.setPaymentMethod(paymentMethod);
        payment.setPgPaymentId(orderRequestDTO.getPgPaymentId());
        payment.setPgStatus("pending");
        payment.setPgResponseMessage("Awaiting confirmation from payment gateway");
        payment.setPgName(orderRequestDTO.getPgName());

        payment.setOrder(order);
        paymentRepository.save(payment);
        order.setPayment(payment);

        // Salvar pedido
        Order savedOrder = orderRepository.save(order);

        // Criar itens do pedido
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);

        // Atualizar estoque e limpar carrinho
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }
        cartService.clearCart(cart.getCartId());

        // Montar OrderDTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setAddressId(orderRequestDTO.getAddressId());
        orderDTO.getOrderItems().clear();
        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

        return orderDTO;
    }
}