package com.paymentchain.infraestucturedomain.keycloak_adapter.services;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
public class JwtService {

    @Value("${keycloak.jwk-set-uri}")
    private String jwksUrl;

    @Value("${keycloak.certs-id}")
    private String certsId;

    @Cacheable
    public Jwk getJwk() throws MalformedURLException, JwkException {
        URL url = new URL(jwksUrl);
        UrlJwkProvider urlJwkProvider = new UrlJwkProvider(url);
        return urlJwkProvider.get(certsId.trim());
    }
}
