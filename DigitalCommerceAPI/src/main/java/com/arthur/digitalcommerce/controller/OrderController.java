package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.OrderDTO;
import com.arthur.digitalcommerce.payload.OrderRequestDTO;
import com.arthur.digitalcommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderDTO order = orderService.processOrder(paymentMethod, orderRequestDTO);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
