package com.paymentchain.transactions.controller;

import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
public class TransactionRestController {

    private final TransactionRepository transactionRepository;

    public TransactionRestController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping()
    public List<Transaction> list() {
        return this.transactionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable(name = "id") long id){
        Optional<Transaction> transactionOfDB = this.transactionRepository.findById(id);
        if(transactionOfDB.isPresent()){
            return new ResponseEntity<>(transactionOfDB.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/customer/transactions")
    public List<Transaction> get(@RequestParam(name = "ibanAccount") String ibanAccount) {
        return transactionRepository.findByIbanAccount(ibanAccount);
    }

    @PutMapping("{id}")//Debera ser Patch porque permite que un atributo pueda ser modificado
    public ResponseEntity<?> put (@PathVariable(name = "id") long id, @RequestBody Transaction transactionInput){
        Optional<Transaction> transactionOfDB = this.transactionRepository.findById(id);

        if(transactionOfDB.isPresent()){
            Transaction transactionToUpdate =  transactionOfDB.get();
            if(transactionInput.getAmount() != 0) {
                transactionToUpdate.setAmount(transactionInput.getAmount());
            }
            if(StringUtils.hasText(transactionInput.getChannel())){
                transactionToUpdate.setChannel(transactionInput.getChannel());
            }
            if(transactionInput.getDate() != null  ) {
                transactionToUpdate.setDate(transactionInput.getDate());
            }
            if(StringUtils.hasText(transactionInput.getDescription())){
                transactionToUpdate.setDescription(transactionInput.getDescription());
            }
            if(transactionInput.getFee() != 0){
                transactionToUpdate.setFee(transactionInput.getFee());
            }
            if(StringUtils.hasText(transactionInput.getIbanAccount())) {
                transactionToUpdate.setIbanAccount(transactionInput.getIbanAccount());
            }
            if(StringUtils.hasText(transactionInput.getDescription())){
                transactionToUpdate.setReference(transactionInput.getDescription());
            }
            if(StringUtils.hasText(transactionInput.getStatus())){
                transactionToUpdate.setStatus(transactionInput.getStatus());
            }

            Transaction transactionUpdated = this.transactionRepository.save(transactionToUpdate);
            return new ResponseEntity<>(transactionUpdated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        }

        @PostMapping
        public ResponseEntity<?> post(@RequestBody Transaction transactionInput){
            Transaction transactionSaved = this.transactionRepository.save(transactionInput);
            return ResponseEntity.ok(transactionSaved);
        }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id){
        Optional<Transaction> transactionForDelete = this.transactionRepository.findById(id);
        if(transactionForDelete.isPresent()){
            this.transactionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }



    }

