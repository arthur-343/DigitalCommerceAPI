package com.arthur.digitalcommerce.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired; // Importe
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final String mercadoPagoAccessToken;

    @Autowired
    public MercadoPagoServiceImpl(@Value("${config.integrations.mercadopago.access-token}") String mercadoPagoAccessToken) {
        this.mercadoPagoAccessToken = mercadoPagoAccessToken;
    }

    @Override
    public Preference createPaymentPreference(PreferenceRequest preferenceRequest) throws MPException, MPApiException {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }
}