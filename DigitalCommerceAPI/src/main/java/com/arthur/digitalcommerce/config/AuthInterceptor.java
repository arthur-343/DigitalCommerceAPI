package com.arthur.digitalcommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

public class AuthInterceptor implements ClientHttpRequestInterceptor {

    // Não precisamos mais do nome do cookie!
    public AuthInterceptor() {}

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest originalRequest = attributes.getRequest();
            String authHeader = originalRequest.getHeader(HttpHeaders.AUTHORIZATION);

            // Se o header Authorization existir na requisição original, repasse-o
            if (authHeader != null) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }

        return execution.execute(request, body);
    }
}