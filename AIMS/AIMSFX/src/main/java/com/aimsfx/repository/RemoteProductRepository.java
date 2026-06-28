package com.aimsfx.repository;

import com.aimsfx.model.OrderItem;
import com.aimsfx.model.Product;
import com.aimsfx.model.StockChangeLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Strangler Fig Pattern: Remote Client
 * 
 * This repository replaces the direct Database connection.
 * It fetches Products from our new Spring Boot Microservice running on port 8081.
 */
public class RemoteProductRepository implements ProductRepository {

    private final String BASE_URL = "http://localhost:8081/api/v1/products";
    private final HttpClient client;
    private final Gson gson;

    public RemoteProductRepository() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public Product save(Product product) {
        try {
            String json = gson.toJson(product);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return gson.fromJson(response.body(), Product.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Optional<Product> findById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(gson.fromJson(response.body(), Product.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findCurrentByBarcode(String barcode) {
        // Find all and filter is a fallback. In a real microservice, we'd add an endpoint for findByBarcode.
        List<Product> all = findAll();
        return all.stream().filter(p -> barcode.equals(p.getBarcode())).findFirst();
    }

    @Override
    public List<Product> findAll() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<Product>>() {}.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existsById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id + "/exists"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Product> findHistoryByProductId(Long productId) {
        throw new UnsupportedOperationException("History API not implemented in Microservice yet.");
    }

    @Override
    public boolean updateStock(Long productId, Integer newStock) {
        throw new UnsupportedOperationException("Absolute stock update API not implemented. Use deduct/restore.");
    }

    @Override
    public boolean deductStockAtomically(Long productId, int quantity) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("productId", productId);
            req.put("quantity", quantity);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/deduct-stock"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(req)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean restoreStock(Long productId, int quantity) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("productId", productId);
            req.put("quantity", quantity);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/restore-stock"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(req)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deductStockForOrder(List<OrderItem> items) {
        try {
            List<Map<String, Object>> payload = new ArrayList<>();
            for (OrderItem item : items) {
                Map<String, Object> req = new HashMap<>();
                req.put("productId", item.getProduct().getProductId());
                req.put("quantity", item.getQuantity());
                payload.add(req);
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/batch-deduct"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean restoreStockForOrder(List<OrderItem> items) {
        try {
            List<Map<String, Object>> payload = new ArrayList<>();
            for (OrderItem item : items) {
                Map<String, Object> req = new HashMap<>();
                req.put("productId", item.getProduct().getProductId());
                req.put("quantity", item.getQuantity());
                payload.add(req);
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/batch-restore"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Boolean.parseBoolean(response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateStock(String barcode, Integer newStock, String reason) {
        throw new UnsupportedOperationException("Direct stock override not implemented via Microservice yet.");
    }

    @Override
    public List<StockChangeLog> getStockChangeHistory(String barcode) {
        throw new UnsupportedOperationException("History API not implemented in Microservice yet.");
    }

    @Override
    public Map<String, Object> getProductDetails(Long productId) {
        throw new UnsupportedOperationException("Details API not implemented in Microservice yet.");
    }

    @Override
    public List<Product> searchProducts(String query, Double minPrice, Double maxPrice, int limit) {
        List<Product> all = findAll();
        // Naive fallback search
        List<Product> result = new ArrayList<>();
        if (query == null) query = "";
        String lowerQuery = query.toLowerCase();
        for (Product p : all) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains(lowerQuery)) {
                result.add(p);
                if (result.size() >= limit && limit > 0) break;
            }
        }
        return result;
    }

    @Override
    public List<Product> getRandomProducts(int limit) {
        List<Product> all = findAll();
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }
}
