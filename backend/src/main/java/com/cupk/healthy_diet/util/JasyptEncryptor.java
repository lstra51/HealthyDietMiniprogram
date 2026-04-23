package com.cupk.healthy_diet.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptEncryptor {
    private static final String ALGORITHM = System.getenv().getOrDefault("JASYPT_ENCRYPTOR_ALGORITHM", "PBEWithMD5AndDES");
    private static final String PASSWORD = System.getenv().getOrDefault("JASYPT_ENCRYPTOR_PASSWORD", "change-me-local-only");

    public static String encrypt(String plainText) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(ALGORITHM);
        encryptor.setPassword(PASSWORD);
        return encryptor.encrypt(plainText);
    }

    public static String decrypt(String encryptedText) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(ALGORITHM);
        encryptor.setPassword(PASSWORD);
        return encryptor.decrypt(encryptedText);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String plainText = args[0];
            System.out.println("原文: " + plainText);
            System.out.println("加密: ENC(" + encrypt(plainText) + ")");
        } else {
            String[] texts = {
                "dbprovider.ap-southeast-1.clawcloudrun.com",
                "35993",
                "root",
                "kjf8wx9g"
            };

            System.out.println("=== 数据库配置加密工具 ===");
            System.out.println("使用方法: java JasyptEncryptor <要加密的文本>");
            System.out.println();
            System.out.println("=== 预生成的加密值 ===");
            for (String text : texts) {
                System.out.println("原文: " + text);
                System.out.println("加密: ENC(" + encrypt(text) + ")");
                System.out.println();
            }
        }
    }
}
