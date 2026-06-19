package com.aimsfx.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig {

    public static final String JASYPT_KEY = "AimsSecretKey20252!@";
    private static StandardPBEStringEncryptor encryptor;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        if (encryptor == null) {
            encryptor = new StandardPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPassword(JASYPT_KEY);
            config.setAlgorithm("PBEWithMD5AndDES");
            config.setKeyObtentionIterations("1000");
            config.setPoolSize("1");
            config.setProviderName("SunJCE");
            config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
            config.setStringOutputType("base64");
            encryptor.setConfig(config);
        }
        return encryptor;
    }

    public static String decryptProperty(String encryptedValue) {
        if (encryptedValue != null && encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
            if (encryptor == null) {
                new JasyptConfig().stringEncryptor();
            }
            String innerValue = encryptedValue.substring(4, encryptedValue.length() - 1);
            return encryptor.decrypt(innerValue);
        }
        return encryptedValue;
    }

    // Helper method to encrypt passwords, will be removed later or kept for utilities
    public static String encryptProperty(String rawValue) {
        if (encryptor == null) {
            new JasyptConfig().stringEncryptor();
        }
        return "ENC(" + encryptor.encrypt(rawValue) + ")";
    }
}
