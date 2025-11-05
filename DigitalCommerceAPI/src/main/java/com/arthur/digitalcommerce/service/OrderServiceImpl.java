package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.*;
import com.arthur.digitalcommerce.repository.*;
import com.arthur.digitalcommerce.util.AuthUtil;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository; // Adicionado
    private final AuthUtil authUtil;
    private final MercadoPagoService mercadoPagoService;

    @Value("${config.integrations.webhook.base-url}")
    private String webhookBaseUrl;

    @Override
    @Transactional
    public Preference createPaymentPreference(Long addressId) {
        User user = authUtil.loggedInUser();
        Cart userCart = cartRepository.findByUser(user)
                .orElseThrow(() -> new APIException("Cart not found for user: " + user.getUserName()));

        if (userCart.getCartItems().isEmpty()) {
            throw new APIException("Cannot create a payment preference for an empty cart.");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new APIException("User does not have permission to use this address.");
        }

        logger.info("Validating cart for checkout...");
        this.validateCartForCheckout(userCart);
        logger.info("Cart validation successful.");

        Order newOrder = createOrderInDatabase(user, userCart, address);

        try {
            PreferenceRequest preferenceRequest = buildPreferenceRequest(newOrder, user, address);

            logger.info("Sending preference request to MercadoPagoService...");
            return mercadoPagoService.createPaymentPreference(preferenceRequest);

        } catch (MPException | MPApiException e) {
            logger.error("Error creating preference in Mercado Pago", e);

            if (e instanceof MPApiException) {
                MPApiException apiException = (MPApiException) e;
                logger.error("Mercado Pago API Error Details: {}", apiException.getApiResponse().getContent());
            }
            throw new RuntimeException("Failed to communicate with Mercado Pago.", e);
        }
    }

    private Order createOrderInDatabase(User user, Cart userCart, Address address) {
        Order newOrder = new Order();
        newOrder.setEmail(user.getEmail());
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setOrderStatus("PENDING_PAYMENT");
        newOrder.setAddress(address); // Endereço agora é salvo

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal orderTotal = BigDecimal.ZERO;

        for (CartItem cartItem : userCart.getCartItems()) {
            Product product = cartItem.getProduct();

            BigDecimal realProductPrice;
            if (product.isSpecialPriceActive() && product.getSpecialPrice() != null) {
                realProductPrice = product.getSpecialPrice();
            } else {
                realProductPrice = product.getPrice();
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(BigDecimal.ZERO);
            orderItem.setOrderedProductPrice(realProductPrice);
            orderItem.setOrder(newOrder);

            orderItems.add(orderItem);

            BigDecimal subtotal = realProductPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderTotal = orderTotal.add(subtotal);
        }

        newOrder.setOrderItems(orderItems);
        newOrder.setTotalAmount(orderTotal);

        Order savedOrder = orderRepository.save(newOrder);
        orderItemRepository.saveAll(orderItems);

        logger.info("Order {} created in database with status PENDING_PAYMENT.", savedOrder.getOrderId());
        return savedOrder;
    }

    private PreferenceRequest buildPreferenceRequest(Order order, User user, Address address) {
        List<PreferenceItemRequest> items = order.getOrderItems().stream()
                .map(orderItem -> PreferenceItemRequest.builder()
                        .id(orderItem.getProduct().getProductId().toString())
                        .title(orderItem.getProduct().getProductName())
                        .quantity(orderItem.getQuantity())
                        .currencyId("BRL")
                        .unitPrice(orderItem.getOrderedProductPrice())
                        .build())
                .collect(Collectors.toList());

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(user.getUserName())
                .email(user.getEmail())
                .identification(IdentificationRequest.builder()
                        .type("CPF")
                        .number(user.getCpf())
                        .build())
                .build();

        PreferenceReceiverAddressRequest receiverAddress = PreferenceReceiverAddressRequest.builder()
                .zipCode(address.getCep())
                .stateName(address.getState())
                .cityName(address.getCity())
                .streetName(address.getStreet())
                .streetNumber(address.getBuildingName()) // Assumindo que buildingName é o número/complemento
                .build();

        PreferenceShipmentsRequest shipments = PreferenceShipmentsRequest.builder()
                .receiverAddress(receiverAddress)
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:5173/payment/success")
                .failure("http://localhost:5173/payment/failure")
                .pending("http://localhost:5173/payment/pending")
                .build();

        String notificationUrl = this.webhookBaseUrl + "/api/webhooks/mercadopago";
        logger.info("Webhook Notification URL set to: {}", notificationUrl);

        return PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .shipments(shipments) // Dados de envio adicionados
                .backUrls(backUrls)
                .externalReference(order.getOrderId().toString())
                .notificationUrl(notificationUrl)
                .build();
    }

    private void validateCartForCheckout(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new APIException("Cannot proceed to checkout with an empty cart.");
        }

        for (CartItem item : cart.getCartItems()) {
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new APIException("Product '" + item.getProduct().getProductName() +
                            "' is no longer available. Please remove it from your cart."));

            if (product.getQuantityInStock() <= 0) {
                throw new APIException("Sorry, '" + product.getProductName() +
                        "' is now out of stock. Please remove it from your cart to proceed.");
            } else if (item.getQuantity() > product.getQuantityInStock()) {
                throw new APIException("Cannot proceed to checkout. Product '" + product.getProductName() +
                        "' has insufficient stock. Available: " + product.getQuantityInStock() +
                        ", in your cart: " + item.getQuantity());
            }
        }
    }
}