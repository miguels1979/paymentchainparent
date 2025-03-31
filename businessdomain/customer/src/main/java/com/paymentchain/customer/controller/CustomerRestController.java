/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.customer.controller;

import com.paymentchain.customer.business.transactions.BusinessTransactions;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.respository.CustomerRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;


/**
 *
 * @author sotobotero
 */
@RestController
//@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/customer")
public class CustomerRestController {
    
    private final CustomerRepository customerRepository;
    private final BusinessTransactions businessTransactions;
    private final Environment env;


    public CustomerRestController(CustomerRepository customerRepository,
                                  Environment env, BusinessTransactions businessTransactions) {
        this.customerRepository = customerRepository;
        this.businessTransactions = businessTransactions;
        this.env = env;
    }

    @GetMapping("/check")
    public String check(){
        return "Hello your property value is :" + env.getProperty("custom.activeprofileName");
    }

    @GetMapping
    public ResponseEntity<List<Customer>> list() {
        List<Customer> findAll = customerRepository.findAll();
        if (findAll.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(findAll);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable(name = "id") long id) {
         Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return new ResponseEntity<>(customer.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Customer input) {
         Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer newCustomer = optionalCustomer.get();
            newCustomer.setName(input.getName());
            newCustomer.setPhone(input.getPhone());
            newCustomer.setIban(input.getIban());
            newCustomer.setCode(input.getCode());
            newCustomer.setSurname(input.getSurname());
             Customer save = customerRepository.save(newCustomer);
          return new ResponseEntity<>(save, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping//Se encarga de guardar un nuevo cliente
    public ResponseEntity<?> post(@RequestBody Customer customerInput) throws BusinessRuleException, UnknownHostException {
        Customer savedCustomer = businessTransactions.validProductAndSaveCustomer(customerInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer); // 201 Created
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        Optional<Customer> customer = this.customerRepository.findById(id);
        if (customer.isPresent()) {
            this.customerRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404 Not Found si el ID no existe
    }

    @GetMapping("/full")
    public Customer getByCode(@RequestParam(name = "code") String code){
        return businessTransactions.getByCodeOfServiceExtern(code);
    }


}
