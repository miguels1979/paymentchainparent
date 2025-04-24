package com.paymentchain.customer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@ToString(exclude = "customer")  // ⬅️ evita imprimir el Customer en el toString
public class CustomerProduct {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private long productId;
    @Transient
    private String productName;

    @JsonIgnore//it is necesary for avoid infinite recursion
    @ManyToOne(fetch = FetchType.LAZY,targetEntity = Customer.class)
    @JoinColumn(name = "customerId", nullable = true)
    private Customer customer;
}
