package com.aimsfx.subsystem.vietqr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VietQRConfig {

    // Bank Config
    private String bankBin;
    private String bankCode;
    private String bankAccount;
    private String accountName;

    // Client API Config
    private String clientUsername;
    private String clientPassword;

    // API URL Config
    private String tokenUrl;
    private String qrUrl;
    private String simulateUrl;

    public VietQRConfig() {
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Error: application.properties not found!");
                return;
            }
            Properties prop = new Properties();
            prop.load(input);

            this.bankBin = prop.getProperty("vietqr.bank.bin");
            this.bankCode = prop.getProperty("vietqr.bank.code");
            this.bankAccount = prop.getProperty("vietqr.bank.account");
            this.accountName = prop.getProperty("vietqr.bank.name");
            this.clientUsername = prop.getProperty("vietqr.client.username");
            this.clientPassword = prop.getProperty("vietqr.client.password");
            this.tokenUrl = prop.getProperty("vietqr.api.token.url");
            this.qrUrl = prop.getProperty("vietqr.api.qr.url");
            this.simulateUrl = prop.getProperty("vietqr.api.simulate.url");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Getters
    public String getBankBin() {
        return bankBin;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public String getSimulateUrl() {
        return simulateUrl;
    }
}