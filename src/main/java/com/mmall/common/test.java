package com.mmall.common;
import org.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;

public class test {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        System.out.println(String.valueOf(random.nextInt(1000)));

        for (int i = 0; i < 1000 ; i++) {
            byte[] bytes = new byte[10];
            (new SecureRandom()).nextBytes(bytes);
            String abc= Base64.encodeBase64URLSafeString(bytes);
            System.out.print(abc);
            System.out.print(abc.length());
            System.out.println();
        }

    }
}
