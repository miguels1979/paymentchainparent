package com.paymentchain.customer.business.transactions;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;

public class Song {

    int count;

    public void setCount() {
        count = 4;
    }

    public static void main(String[] args) {
        Song s = new Song();
        System.out.println(s.count);

    }
}