package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import jakarta.servlet.http.HttpServletRequest; // 1. Adicione esta importação

public interface OrderService {


    PixPaymentResponse processOrderPayment(HttpServletRequest request);
}