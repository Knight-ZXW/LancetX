package com.knightboost.lancet.internal.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class BitsetTest {
    private static final String ACCESS = "access$";

    @Test
    public void test(){
        Bitset bitset = new Bitset();
        bitset.setInitializer(new Bitset.Initializer() {
            @Override
            public void initialize(Bitset bitset) {
                int len = ACCESS.length();
                bitset.tryAdd("access$001", len);
                bitset.tryAdd("access$011", len);
                bitset.tryAdd("access$1111", len);
                bitset.tryAdd("access$11111", len);
            }
        });
        for (int i = 0; i < 11115; i++) {
            int index = bitset.consume();
        }
    }
}