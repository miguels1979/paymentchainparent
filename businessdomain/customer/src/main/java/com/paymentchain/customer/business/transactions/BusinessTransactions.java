package com.paymentchain.customer.business.transactions;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.respository.CustomerRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
public class BusinessTransactions {

    private final WebClient.Builder webClientBuilder;
    private final CustomerRepository customerRepository;



    public BusinessTransactions(WebClient.Builder webClientBuilder, CustomerRepository customerRepository){
          this.webClientBuilder = webClientBuilder;
          this.customerRepository = customerRepository;
    }



    public Customer validProductAndSaveCustomer(Customer customerInput) throws BusinessRuleException, UnknownHostException {
        //validamos que traiga productos el customer
        List<CustomerProduct> products = customerInput.getProducts();
        System.out.println("---PRODUCTS: ----" + products);
        if(products != null && !products.isEmpty()){
            for(CustomerProduct product: products){
                String productName = getProductNameOfServiceExtern(product.getProductId());
                if(productName.isBlank()){
                    throw new BusinessRuleException("1025","Error de validacion, producto con id " + product.getProductId() + " no existe", HttpStatus.PRECONDITION_FAILED);
                }else{
                    product.setCustomer(customerInput);
                }
            }
        }
        return this.customerRepository.save(customerInput);
    }

    //si quieres eliminar un elemento mientras iteras.
    public Customer validProductAndSaveCustomer2(Customer input) throws BusinessRuleException,UnknownHostException {
        if (input.getProducts() != null) {
            for (Iterator<CustomerProduct> it = input.getProducts().iterator(); it.hasNext();) {
                CustomerProduct dto = it.next();
                String productName = getProductNameOfServiceExtern(dto.getProductId());
                if(productName.isBlank()){
                    throw new BusinessRuleException("1025", "Error validacion, producto con id "+dto.getProductId()+ " no existe", HttpStatus.PRECONDITION_FAILED);
                }else{
                    dto.setCustomer(input);
                }
            }
        }
        return this.customerRepository.save(input);
    }


    /**
     * Call Product Microservice , find a product by Id and return it name
     *
     * @param id of product to find
     * @return name of product if it was finde
     */
    public String getProductNameOfServiceExtern(long id) throws UnknownHostException {
        String name = "";
        try {
            WebClient webClient = this.webClientBuilder.build();
            JsonNode block = webClient.method(HttpMethod.GET)
                    .uri("http://BUSINESSDOMAIN-PRODUCT/product/" + id)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if(block == null){
                throw new RuntimeException("Response is null for ID: " + id);
            }
            name = block.get("name").asText();
        }catch(WebClientResponseException e){
            if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
                return "";
            }else{
                throw new UnknownHostException(e.getMessage());
            }
        }
        return name;
    }

    /**
     * Call Transaction Microservice and Find all transaction that belong to the
     * account give
     *
     * @param iban account number of the customer
     * @return All transaction that belong this account
     */
    public List<?> getTransactions(String iban){
        WebClient webClient = this.webClientBuilder.build();
        Optional<List<?>> transactionalOptional = Optional.ofNullable(webClient.method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8082)
                        .path("/transaction/customer/transactions")
                        .queryParam("ibanAccount",iban)
                        .build())
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .block());
        return transactionalOptional.orElse(Collections.emptyList());
    }


    public Customer getByCodeOfServiceExtern(String code) {
        Customer customer = customerRepository.findByCode(code);
        if(customer == null){
            throw new RuntimeException("Cliente no encontrado");
        }
        System.out.println("CUSTOMER------" + customer);
        List<CustomerProduct> products = customer.getProducts();
        //for each product find it name
        products.forEach(product->{
            String productName = null;
            try {
                productName = getProductNameOfServiceExtern(product.getProductId());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            product.setProductName(productName);
        });
        //find all transactions that belong this account number
        /*List<?> transactions = this.getTransactions(customer.getIban());
        customer.setTransactions(transactions);*/
        return customer;
    }
}
