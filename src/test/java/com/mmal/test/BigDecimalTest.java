package com.mmal.test;

import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalTest {

    @Test
    public void test1(){
        System.out.println(0.05+0.01);
//        System.out.println(0.05-0.01);
        System.out.println(1.0-0.01);
        System.out.println(3.021*100);
        System.out.println(123.3/100);
    }

    @Test
    public void test2(){
        BigDecimal bigDecimal1 = new BigDecimal(0.05);
        BigDecimal bigDecima2 = new BigDecimal(0.01);
        System.out.println(bigDecimal1.add(bigDecima2));
    }

    @Test
    public void test3(){
        BigDecimal bigDecimal1 = new BigDecimal("0.05");
        BigDecimal bigDecima2 = new BigDecimal("0.01");
        System.out.println(bigDecimal1.add(bigDecima2));
    }
}
