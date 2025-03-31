package com.paymentchain.infraestucturedomain.keycloak_adapter.controllers;

import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.paymentchain.infraestucturedomain.keycloak_adapter.exceptions.BusinessRuleException;
import com.paymentchain.infraestucturedomain.keycloak_adapter.exceptions.TokenExpiredException;
import com.paymentchain.infraestucturedomain.keycloak_adapter.services.JwtService;
import com.paymentchain.infraestucturedomain.keycloak_adapter.services.KeycloakRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
public class IndexController {

    private final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final JwtService jwtService;
    private final KeycloakRestService KeycloakRestService;

    public IndexController(JwtService jwtService
            ,KeycloakRestService keycloakRestService) {
        this.jwtService = jwtService;
        KeycloakRestService = keycloakRestService;
    }
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@RequestHeader( value ="Authorization") String authHeader) throws BusinessRuleException {
        DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer","").trim());

        //check JWT is valid
        try {
            Jwk jwk =  this.jwtService.getJwk();
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(),null);
            algorithm.verify(jwt);


        // check JWT is still active
            Date experyDate =  jwt.getExpiresAt();
            if(experyDate.before(new Date())){
                throw new TokenExpiredException("token is expired");
            }

        //all validation passed
            // check JWT role is correct
            System.out.println("-------------ENTRO-----------");
            logger.info("JWT Claims: {}", jwt.getClaims());
            Object rolesObject = jwt.getClaim("realm_access").asMap().get("roles");
            logger.info("JWT Claims: {}", jwt.getClaims());
            System.out.println("-------------ENTRO-----------");
            List<String> roles = new ArrayList<>();
            if (rolesObject instanceof List<?>) {
                for (Object role : (List<?>) rolesObject) {
                    if (role instanceof String) {
                        roles.add((String) role);
                    }
                }
            }
            Map<String,Integer> validation =  new HashMap<>();
            for (String role : roles){
                validation.put(role,role.length());
            }
            return ResponseEntity.ok(validation);

        } catch (Exception e) {
            logger.error("exception : {} " ,  e.getMessage());
            throw new BusinessRuleException("01", e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/valid")
    public ResponseEntity<?> valid(@RequestHeader("Authorization") String authHeader ) throws BusinessRuleException {
        try {
            this.KeycloakRestService.checkValidity(authHeader);
            return ResponseEntity.ok(Map.of("is_valid","true"));
        } catch (Exception e) {
            logger.error("token is not valid, exception : {} ", e.getMessage());
            throw new BusinessRuleException("is_valid","False", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password){
        String login = this.KeycloakRestService.login(username, password);
        return ResponseEntity.ok(login);
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(@RequestParam(value ="refreshToken") String refreshToken) throws BusinessRuleException {
        try {
            this.KeycloakRestService.logout(refreshToken);
            return ResponseEntity.ok(Map.of("logout","true"));
        } catch (Exception e) {
            logger.error("unable to logout, exception : {} ", e.getMessage());
            throw new BusinessRuleException("logout", "False",HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@RequestParam(value ="refreshToken") String refreshToken) throws BusinessRuleException {
        try {
            return ResponseEntity.ok(KeycloakRestService.refresh(refreshToken));
        } catch (Exception e) {
            logger.error("unable to refresh, exception : {} ", e.getMessage());
            throw new BusinessRuleException("refresh", "False",HttpStatus.FORBIDDEN);
        }
    }

}
