package com.paymentchain.infraestucturedomain.keycloak_adapter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.infraestucturedomain.keycloak_adapter.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeycloakRestService {

    private final RestTemplate restTemplate;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.authorization-grant-type}")
    private String grantType;

    @Value("${keycloak.authorization-grant-type-refresh}")
    private String grantTypeRefresh;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.scope}")
    private String scope;

    public KeycloakRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    /*
    login by using username and password to keycloak, and capturing token on response body
     */
    public String login (String username, String password){
        MultiValueMap<String,String> map =  new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password",password);
        map.add("client_id",clientId);
        map.add("grant_type",grantType);
        map.add("client_secrete",clientSecret);
        map.add("scope",scope);

        HttpEntity<MultiValueMap<String,String>> request =new HttpEntity<>(map,new HttpHeaders());
        return this.restTemplate.postForObject(keycloakTokenUri,request, String.class);
    }

    /*
    A successful user token will generate http code 200, other than that will create an exception
     */
    public void checkValidity(String token) throws Exception{
       String response = getUserInfo(token);
       if(response == null || response.isEmpty()){
           throw new BusinessRuleException("is valid", "False", HttpStatus.FORBIDDEN);
       }
    }

    /*
    logging out and disabling active token from keycloak
     */
    public void logout(String refreshToken){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id",clientId);
        map.add("client_secret",clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(map,null);
        this.restTemplate.postForObject(keycloakLogout,request, String.class);
    }

    public List<String> getRoles(String token) throws JsonProcessingException {
        String response = getUserInfo(token);
        Map<String,Object> map =  new ObjectMapper().readValue(response
                , new TypeReference<HashMap<String,Object>>(){});
        Object rolesObject = map.get("roles");
        //return (List<String>) map.get("roles");

        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }

        //return (List<String>) map.get("roles");
    }


    private String getUserInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",token);
        HttpEntity<?> request =  new HttpEntity<>(null, headers);
        return this.restTemplate.postForObject(keycloakUserInfo,request, String.class);
    }

    /*

     */
    public String refresh(String refreshToken){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id",clientId);
        map.add("grant_type",grantTypeRefresh);
        map.add("refresh_token",refreshToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        return restTemplate.postForObject(keycloakTokenUri, request, String.class);
    }


}
