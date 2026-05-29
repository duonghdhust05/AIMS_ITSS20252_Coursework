package com.aimsfx.model;

import com.aimsfx.repository.ProductRepository;
import com.aimsfx.repository.DatabaseProductRepository;

/**
 * CartManager - Singleton for managing shopping cart
 */
public class CartManager implements ICartManager {
    
    private Cart cart;
    private ProductRepository productRepository;
    
    private CartManager() {
        this.cart = new Cart(1, 1);
    }
    
    private static class Holder {
        private static final CartManager INSTANCE = new CartManager();
    }
    
    public static CartManager getInstance() {
        return Holder.INSTANCE;
    }
    
    @Override
    public Cart getCart() {
        return cart;
    }
    
    @Override
    public void setCart(Cart cart) {
        this.cart = cart;
    }
    
    @Override
    public boolean addProduct(Product product, int quantity) {
        return cart != null && product != null ? cart.addProduct(product, quantity) : false;
    }
    
    @Override
    public boolean addCartItem(CartItem cartItem) {
        return cart != null && cartItem != null ? cart.addCartItem(cartItem) : false;
    }
    
    @Override
    public boolean removeProduct(Long productId) {
        return cart != null && productId != null ? cart.removeProduct(productId) : false;
    }
    
    @Override
    public boolean updateQuantity(Long productId, int quantity) {
        return cart != null ? cart.updateQuantity(productId, quantity) : false;
    }
    
    @Override
    public void clearCart() {
        if (cart != null && cart.getItems() != null) {
            cart.getItems().clear();
        }
    }
    
    @Deprecated
    public DeliveryInfo getDeliveryInfo() {
        return SessionManager.getInstance().getDeliveryInfo();
    }
    
    @Deprecated
    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        SessionManager.getInstance().setDeliveryInfo(deliveryInfo);
    }
    
    @Deprecated
    public void clearDeliveryInfo() {
        SessionManager.getInstance().clearDeliveryInfo();
    }
    
    private ProductRepository getProductRepository() {
        if (productRepository == null) {
            productRepository = new DatabaseProductRepository();
        }
        return productRepository;
    }
    
    public void setProductRepository(ProductRepository repository) {
        this.productRepository = repository;
    }
    
    @Override
    public int refreshCartProducts() {
        if (cart == null) {
            return 0;
        }
        return cart.syncWithDatabase(getProductRepository());
    }
}
