package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.OrderDTO;
import com.arthur.digitalcommerce.payload.OrderRequestDTO;

public interface OrderService {
    OrderDTO processOrder(String paymentMethod, OrderRequestDTO orderRequestDTO);
}
