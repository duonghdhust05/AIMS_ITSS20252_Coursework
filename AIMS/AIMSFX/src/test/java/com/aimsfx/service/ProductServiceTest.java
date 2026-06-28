package com.aimsfx.service;

import com.aimsfx.dto.ProductDTO;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.model.Product;
import com.aimsfx.repository.ProductRepository;
import com.aimsfx.validator.CommonProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 * Maps to Phase 2 of Test Plan
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository mockRepository;

    @Mock
    private CommonProductValidator mockValidator;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(mockRepository, mockValidator);
    }

    @Test
    @DisplayName("[IT_PROD_003] Service Add Product")
    void testAddProduct_Success() throws Exception {
        // Arrange
        ProductDTO dto = ProductDTO.builder()
                .type("Book")
                .barcode("123456789")
                .title("Effective Java")
                .category("Programming")
                .originalPrice(100.0)
                .currentPrice(100.0)
                .weight(1.0)
                .dimensions("10x10x10")
                .stock(10)
                .attribute("author", "John Doe")
                .attribute("publisher", "Tech Press")
                .attribute("publicationDate", "2023-01-01")
                .attribute("pages", "300")
                .attribute("language", "English")
                .attribute("coverType", "Paperback")
                .attribute("genre", "Educational")
                .build();
        
        when(mockRepository.findCurrentByBarcode("123456789")).thenReturn(Optional.empty());

        Product mockProduct = mock(Product.class);
        when(mockProduct.getProductId()).thenReturn(1L);
        when(mockRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        Product result = productService.addProduct(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        verify(mockValidator).validateCommonFields(
            eq("Book"), eq("123456789"), eq("Effective Java"),
            eq(100.0), eq(100.0), eq("Programming"),
            eq(1.0), eq("10x10x10"), eq(10)
        );
        verify(mockRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("[IT_PROD_004] Service Update Non-existent")
    void testUpdateProduct_NonExistent() {
        // Arrange
        ProductDTO dto = ProductDTO.builder()
                .productId(999L)
                .type("Book")
                .title("Effective Java")
                .barcode("123456789")
                .originalPrice(100.0)
                .stock(10)
                .category("Programming")
                .attribute("author", "John Doe")
                .build();

        when(mockRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.updateProduct(dto);
        });

        assertEquals("Product not found with ID: 999", exception.getMessage());
        verify(mockRepository, never()).save(any(Product.class));
    }
}
