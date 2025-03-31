/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.entities;

import jakarta.persistence.*;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author sotobotero
 */
@Data
@Entity
@NoArgsConstructor
public class Customer {
   
   @GeneratedValue(strategy = GenerationType.AUTO)  
   @Id
   private long id;
   private String code;
   private String name;
   private String phone;
   private String iban;
   private String surname;
   private String address;
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<CustomerProduct> products;
   @Transient
   private List<?> transactions;

}
