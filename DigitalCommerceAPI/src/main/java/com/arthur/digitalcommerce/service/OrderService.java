package com.arthur.digitalcommerce.service;

import com.mercadopago.resources.preference.Preference;

public interface OrderService {
    Preference createPaymentPreference();
}