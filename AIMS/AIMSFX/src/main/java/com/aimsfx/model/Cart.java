package com.aimsfx.model;

import com.aimsfx.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Cart model representing shopping cart
 */
public class Cart {
    private int cartId;
    private int userId;
    private List<CartItem> items;
    
    public Cart() {
        this.items = new ArrayList<>();
    }
    
    public Cart(int cartId, int userId) {
        this.cartId = cartId;
        this.userId = userId;
        this.items = new ArrayList<>();
    }
    
    public int getCartId() {
        return cartId;
    }
    
    public void setCartId(int cartId) {
        this.cartId = cartId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    public boolean addProduct(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }
        
        for (CartItem item : items) {
            if (item.getBarcode() != null && item.getBarcode().equals(product.getBarcode())) {
                item.setQuantity(item.getQuantity() + quantity);
                return true;
            }
        }
        
        items.add(new CartItem(product, quantity));
        return true;
    }
    
    public boolean addCartItem(CartItem cartItem) {
        if (cartItem == null || cartItem.getProduct() == null) {
            return false;
        }
        
        for (CartItem item : items) {
            if (item.getBarcode() != null && item.getBarcode().equals(cartItem.getBarcode())) {
                item.setQuantity(item.getQuantity() + cartItem.getQuantity());
                return true;
            }
        }
        
        items.add(cartItem);
        return true;
    }
    
    public boolean removeProduct(Long productId) {
        return items.removeIf(item -> item.getProductId() != null && item.getProductId().equals(productId));
    }
    
    public boolean removeProductByBarcode(String barcode) {
        if (barcode == null) return false;
        return items.removeIf(item -> barcode.equals(item.getBarcode()));
    }
    
    public boolean removeProduct(String productId) {
        try {
            Long id = Long.parseLong(productId);
            return removeProduct(id);
        } catch (NumberFormatException e) {
            return removeProductByBarcode(productId);
        }
    }
    
    public boolean updateQuantity(Long productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId() != null && item.getProductId().equals(productId)) {
                if (quantity <= 0) {
                    return removeProduct(productId);
                }
                item.setQuantity(quantity);
                return true;
            }
        }
        return false;
    }
    
    public boolean updateQuantityByBarcode(String barcode, int quantity) {
        if (barcode == null) return false;
        for (CartItem item : items) {
            if (barcode.equals(item.getBarcode())) {
                if (quantity <= 0) {
                    return removeProductByBarcode(barcode);
                }
                item.setQuantity(quantity);
                return true;
            }
        }
        return false;
    }
    
    public boolean updateQuantity(String productId, int quantity) {
        try {
            Long id = Long.parseLong(productId);
            return updateQuantity(id, quantity);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public CartItem getCartItem(Long productId) {
        for (CartItem item : items) {
            if (item.getProductId() != null && item.getProductId().equals(productId)) {
                return item;
            }
        }
        return null;
    }
    
    public CartItem getCartItemByBarcode(String barcode) {
        if (barcode == null) return null;
        for (CartItem item : items) {
            if (barcode.equals(item.getBarcode())) {
                return item;
            }
        }
        return null;
    }
    
    public Product getProduct(Long productId) {
        CartItem item = getCartItem(productId);
        return item != null ? item.getProduct() : null;
    }
    
    public Product getProductByBarcode(String barcode) {
        CartItem item = getCartItemByBarcode(barcode);
        return item != null ? item.getProduct() : null;
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
    
    public double calculateSubtotal() {
        return items.stream()
            .mapToDouble(CartItem::getLineTotal)
            .sum();
    }
    
    public void clear() {
        items.clear();
    }
    
    public boolean checkAvailability() {
        for (CartItem item : items) {
            if (!item.isAvailable()) {
                return false;
            }
        }
        return true;
    }

    public int syncWithDatabase(ProductRepository productRepository) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int updatedCount = 0;
        Iterator<CartItem> iterator = items.iterator();
        
        while (iterator.hasNext()) {
            CartItem cartItem = iterator.next();
            Product cartProduct = cartItem.getProduct();
            
            if (cartProduct == null || cartProduct.getBarcode() == null) {
                continue;
            }
            
            Optional<Product> latestProductOpt = productRepository.findCurrentByBarcode(cartProduct.getBarcode());
            
            if (latestProductOpt.isPresent()) {
                Product latestProduct = latestProductOpt.get();
                
                boolean changed = false;
                
                if (!latestProduct.getProductId().equals(cartProduct.getProductId())) {
                    changed = true;
                    System.out.println("📦 Cart sync: Product '" + latestProduct.getTitle() + 
                            "' ID changed: " + cartProduct.getProductId() + " -> " + latestProduct.getProductId());
                }
                
                if (!latestProduct.getCurrentPrice().equals(cartProduct.getCurrentPrice())) {
                    changed = true;
                    System.out.println("💰 Cart sync: Product '" + latestProduct.getTitle() + 
                            "' price changed: " + cartProduct.getCurrentPrice() + " -> " + latestProduct.getCurrentPrice());
                }
                
                if (!latestProduct.getStock().equals(cartProduct.getStock())) {
                    changed = true;
                }
                
                if (latestProduct.getTitle() != null && !latestProduct.getTitle().equals(cartProduct.getTitle())) {
                    changed = true;
                }
                
                if (changed) {
                    cartItem.setProduct(latestProduct.copy());
                    updatedCount++;
                }
            } else {
                System.out.println("🗑️ Cart sync: Product '" + cartProduct.getTitle() + 
                        "' (barcode: " + cartProduct.getBarcode() + ") no longer exists - removed from cart");
                iterator.remove();
                updatedCount++;
            }
        }
        
        return updatedCount;
    }
    
    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", items=" + items.size() +
                ", subtotal=" + calculateSubtotal() +
                '}';
    }
}
