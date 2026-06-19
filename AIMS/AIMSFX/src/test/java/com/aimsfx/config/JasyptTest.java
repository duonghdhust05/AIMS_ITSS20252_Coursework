package com.aimsfx.config;

import org.junit.jupiter.api.Test;

public class JasyptTest {

    @Test
    public void generateEncryptedPasswords() {
        System.out.println("spring.datasource.password=" + JasyptConfig.encryptProperty("Aims20252!@"));
        System.out.println("vietqr.client.password=" + JasyptConfig.encryptProperty("Y3VzdG9tZXItYWltc2dyb3VwMS1jM2VjMjY1OTM="));
        System.out.println("app.jwt.secret=" + JasyptConfig.encryptProperty("2989b06328f7df8020dd3e4659634821"));
        System.out.println("paypal.client.secret=" + JasyptConfig.encryptProperty("EB0xkzdFNAsnbzjQoDeA82zP_p0B8FF-aracCfNlc6zr79kTqqpOUUlG0sPXOy0rcezgTrOsdjPcl6Rd"));
        System.out.println("email.password=" + JasyptConfig.encryptProperty("sfnqpfvxmfnzkvap"));
    }

    @Test
    public void testDecryption() {
        String encryptedDbPass = "ENC(0h2O+7/AaXFT7ZZR6uD51SGO+hoLDWfv)";
        String decrypted = JasyptConfig.decryptProperty(encryptedDbPass);
        System.out.println("Decrypted DB Password: " + decrypted);
    }
}
