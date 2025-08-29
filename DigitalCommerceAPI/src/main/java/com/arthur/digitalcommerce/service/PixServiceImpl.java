package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentRequest;
import com.arthur.digitalcommerce.payload.mercadopago.PixPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class PixServiceImpl implements PixService {

    // Mantemos o token aqui para a simulação de validação
    @Value("${mercadopago.access.token}")
    private String mercadopagoAccessToken;

    @Override
    public PixPaymentResponse createPixPayment(PixPaymentRequest request) {
        // 1. Validação simples do token de acesso
        if (!mercadopagoAccessToken.equals(request.getAccessToken())) {
            throw new RuntimeException("Access token inválido.");
        }

        // 2. Gera um ID de pagamento único para cada requisição
        long paymentId = ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L);

        // 3. Gera dados simulados (mock) para a resposta
        String qrCodeIdentifier = UUID.randomUUID().toString();
        String qrCode = "00020126580014br.gov.bcb.pix0136" + qrCodeIdentifier + "5204000053039865802BR5913APINAME6009SAOPAULO62070503***6304E7DF";
        String qrCodeBase64 = "iVBORw0KGgoAAAANSUhEUgAAAQAAAAEAAQMAAABTxJm9AAAABlBMVEX///8AAABVwtLpAAAAAXRSTlMAQObYZgAAAFRJREFUeNrtwTEBAAAAwiD7pT2sBgAAAAAAAAAAAAAAAIz+y9u7AQAAAAAAgJgAAAAAAJgAAAAAAMwAAAAAAAAAAMwAAAAAAMwAAAAAAAAAAAAAAIBfgGk5AAGqO8WcAAAAAElFTSuQmCC";

        // 4. Constrói e retorna a resposta
        return PixPaymentResponse.builder()
                .id(paymentId)
                .status("pending")
                .pointOfInteraction(PixPaymentResponse.PointOfInteraction.builder()
                        .transactionData(PixPaymentResponse.TransactionData.builder()
                                .qrCode(qrCode)
                                .qrCodeBase64(qrCodeBase64)
                                .build())
                        .build())
                .build();
    }
}