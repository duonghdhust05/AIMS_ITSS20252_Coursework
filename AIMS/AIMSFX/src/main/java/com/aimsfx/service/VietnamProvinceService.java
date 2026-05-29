package com.aimsfx.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import com.aimsfx.model.Province;
import com.aimsfx.model.Ward;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to fetch Vietnam administrative divisions from API v2
 * (After province merger in July 2025)
 */
public class VietnamProvinceService {
    
    private static final String API_BASE_URL = "https://provinces.open-api.vn/api/v2";
    private static VietnamProvinceService instance;
    private List<Province> cachedProvinces;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private VietnamProvinceService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public static synchronized VietnamProvinceService getInstance() {
        if (instance == null) {
            instance = new VietnamProvinceService();
        }
        return instance;
    }
    
    /**
     * Fetch all provinces WITHOUT wards (depth=1) for better performance
     * Wards will be loaded lazily when user selects a province
     * API: GET /?depth=1
     */
    public List<Province> getAllProvinces() throws IOException, InterruptedException {
        if (cachedProvinces != null && !cachedProvinces.isEmpty()) {
            return cachedProvinces;
        }
        
        String url = API_BASE_URL + "/?depth=1";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Province[] provinces = objectMapper.readValue(response.body(), Province[].class);
            cachedProvinces = List.of(provinces);
            return cachedProvinces;
        } else {
            throw new IOException("Failed to fetch provinces. Status code: " + response.statusCode());
        }
    }
    
    /**
     * Get a specific province by code with its wards
     * API: GET /p/{code}?depth=2
     */
    public Province getProvinceByCode(Integer code) throws IOException, InterruptedException {
        String url = API_BASE_URL + "/p/" + code + "?depth=2";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Province.class);
        } else {
            throw new IOException("Failed to fetch province. Status code: " + response.statusCode());
        }
    }
    
    /**
     * Get wards by province code
     * API: GET /w/?province={code}
     */
    public List<Ward> getWardsByProvince(Integer provinceCode) throws IOException, InterruptedException {
        String url = API_BASE_URL + "/w/?province=" + provinceCode;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Ward[] wards = objectMapper.readValue(response.body(), Ward[].class);
            return List.of(wards);
        } else {
            throw new IOException("Failed to fetch wards. Status code: " + response.statusCode());
        }
    }
    
    /**
     * Clear cache (useful if data needs to be refreshed)
     */
    public void clearCache() {
        cachedProvinces = null;
    }
    
    /**
     * Load wards for a specific province (lazy loading)
     * Called only when user selects a province
     */
    public List<Ward> loadWardsForProvince(Integer provinceCode) throws IOException, InterruptedException {
        // Check if province already has wards cached
        if (cachedProvinces != null) {
            for (Province p : cachedProvinces) {
                if (p.getCode().equals(provinceCode) && p.getWards() != null && !p.getWards().isEmpty()) {
                    return p.getWards();
                }
            }
        }
        
        // Fetch wards from API
        List<Ward> wards = getWardsByProvince(provinceCode);
        
        // Update cache
        if (cachedProvinces != null) {
            for (Province p : cachedProvinces) {
                if (p.getCode().equals(provinceCode)) {
                    p.setWards(wards);
                    break;
                }
            }
        }
        
        return wards;
    }
}
