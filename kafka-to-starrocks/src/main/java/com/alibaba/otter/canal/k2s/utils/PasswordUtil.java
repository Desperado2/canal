package com.alibaba.otter.canal.k2s.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class PasswordUtil {


   public static String encrypt(String password){
       try {
           return SecurityUtil.scrambleGenPass(password.getBytes());
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
       }
       return null;
   }

}
