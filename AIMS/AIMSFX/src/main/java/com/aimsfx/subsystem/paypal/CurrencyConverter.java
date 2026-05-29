package com.aimsfx.subsystem.paypal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CurrencyConverter Class
 * Purpose: Convert VND to USD using live exchange rate API
 * 
 * COHESION: HIGH - Functional Cohesion
 * - Single method convertVndToUsd() performs one well-defined computation
 * - FALLBACK_RATE constant supports the same single task
 * - All code elements contribute to currency conversion goal
 * 
 * COUPLING ANALYSIS:
 * 1. Data Coupling with PayPalSubsystem (consumer)
 * - Uses: convertVndToUsd(vndAmount) returns double
 * - Type: Data coupling - only primitive types passed (double)
 * - Justification: PayPalSubsystem needs USD amount for PayPal API
 */
public class CurrencyConverter {

    private static final double FALLBACK_RATE = 25000.0;

    @SuppressWarnings("deprecation")
	public double convertVndToUsd(double vndAmount) {
        try {
            URL url = new URL("https://open.er-api.com/v6/latest/VND");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonText = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    jsonText.append(line);
                reader.close();

                JsonObject json = JsonParser.parseString(jsonText.toString()).getAsJsonObject();
                return vndAmount * json.get("rates").getAsJsonObject().get("USD").getAsDouble();
            }
        } catch (Exception e) {
            System.err.println("Exchange Rate API Failed: " + e.getMessage());
        }
        return vndAmount / FALLBACK_RATE;
    }
}