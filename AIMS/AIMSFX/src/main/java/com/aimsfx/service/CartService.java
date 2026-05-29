package com.aimsfx.service;

import com.aimsfx.model.Cart;
import com.aimsfx.model.CartItem;
import com.aimsfx.model.Product;
import com.aimsfx.repository.ProductRepository;
import com.aimsfx.repository.DatabaseProductRepository;

import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * CartService - Business Logic Layer for Cart Operations
 */
public class CartService implements ICartService {
    
    private static final Logger LOGGER = Logger.getLogger(CartService.class.getName());
    @SuppressWarnings("deprecation")
	private static final Locale VN_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance(VN_LOCALE);
    
    private ProductRepository productRepository;
    
    public CartService() {
        LOGGER.info("CartService initialized");
    }
    
    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        LOGGER.info("CartService initialized with injected repository");
    }
    
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    private ProductRepository getProductRepository() {
        if (productRepository == null) {
            productRepository = new DatabaseProductRepository();
        }
        return productRepository;
    }
    
    @Override
    public double calculateSubtotal(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        double subtotal = 0;
        for (CartItem cartItem : items) {
            if (cartItem != null) {
                subtotal += cartItem.getLineTotal();
            }
        }
        return subtotal;
    }
    
    @Override
    public double calculateSubtotal(Cart cart) {
        if (cart == null) {
            return 0;
        }
        return calculateSubtotal(cart.getItems());
    }
    
    @Override
    public double calculateVAT(double subtotal) {
        return subtotal * 0.1;
    }
    
    @Override
    public float calculateTotalWeight(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0f;
        }
        
        float totalWeight = 0f;
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getProduct() == null) {
                continue;
            }
            
            Product product = cartItem.getProduct();
            if (product.getWeight() != null && product.getWeight() > 0) {
                totalWeight += product.getWeight() * cartItem.getQuantity();
            }
        }
        return totalWeight;
    }
    
    @Override
    public String checkProductAvailability(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return null;
        }
        
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getProduct() == null) {
                continue;
            }
            
            Product product = cartItem.getProduct();
            if (!product.isSufficient(cartItem.getQuantity())) {
                return String.format("Product '%s' is out of stock.\nAvailable: %d, Requested: %d",
                        product.getTitle(), product.getStock(), cartItem.getQuantity());
            }
        }
        return null;
    }
    
    @Override
    public List<Map<String, Object>> getInsufficientStockItems(Cart cart) {
        List<Map<String, Object>> insufficientItems = new ArrayList<>();
        
        if (cart == null || cart.getItems() == null) {
            return insufficientItems;
        }
        
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getProduct() == null) {
                continue;
            }
            
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();
            
            if (!product.checkAvailability(requestedQuantity)) {
                Map<String, Object> item = new HashMap<>();
                item.put("product", product);
                item.put("productId", product.getProductId());
                item.put("title", product.getTitle());
                item.put("requestedQty", requestedQuantity);
                item.put("availableQty", product.getStock());
                insufficientItems.add(item);
            }
        }
        
        return insufficientItems;
    }
    
    @Override
    public List<Map<String, Object>> checkCartStockWithDatabaseRefresh(Cart cart) {
        return checkCartStockWithDatabaseRefresh(cart, getProductRepository());
    }
    
    @Deprecated
    @Override
    public List<Map<String, Object>> checkCartStockWithDatabaseRefresh(
            Cart cart, ProductRepository productRepository) {
        
        List<Map<String, Object>> insufficientItems = new ArrayList<>();

        if (cart == null || cart.getItems() == null) {
            return insufficientItems;
        }

        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getProduct() == null) {
                continue;
            }

            Product product = cartItem.getProduct();
            Optional<Product> latestProductOpt = productRepository.findById(product.getProductId());

            if (!latestProductOpt.isPresent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("product", product);
                item.put("productId", product.getProductId());
                item.put("title", product.getTitle());
                item.put("requestedQty", cartItem.getQuantity());
                item.put("availableQty", 0);
                item.put("error", "Product no longer available");
                insufficientItems.add(item);
                continue;
            }

            Product latestProduct = latestProductOpt.get();
            int requestedQuantity = cartItem.getQuantity();
            int actualStock = latestProduct.getStock();

            if (!latestProduct.checkAvailability(requestedQuantity)) {
                Map<String, Object> item = new HashMap<>();
                item.put("product", product);
                item.put("productId", product.getProductId());
                item.put("title", product.getTitle());
                item.put("requestedQty", requestedQuantity);
                item.put("availableQty", actualStock);
                insufficientItems.add(item);
            }

            product.setStock(actualStock);
        }

        return insufficientItems;
    }
    
    @Override
    public void updateProductStockFromCart(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return;
        }
        
        ProductRepository productRepo = getProductRepository();
        
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getProduct() == null) {
                continue;
            }
            
            Product product = cartItem.getProduct();
            int newStock = product.getStock() - cartItem.getQuantity();
            
            product.setStock(newStock);
            
            boolean success = productRepo.updateStock(product.getProductId(), newStock);
            
            if (success) {
                LOGGER.info(String.format("Stock updated: %s - Sold: %d, Remaining: %d",
                        product.getTitle(), cartItem.getQuantity(), newStock));
            } else {
                LOGGER.warning(String.format("Failed to update stock for: %s (ID: %d)",
                        product.getTitle(), product.getProductId()));
            }
        }
    }
    
    @Override
    public String formatPrice(double price) {
        return PRICE_FORMATTER.format(price);
    }
    
    public int getItemCount(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0;
        }
        return cart.getItems().size();
    }
    
    public int getTotalQuantity(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0;
        }
        
        int total = 0;
        for (CartItem item : cart.getItems()) {
            if (item != null) {
                total += item.getQuantity();
            }
        }
        return total;
    }
    
    public boolean isCartEmpty(Cart cart) {
        return cart == null || cart.getItems() == null || cart.getItems().isEmpty();
    }
    
    public double calculateTotalWithVAT(Cart cart) {
        double subtotal = calculateSubtotal(cart);
        double vat = calculateVAT(subtotal);
        return subtotal + vat;
    }
}
