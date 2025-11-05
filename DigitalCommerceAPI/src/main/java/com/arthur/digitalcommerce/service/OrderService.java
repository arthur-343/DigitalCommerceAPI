package com.arthur.digitalcommerce.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

public interface OrderService {
    Preference createPaymentPreference(Long addressId) throws MPException, MPApiException;
}