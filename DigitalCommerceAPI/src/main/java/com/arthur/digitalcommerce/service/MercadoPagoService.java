// src/main/java/com/arthur/digitalcommerce/service/MercadoPagoService.java
package com.arthur.digitalcommerce.service;

import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

public interface MercadoPagoService {
    Preference createPaymentPreference(PreferenceRequest preferenceRequest) throws MPException, MPApiException;
}