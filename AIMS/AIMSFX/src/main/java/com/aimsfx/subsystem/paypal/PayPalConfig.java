package com.aimsfx.subsystem.paypal;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel; // Import 1

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.aimsfx.config.JasyptConfig;

/**
 * PayPalConfig Class
 * Purpose: Load PayPal credentials and create configured SDK client
 * 
 * COHESION: HIGH - Functional Cohesion
 * - All attributes (clientId, clientSecret) work toward one goal: client
 * configuration
 * - Constructor loads credentials, paypalClient() creates client using those
 * credentials
 * - Single responsibility: PayPal SDK client configuration
 * 
 * COUPLING ANALYSIS:
 * 1. Content Coupling with PayPal SDK (PaypalServerSdkClient,
 * ClientCredentialsAuthModel)
 * - Uses: PaypalServerSdkClient.Builder(), ClientCredentialsAuthModel.Builder()
 * - Type: Content coupling - directly accesses SDK's internal builder patterns
 * - Risk: If PayPal SDK changes its API, this class must change
 * - Justification: Unavoidable when configuring external SDK
 * 
 * 2. Data Coupling with PlaceOrderScreen (consumer)
 * - Uses: paypalClient() to get configured client
 * - Type: Stamp coupling - passes entire PaypalServerSdkClient object
 * - Justification: PayPalSubsystem needs full SDK client for API operations
 * 
 * 3. External Coupling with application.properties
 * - Type: External coupling - depends on configuration file
 * - Justification: Credentials should not be hardcoded
 */
public class PayPalConfig {

    private String clientId;
    private String clientSecret;

    public PayPalConfig() {
        // MANUAL PROPERTY LOADING (Your logic is perfect here)
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            // Read keys manually
            this.clientId = prop.getProperty("paypal.client.id");
            this.clientSecret = JasyptConfig.decryptProperty(prop.getProperty("paypal.client.secret"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public PaypalServerSdkClient paypalClient() {
        // Check to ensure they loaded
        if (clientId == null || clientSecret == null) {
            throw new RuntimeException("PayPal Credentials failed to load from application.properties");
        }

        return new PaypalServerSdkClient.Builder()
                .environment(Environment.SANDBOX)
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(clientId, clientSecret).build())
                .loggingConfig(builder -> builder
                        .level(org.slf4j.event.Level.DEBUG)
                        .requestConfig(log -> log.body(true))
                        .responseConfig(log -> log.headers(true)))
                .build();
    }
}