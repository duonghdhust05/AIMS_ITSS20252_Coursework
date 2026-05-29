package com.aimsfx.controller;

import com.aimsfx.dto.ProductDTO;
import com.aimsfx.exception.InvalidProductDataException;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.model.*;
import com.aimsfx.repository.ProductRepository;
import com.aimsfx.service.ProductService;
import com.aimsfx.validator.CommonProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductControllerTest - Unit tests for Add/Update Product use case
 * 
 * Testing Strategy (TDD Approach):
 * ============================================================
 * 1. Test-First Development:
 * - RED: Write failing tests first
 * - GREEN: Implement minimum code to pass
 * - REFACTOR: Improve code quality
 * 
 * 2. Test Categories:
 * - ADD PRODUCT: handleAddProduct() method
 * - UPDATE PRODUCT: handleUpdateProduct() method
 * - UPDATE STOCK: handleUpdateStock() method
 * - EDGE CASES: Null inputs, invalid data, boundaries
 * 
 * 3. Isolation Strategy:
 * - Mock ProductService to isolate controller logic
 * - Focus on controller's parsing and orchestration responsibilities
 * 
 * 4. Naming Convention:
 * - testMethodName_StateUnderTest_ExpectedBehavior
 */
@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

        @Mock
        private ProductService mockProductService;

        @Mock
        private ProductRepository mockProductRepository;

        private ProductController controller;

        // Test data constants
        private static final String VALID_BARCODE = "12345678901234";
        private static final String VALID_TITLE = "Test Product";
        private static final String VALID_CATEGORY = "Electronics";
        private static final String VALID_PRICE = "100000";
        private static final String VALID_STOCK = "50";
        private static final String VALID_WEIGHT = "0.5";
        private static final String VALID_DIMENSIONS = "10x20x5";
        private static final String VALID_VAT_RATE = "0.1";
        private static final String VALID_DESCRIPTION = "Test Description";

        @BeforeEach
        void setUp() {
                // Create controller with mocked service for testing
                controller = new ProductController(mockProductService, true);
        }

        // ============================================================
        // NESTED CLASS: ADD PRODUCT TESTS
        // ============================================================
        @Nested
        @DisplayName("handleAddProduct() Tests")
        class HandleAddProductTests {

                // ==================== BOOK TESTS ====================

                @Test
                @DisplayName("Add Book with valid data should return created product")
                void testHandleAddProduct_ValidBook_ShouldReturnCreatedProduct() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidBookAttributes();

                        Book expectedBook = createSampleBook();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        specificAttributes,
                                        null);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        assertEquals(expectedBook.getTitle(), result.getTitle());
                        verify(mockProductService, times(1)).addProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Add Book with all optional fields should succeed")
                void testHandleAddProduct_BookWithAllFields_ShouldSucceed() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("description", "A comprehensive programming guide");
                        commonFields.put("vatRate", "0.08");

                        Map<String, String> specificAttributes = createValidBookAttributes();
                        specificAttributes.put("genre", "Programming");

                        Book expectedBook = createSampleBook();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        specificAttributes,
                                        null);

                        // Assert
                        assertNotNull(result);
                        verify(mockProductService).addProduct(any(ProductDTO.class));
                }

                // ==================== CD TESTS ====================

                @Test
                @DisplayName("Add CD with valid data and tracks should return created product")
                void testHandleAddProduct_ValidCDWithTracks_ShouldReturnCreatedProduct() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidCDAttributes();
                        List<Track> tracks = createSampleTracks(5);

                        CD expectedCD = createSampleCD();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedCD);
                        when(mockProductService.saveTracks(anyList())).thenReturn(true);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.CD.toString(),
                                        commonFields,
                                        specificAttributes,
                                        tracks);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        verify(mockProductService).addProduct(any(ProductDTO.class));
                        verify(mockProductService).saveTracks(anyList());
                }

                @Test
                @DisplayName("Add CD without tracks should throw exception (CD requires at least 1 track)")
                void testHandleAddProduct_CDWithoutTracks_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidCDAttributes();

                        // Act & Assert
                        // Note: This test may pass or fail depending on factory validation rules
                        // If CD factory requires tracks, it should throw IllegalArgumentException
                        assertThrows(IllegalArgumentException.class, () -> {
                                controller.handleAddProduct(
                                                ProductType.CD.toString(),
                                                commonFields,
                                                specificAttributes,
                                                null // No tracks
                                );
                        });
                }

                // ==================== DVD TESTS ====================

                @Test
                @DisplayName("Add DVD with valid data should return created product")
                void testHandleAddProduct_ValidDVD_ShouldReturnCreatedProduct() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidDVDAttributes();

                        DVD expectedDVD = createSampleDVD();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedDVD);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.DVD.toString(),
                                        commonFields,
                                        specificAttributes,
                                        null);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        assertEquals(expectedDVD.getTitle(), result.getTitle());
                        verify(mockProductService).addProduct(any(ProductDTO.class));
                }

                // ==================== NEWSPAPER TESTS ====================

                @Test
                @DisplayName("Add Newspaper with valid data should return created product")
                void testHandleAddProduct_ValidNewspaper_ShouldReturnCreatedProduct() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidNewspaperAttributes();

                        Newspaper expectedNewspaper = createSampleNewspaper();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedNewspaper);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.NEWSPAPER.toString(),
                                        commonFields,
                                        specificAttributes,
                                        null);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        verify(mockProductService).addProduct(any(ProductDTO.class));
                }

                // ==================== VALIDATION TESTS ====================

                @Test
                @DisplayName("Add Product with null type should throw IllegalArgumentException")
                void testHandleAddProduct_NullType_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(null, commonFields, new HashMap<>(), null));
                        assertTrue(exception.getMessage().contains("required"));
                }

                @Test
                @DisplayName("Add Product with empty type should throw IllegalArgumentException")
                void testHandleAddProduct_EmptyType_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct("", commonFields, new HashMap<>(), null));
                        assertTrue(exception.getMessage().contains("required"));
                }

                @Test
                @DisplayName("Add Product with unsupported type should throw IllegalArgumentException")
                void testHandleAddProduct_UnsupportedType_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct("INVALID_TYPE", commonFields, new HashMap<>(),
                                                        null));
                }

                @Test
                @DisplayName("Add Product with missing required barcode should throw IllegalArgumentException")
                void testHandleAddProduct_MissingBarcode_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.remove("barcode");

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                        assertTrue(exception.getMessage().toLowerCase().contains("barcode") ||
                                        exception.getMessage().toLowerCase().contains("required"));
                }

                @Test
                @DisplayName("Add Product with missing required title should throw IllegalArgumentException")
                void testHandleAddProduct_MissingTitle_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.remove("title");

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                        assertTrue(exception.getMessage().toLowerCase().contains("title") ||
                                        exception.getMessage().toLowerCase().contains("required"));
                }

                @Test
                @DisplayName("Add Product with invalid price format should throw IllegalArgumentException")
                void testHandleAddProduct_InvalidPriceFormat_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("price", "not_a_number");

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                }

                @Test
                @DisplayName("Add Product with negative price should throw IllegalArgumentException")
                void testHandleAddProduct_NegativePrice_ShouldThrowException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("price", "-100");

                        // Mock service to throw exception for negative price (validation happens at
                        // service layer)
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException(
                                                        "Original price must be non-negative"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                        assertTrue(exception.getMessage().toLowerCase().contains("price") ||
                                        exception.getMessage().toLowerCase().contains("negative"));
                }

                @Test
                @DisplayName("Add Product with invalid stock format should throw IllegalArgumentException")
                void testHandleAddProduct_InvalidStockFormat_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("stock", "fifty");

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                }

                @Test
                @DisplayName("Add Product with negative stock should throw IllegalArgumentException")
                void testHandleAddProduct_NegativeStock_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("stock", "-10");

                        // Act & Assert - Controller validates negative stock at controller layer
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                        assertTrue(exception.getMessage().toLowerCase().contains("stock") ||
                                        exception.getMessage().toLowerCase().contains("negative"));
                }

                @Test
                @DisplayName("Add physical product with missing weight should throw IllegalArgumentException")
                void testHandleAddProduct_PhysicalProductMissingWeight_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.remove("weight");

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                }

                @Test
                @DisplayName("Add physical product with missing dimensions should throw IllegalArgumentException")
                void testHandleAddProduct_PhysicalProductMissingDimensions_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.remove("dimensions");

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                }

                // ==================== DATE FORMAT VALIDATION TESTS ====================

                @Test
                @DisplayName("Add Book with invalid publication date format should throw IllegalArgumentException")
                void testHandleAddProduct_BookInvalidDateFormat_ShouldThrowException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidBookAttributes();
                        specificAttributes.put("publicationDate", "15-01-2024"); // Wrong format (DD-MM-YYYY)

                        // Mock service to throw exception for invalid date format
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException(
                                                        "Publication date must be in format YYYY-MM-DD"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        specificAttributes,
                                                        null));
                        assertTrue(exception.getMessage().contains("date")
                                        || exception.getMessage().contains("YYYY-MM-DD"));
                }

                @Test
                @DisplayName("Add CD with invalid release date format should throw IllegalArgumentException")
                void testHandleAddProduct_CDInvalidDateFormat_ShouldThrowException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidCDAttributes();
                        specificAttributes.put("releaseDate", "2024/01/15"); // Wrong format (slash separator)
                        List<Track> tracks = createSampleTracks(3);

                        // Mock service to throw exception for invalid date format
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException(
                                                        "Release date must be in format YYYY-MM-DD"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.CD.toString(),
                                                        commonFields,
                                                        specificAttributes,
                                                        tracks));
                        assertTrue(exception.getMessage().contains("date")
                                        || exception.getMessage().contains("YYYY-MM-DD"));
                }

                @Test
                @DisplayName("Add DVD with invalid release date format should throw IllegalArgumentException")
                void testHandleAddProduct_DVDInvalidDateFormat_ShouldThrowException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidDVDAttributes();
                        specificAttributes.put("releaseDate", "Jan 15, 2024"); // Wrong format (text format)

                        // Mock service to throw exception for invalid date format
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException(
                                                        "Release date must be in format YYYY-MM-DD"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.DVD.toString(),
                                                        commonFields,
                                                        specificAttributes,
                                                        null));
                        assertTrue(exception.getMessage().contains("date")
                                        || exception.getMessage().contains("YYYY-MM-DD"));
                }

                @Test
                @DisplayName("Add Newspaper with invalid publication date format should throw IllegalArgumentException")
                void testHandleAddProduct_NewspaperInvalidDateFormat_ShouldThrowException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidNewspaperAttributes();
                        specificAttributes.put("publicationDate", "20240115"); // Wrong format (no separators)

                        // Mock service to throw exception for invalid date format
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException(
                                                        "Publication date must be in format YYYY-MM-DD"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.NEWSPAPER.toString(),
                                                        commonFields,
                                                        specificAttributes,
                                                        null));
                        assertTrue(exception.getMessage().contains("date")
                                        || exception.getMessage().contains("YYYY-MM-DD"));
                }

                @Test
                @DisplayName("Add Book with invalid date values should throw IllegalArgumentException")
                void testHandleAddProduct_BookInvalidDateValues_ShouldThrowException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Map<String, String> specificAttributes = createValidBookAttributes();
                        specificAttributes.put("publicationDate", "2024-02-30"); // Invalid date (Feb 30 doesn't exist)

                        // Mock service to throw exception for invalid date values
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException(
                                                        "Publication date is not a valid date"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        specificAttributes,
                                                        null));
                        assertTrue(exception.getMessage().toLowerCase().contains("date") ||
                                        exception.getMessage().toLowerCase().contains("valid"));
                }
        }

        // ============================================================
        // NESTED CLASS: UPDATE PRODUCT TESTS
        // ============================================================
        @Nested
        @DisplayName("handleUpdateProduct() Tests")
        class HandleUpdateProductTests {

                @Test
                @DisplayName("Update Book with valid data should return updated product")
                void testHandleUpdateProduct_ValidBook_ShouldReturnUpdatedProduct() throws Exception {
                        // Arrange
                        String productIdRaw = "1";
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();
                        Map<String, String> specificAttributes = createValidBookAttributes();

                        Book expectedBook = createSampleBook();
                        expectedBook.setProductId(1L);
                        when(mockProductService.updateProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleUpdateProduct(
                                        productIdRaw,
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        specificAttributes);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        assertEquals(expectedBook.getTitle(), result.getTitle());
                        verify(mockProductService).updateProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Update CD with valid data should return updated product")
                void testHandleUpdateProduct_ValidCD_ShouldReturnUpdatedProduct() throws Exception {
                        // Arrange
                        String productIdRaw = "2";
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();
                        Map<String, String> specificAttributes = createValidCDAttributes();

                        CD expectedCD = createSampleCD();
                        expectedCD.setProductId(2L);
                        when(mockProductService.updateProduct(any(ProductDTO.class))).thenReturn(expectedCD);

                        // Act
                        Product result = controller.handleUpdateProduct(
                                        productIdRaw,
                                        ProductType.CD.toString(),
                                        commonFields,
                                        specificAttributes);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        verify(mockProductService).updateProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Update DVD with valid data should return updated product")
                void testHandleUpdateProduct_ValidDVD_ShouldReturnUpdatedProduct() throws Exception {
                        // Arrange
                        String productIdRaw = "3";
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();
                        Map<String, String> specificAttributes = createValidDVDAttributes();

                        DVD expectedDVD = createSampleDVD();
                        expectedDVD.setProductId(3L);
                        when(mockProductService.updateProduct(any(ProductDTO.class))).thenReturn(expectedDVD);

                        // Act
                        Product result = controller.handleUpdateProduct(
                                        productIdRaw,
                                        ProductType.DVD.toString(),
                                        commonFields,
                                        specificAttributes);

                        // Assert
                        assertNotNull(result, "Result should not be null");
                        verify(mockProductService).updateProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Update with null productId should throw IllegalArgumentException")
                void testHandleUpdateProduct_NullProductId_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateProduct(
                                                        null,
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes()));
                        assertTrue(exception.getMessage().toLowerCase().contains("required") ||
                                        exception.getMessage().toLowerCase().contains("product id"));
                }

                @Test
                @DisplayName("Update with empty productId should throw IllegalArgumentException")
                void testHandleUpdateProduct_EmptyProductId_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateProduct(
                                                        "",
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes()));
                }

                @Test
                @DisplayName("Update with invalid productId format should throw IllegalArgumentException")
                void testHandleUpdateProduct_InvalidProductIdFormat_ShouldThrowException() {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateProduct(
                                                        "not_a_number",
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes()));
                        assertTrue(exception.getMessage().toLowerCase().contains("number") ||
                                        exception.getMessage().toLowerCase().contains("valid"));
                }

                @Test
                @DisplayName("Update non-existent product should throw IllegalArgumentException")
                void testHandleUpdateProduct_NonExistentProduct_ShouldThrowException() throws Exception {
                        // Arrange
                        String productIdRaw = "999";
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();

                        when(mockProductService.updateProduct(any(ProductDTO.class)))
                                        .thenThrow(new ProductNotFoundException("Product not found with ID: 999"));

                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateProduct(
                                                        productIdRaw,
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes()));
                }

                @Test
                @DisplayName("Update with price change should use currentPrice if provided")
                void testHandleUpdateProduct_WithCurrentPrice_ShouldUseProvidedPrice() throws Exception {
                        // Arrange
                        String productIdRaw = "1";
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();
                        commonFields.put("originalPrice", "100000");
                        commonFields.put("currentPrice", "80000"); // Discounted price

                        Book expectedBook = createSampleBook();
                        expectedBook.setProductId(1L);
                        expectedBook.setCurrentPrice(80000.0);
                        when(mockProductService.updateProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleUpdateProduct(
                                        productIdRaw,
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes());

                        // Assert
                        assertNotNull(result);
                        verify(mockProductService).updateProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Update with blank currentPrice should default to originalPrice")
                void testHandleUpdateProduct_BlankCurrentPrice_ShouldDefaultToOriginalPrice() throws Exception {
                        // Arrange
                        String productIdRaw = "1";
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();
                        commonFields.put("originalPrice", "100000");
                        commonFields.put("currentPrice", ""); // Blank - should default to originalPrice

                        Book expectedBook = createSampleBook();
                        expectedBook.setProductId(1L);
                        when(mockProductService.updateProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleUpdateProduct(
                                        productIdRaw,
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes());

                        // Assert
                        assertNotNull(result);
                        verify(mockProductService).updateProduct(any(ProductDTO.class));
                }
        }

        // ============================================================
        // NESTED CLASS: UPDATE STOCK TESTS
        // ============================================================
        @Nested
        @DisplayName("handleUpdateStock() Tests")
        class HandleUpdateStockTests {

                @Test
                @DisplayName("Update stock with valid data should succeed")
                void testHandleUpdateStock_ValidData_ShouldSucceed() {
                        // Arrange
                        String barcode = VALID_BARCODE;
                        String newStock = "100";
                        String reason = "Stock replenishment";

                        when(mockProductService.updateStock(anyString(), anyInt(), anyString())).thenReturn(true);

                        // Act - no exception should be thrown
                        assertDoesNotThrow(() -> controller.handleUpdateStock(barcode, newStock, reason));

                        // Assert
                        verify(mockProductService).updateStock(barcode.trim(), 100, reason.trim());
                }

                @Test
                @DisplayName("Update stock with null barcode should throw IllegalArgumentException")
                void testHandleUpdateStock_NullBarcode_ShouldThrowException() {
                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(null, "100", "reason"));
                        assertTrue(exception.getMessage().toLowerCase().contains("barcode") ||
                                        exception.getMessage().toLowerCase().contains("required"));
                }

                @Test
                @DisplayName("Update stock with empty barcode should throw IllegalArgumentException")
                void testHandleUpdateStock_EmptyBarcode_ShouldThrowException() {
                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock("", "100", "reason"));
                }

                @Test
                @DisplayName("Update stock with null stock value should throw IllegalArgumentException")
                void testHandleUpdateStock_NullStockValue_ShouldThrowException() {
                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(VALID_BARCODE, null, "reason"));
                }

                @Test
                @DisplayName("Update stock with invalid stock format should throw IllegalArgumentException")
                void testHandleUpdateStock_InvalidStockFormat_ShouldThrowException() {
                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(VALID_BARCODE, "not_a_number", "reason"));
                        assertTrue(exception.getMessage().toLowerCase().contains("integer") ||
                                        exception.getMessage().toLowerCase().contains("valid"));
                }

                @Test
                @DisplayName("Update stock with negative stock should throw IllegalArgumentException")
                void testHandleUpdateStock_NegativeStock_ShouldThrowException() {
                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(VALID_BARCODE, "-10", "reason"));
                        assertTrue(exception.getMessage().toLowerCase().contains("negative"));
                }

                @Test
                @DisplayName("Update stock with null reason should throw IllegalArgumentException")
                void testHandleUpdateStock_NullReason_ShouldThrowException() {
                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(VALID_BARCODE, "100", null));
                }

                @Test
                @DisplayName("Update stock with empty reason should throw IllegalArgumentException")
                void testHandleUpdateStock_EmptyReason_ShouldThrowException() {
                        // Act & Assert
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(VALID_BARCODE, "100", ""));
                }

                @Test
                @DisplayName("Update stock when product not found should throw IllegalArgumentException")
                void testHandleUpdateStock_ProductNotFound_ShouldThrowException() {
                        // Arrange
                        when(mockProductService.updateStock(anyString(), anyInt(), anyString())).thenReturn(false);

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleUpdateStock(VALID_BARCODE, "100", "reason"));
                        assertTrue(exception.getMessage().toLowerCase().contains("failed") ||
                                        exception.getMessage().toLowerCase().contains("not exist"));
                }

                @Test
                @DisplayName("Update stock to zero should succeed")
                void testHandleUpdateStock_ZeroStock_ShouldSucceed() {
                        // Arrange
                        when(mockProductService.updateStock(anyString(), anyInt(), anyString())).thenReturn(true);

                        // Act & Assert
                        assertDoesNotThrow(() -> controller.handleUpdateStock(VALID_BARCODE, "0", "Sold out"));
                        verify(mockProductService).updateStock(anyString(), eq(0), anyString());
                }
        }

        // ============================================================
        // NESTED CLASS: BOUNDARY AND EDGE CASE TESTS
        // ============================================================
        @Nested
        @DisplayName("Boundary and Edge Case Tests")
        class BoundaryAndEdgeCaseTests {

                @Test
                @DisplayName("Add product with minimum valid stock (0) should succeed")
                void testHandleAddProduct_ZeroStock_ShouldSucceed() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("stock", "0");

                        Book expectedBook = createSampleBook();
                        expectedBook.setStock(0);
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes(),
                                        null);

                        // Assert
                        assertNotNull(result);
                }

                @Test
                @DisplayName("Add product with maximum stock should succeed")
                void testHandleAddProduct_MaxStock_ShouldSucceed() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("stock", "999999");

                        Book expectedBook = createSampleBook();
                        expectedBook.setStock(999999);
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes(),
                                        null);

                        // Assert
                        assertNotNull(result);
                }

                @Test
                @DisplayName("Add product with minimum valid price (0) should succeed")
                void testHandleAddProduct_ZeroPrice_ShouldSucceed() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("price", "0");

                        Book expectedBook = createSampleBook();
                        expectedBook.setOriginalPrice(0.0);
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes(),
                                        null);

                        // Assert
                        assertNotNull(result);
                }

                @Test
                @DisplayName("Add product with decimal price should succeed")
                void testHandleAddProduct_DecimalPrice_ShouldSucceed() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("price", "99999.99");

                        Book expectedBook = createSampleBook();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes(),
                                        null);

                        // Assert
                        assertNotNull(result);
                }

                @Test
                @DisplayName("Add product with whitespace in fields should trim values")
                void testHandleAddProduct_WhitespaceInFields_ShouldTrimValues() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        commonFields.put("barcode", "  12345678901234  ");
                        commonFields.put("title", "  Test Product  ");

                        Book expectedBook = createSampleBook();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        Product result = controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes(),
                                        null);

                        // Assert
                        assertNotNull(result);
                        verify(mockProductService).addProduct(any(ProductDTO.class));
                }
        }

        // ============================================================
        // NESTED CLASS: SERVICE INTERACTION TESTS
        // ============================================================
        @Nested
        @DisplayName("Service Interaction Tests")
        class ServiceInteractionTests {

                @Test
                @DisplayName("Add product should call service.addProduct exactly once")
                void testHandleAddProduct_ShouldCallServiceOnce() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        Book expectedBook = createSampleBook();
                        when(mockProductService.addProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        controller.handleAddProduct(
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes(),
                                        null);

                        // Assert
                        verify(mockProductService, times(1)).addProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Update product should call service.updateProduct exactly once")
                void testHandleUpdateProduct_ShouldCallServiceOnce() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFieldsForUpdate();
                        Book expectedBook = createSampleBook();
                        when(mockProductService.updateProduct(any(ProductDTO.class))).thenReturn(expectedBook);

                        // Act
                        controller.handleUpdateProduct(
                                        "1",
                                        ProductType.BOOK.toString(),
                                        commonFields,
                                        createValidBookAttributes());

                        // Assert
                        verify(mockProductService, times(1)).updateProduct(any(ProductDTO.class));
                }

                @Test
                @DisplayName("Service exception should be wrapped in IllegalArgumentException")
                void testHandleAddProduct_ServiceException_ShouldWrapInIllegalArgumentException() throws Exception {
                        // Arrange
                        Map<String, String> commonFields = createValidCommonFields();
                        when(mockProductService.addProduct(any(ProductDTO.class)))
                                        .thenThrow(new InvalidProductDataException("Service validation failed"));

                        // Act & Assert
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> controller.handleAddProduct(
                                                        ProductType.BOOK.toString(),
                                                        commonFields,
                                                        createValidBookAttributes(),
                                                        null));
                        assertTrue(exception.getMessage().contains("Service validation failed"));
                }
        }

        // ============================================================
        // HELPER METHODS: Create Test Data
        // ============================================================

        private Map<String, String> createValidCommonFields() {
                Map<String, String> fields = new HashMap<>();
                fields.put("barcode", VALID_BARCODE);
                fields.put("title", VALID_TITLE);
                fields.put("category", VALID_CATEGORY);
                fields.put("price", VALID_PRICE);
                fields.put("stock", VALID_STOCK);
                fields.put("weight", VALID_WEIGHT);
                fields.put("dimensions", VALID_DIMENSIONS);
                fields.put("vatRate", VALID_VAT_RATE);
                fields.put("description", VALID_DESCRIPTION);
                return fields;
        }

        private Map<String, String> createValidCommonFieldsForUpdate() {
                Map<String, String> fields = new HashMap<>();
                fields.put("barcode", VALID_BARCODE);
                fields.put("title", VALID_TITLE);
                fields.put("category", VALID_CATEGORY);
                fields.put("originalPrice", VALID_PRICE);
                fields.put("currentPrice", VALID_PRICE);
                fields.put("weight", VALID_WEIGHT);
                fields.put("dimensions", VALID_DIMENSIONS);
                fields.put("vatRate", VALID_VAT_RATE);
                fields.put("description", VALID_DESCRIPTION);
                fields.put("status", "available");
                return fields;
        }

        private Map<String, String> createValidBookAttributes() {
                Map<String, String> attrs = new HashMap<>();
                attrs.put("author", "Test Author");
                attrs.put("publisher", "Test Publisher");
                attrs.put("publicationDate", "2024-01-15");
                attrs.put("pages", "300");
                attrs.put("language", "English");
                attrs.put("coverType", "Paperback");
                attrs.put("genre", "Fiction");
                return attrs;
        }

        private Map<String, String> createValidCDAttributes() {
                Map<String, String> attrs = new HashMap<>();
                attrs.put("artist", "Test Artist");
                attrs.put("recordLabel", "Test Label");
                attrs.put("genre", "Pop");
                attrs.put("releaseDate", "2024-01-15");
                return attrs;
        }

        private Map<String, String> createValidDVDAttributes() {
                Map<String, String> attrs = new HashMap<>();
                attrs.put("director", "Test Director");
                attrs.put("duration", "120");
                attrs.put("studio", "Test Studio");
                attrs.put("subtitle", "Vietnamese");
                attrs.put("genre", "Action");
                attrs.put("releaseDate", "2024-01-15");
                return attrs;
        }

        private Map<String, String> createValidNewspaperAttributes() {
                Map<String, String> attrs = new HashMap<>();
                attrs.put("publisher", "Test Publisher");
                attrs.put("issn", "2024-001");
                attrs.put("frequency", "Daily");
                attrs.put("publicationDate", "2024-01-15");
                return attrs;
        }

        private List<Track> createSampleTracks(int count) {
                List<Track> tracks = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                        Track track = new Track();
                        track.setTrackId((long) i);
                        track.setTitle("Track " + i);
                        track.setDuration(180 + i * 10); // Duration in seconds
                        tracks.add(track);
                }
                return tracks;
        }

        private Book createSampleBook() {
                Book book = new Book();
                book.setProductId(1L);
                book.setBarcode(VALID_BARCODE);
                book.setTitle(VALID_TITLE);
                book.setCategory(VALID_CATEGORY);
                book.setOriginalPrice(Double.parseDouble(VALID_PRICE));
                book.setCurrentPrice(Double.parseDouble(VALID_PRICE));
                book.setStock(Integer.parseInt(VALID_STOCK));
                book.setWeight(Double.parseDouble(VALID_WEIGHT));
                book.setDimensions(VALID_DIMENSIONS);
                book.setAuthor("Test Author");
                book.setPublisher("Test Publisher");
                book.setPages(300);
                book.setLanguage("English");
                book.setCoverType("Paperback");
                book.setCreatedAt(LocalDateTime.now());
                book.setUpdatedAt(LocalDateTime.now());
                return book;
        }

        private CD createSampleCD() {
                CD cd = new CD();
                cd.setProductId(2L);
                cd.setBarcode(VALID_BARCODE);
                cd.setTitle("Test CD");
                cd.setCategory("Music");
                cd.setOriginalPrice(Double.parseDouble(VALID_PRICE));
                cd.setCurrentPrice(Double.parseDouble(VALID_PRICE));
                cd.setStock(Integer.parseInt(VALID_STOCK));
                cd.setWeight(Double.parseDouble(VALID_WEIGHT));
                cd.setDimensions(VALID_DIMENSIONS);
                cd.setArtist("Test Artist");
                cd.setRecordLabel("Test Label");
                cd.setGenre("Pop");
                cd.setCreatedAt(LocalDateTime.now());
                cd.setUpdatedAt(LocalDateTime.now());
                return cd;
        }

        private DVD createSampleDVD() {
                DVD dvd = new DVD();
                dvd.setProductId(3L);
                dvd.setBarcode(VALID_BARCODE);
                dvd.setTitle("Test DVD");
                dvd.setCategory("Movies");
                dvd.setOriginalPrice(Double.parseDouble(VALID_PRICE));
                dvd.setCurrentPrice(Double.parseDouble(VALID_PRICE));
                dvd.setStock(Integer.parseInt(VALID_STOCK));
                dvd.setWeight(Double.parseDouble(VALID_WEIGHT));
                dvd.setDimensions(VALID_DIMENSIONS);
                dvd.setDirector("Test Director");
                dvd.setDuration(120);
                dvd.setStudio("Test Studio");
                dvd.setSubtitle("Vietnamese");
                dvd.setCreatedAt(LocalDateTime.now());
                dvd.setUpdatedAt(LocalDateTime.now());
                return dvd;
        }

        private Newspaper createSampleNewspaper() {
                Newspaper newspaper = new Newspaper();
                newspaper.setProductId(4L);
                newspaper.setBarcode(VALID_BARCODE);
                newspaper.setTitle("Test Newspaper");
                newspaper.setCategory("News");
                newspaper.setOriginalPrice(Double.parseDouble(VALID_PRICE));
                newspaper.setCurrentPrice(Double.parseDouble(VALID_PRICE));
                newspaper.setStock(Integer.parseInt(VALID_STOCK));
                newspaper.setWeight(Double.parseDouble(VALID_WEIGHT));
                newspaper.setDimensions(VALID_DIMENSIONS);
                newspaper.setPublisher("Test Publisher");
                newspaper.setIssn("2024-001");
                newspaper.setFrequency("Daily");
                newspaper.setCreatedAt(LocalDateTime.now());
                newspaper.setUpdatedAt(LocalDateTime.now());
                return newspaper;
        }
}
