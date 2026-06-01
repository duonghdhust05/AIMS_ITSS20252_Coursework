package com.aimsfx.controller;

import com.aimsfx.controller.ProductManagerController.IProductDataProvider;
import com.aimsfx.controller.ProductManagerController.ViewProductController;
import com.aimsfx.exception.ProductNotFoundException;
import com.aimsfx.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ViewProductController
 * Tests the View Product Detail use case
 * 
 * SOLID VERIFICATION:
 * - Uses mock IProductDataProvider to verify DIP compliance
 * - Tests data retrieval, validation, and transformation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ViewProductController Tests - View Product Detail Use Case")
class ViewProductControllerTest {

    @Mock
    private IProductDataProvider mockDataProvider;

    private ViewProductController controller;

    @BeforeEach
    void setUp() {
        // Use DI constructor to inject mock data provider
        controller = new ViewProductController(mockDataProvider);
    }

    // ==================== TEST getProductDetail() ====================

    @Test
    @DisplayName("getProductDetail with valid Book ID should return complete product data")
    void testGetProductDetail_WithValidBookId_ShouldReturnCompleteData() throws Exception {
        // Arrange: Create and mock a Book product
        Book book = createSampleBook();
        when(mockDataProvider.findById(1L)).thenReturn(book);

        // Act: Get product detail
        Map<String, Object> result = controller.getProductDetail("1");

        // Assert: Verify common fields
        assertNotNull(result, "Result should not be null");
        assertEquals("Clean Code", result.get("title"));
        assertEquals("Programming", result.get("category"));
        assertEquals(450000.0, result.get("currentPrice"));
        assertEquals("BOOK", result.get("productType"));

        // Assert: Verify specific fields with "specific_" prefix
        assertEquals("Robert C. Martin", result.get("specific_author"));
        assertEquals("Prentice Hall", result.get("specific_publisher"));
        assertEquals(464, result.get("specific_pages"));
        assertEquals("English", result.get("specific_language"));
        assertEquals("Paperback", result.get("specific_coverType"));

        // Verify repository was called
        verify(mockDataProvider).findById(1L);
    }

    @Test
    @DisplayName("getProductDetail with valid CD ID should return CD-specific data")
    void testGetProductDetail_WithValidCDId_ShouldReturnCDData() throws Exception {
        // Arrange: Create and mock a CD product
        CD cd = createSampleCD();
        when(mockDataProvider.findById(2L)).thenReturn(cd);

        // Act: Get product detail
        Map<String, Object> result = controller.getProductDetail("2");

        // Assert: Verify common and specific fields
        assertEquals("Greatest Hits", result.get("title"));
        assertEquals("CD", result.get("productType"));
        assertEquals("The Beatles", result.get("specific_artist"));
        assertEquals("Apple Records", result.get("specific_recordLabel"));
        assertEquals("Rock", result.get("specific_genre"));
        assertEquals(15, result.get("specific_trackCount"));
    }

    @Test
    @DisplayName("getProductDetail with valid DVD ID should return DVD-specific data")
    void testGetProductDetail_WithValidDVDId_ShouldReturnDVDData() throws Exception {
        // Arrange: Create and mock a DVD product
        DVD dvd = createSampleDVD();
        when(mockDataProvider.findById(3L)).thenReturn(dvd);

        // Act: Get product detail
        Map<String, Object> result = controller.getProductDetail("3");

        // Assert: Verify common and specific fields
        assertEquals("The Matrix", result.get("title"));
        assertEquals("DVD", result.get("productType"));
        assertEquals("Wachowski Brothers", result.get("specific_director"));
        assertEquals(136, result.get("specific_duration"));
        assertEquals("Warner Bros", result.get("specific_studio"));
        assertEquals("Multiple", result.get("specific_subtitle"));
    }

    @Test
    @DisplayName("getProductDetail with valid Newspaper ID should return Newspaper-specific data")
    void testGetProductDetail_WithValidNewspaperId_ShouldReturnNewspaperData() throws Exception {
        // Arrange: Create and mock a Newspaper product
        Newspaper newspaper = createSampleNewspaper();
        when(mockDataProvider.findById(4L)).thenReturn(newspaper);

        // Act: Get product detail
        Map<String, Object> result = controller.getProductDetail("4");

        // Assert: Verify common and specific fields
        assertEquals("Daily News", result.get("title"));
        assertEquals("NEWSPAPER", result.get("productType"));
        assertEquals("2024-001", result.get("specific_issn"));
        assertEquals("News Corp", result.get("specific_publisher"));
        assertEquals("Daily", result.get("specific_frequency"));
    }

    @Test
    @DisplayName("getProductDetail with null ID should throw ProductNotFoundException")
    void testGetProductDetail_WithNullId_ShouldThrowException() {
        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail(null),
                "Should throw ProductNotFoundException for null ID");

        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("getProductDetail with empty ID should throw ProductNotFoundException")
    void testGetProductDetail_WithEmptyId_ShouldThrowException() {
        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail(""),
                "Should throw ProductNotFoundException for empty ID");

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("getProductDetail with blank ID should throw ProductNotFoundException")
    void testGetProductDetail_WithBlankId_ShouldThrowException() {
        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail("   "),
                "Should throw ProductNotFoundException for blank ID");
    }

    @Test
    @DisplayName("getProductDetail with invalid ID format should throw ProductNotFoundException")
    void testGetProductDetail_WithInvalidIdFormat_ShouldThrowException() {
        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail("invalid-id"),
                "Should throw ProductNotFoundException for invalid ID format");

        assertTrue(exception.getMessage().contains("Invalid product ID format"));
    }

    @Test
    @DisplayName("getProductDetail with non-existent ID should throw ProductNotFoundException")
    void testGetProductDetail_WithNonExistentId_ShouldThrowException() {
        // Arrange: Mock repository to return null
        when(mockDataProvider.findById(99999L)).thenReturn(null);

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail("99999"),
                "Should throw ProductNotFoundException for non-existent ID");

        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    @DisplayName("getProductDetail with invalid product data should throw ProductNotFoundException")
    void testGetProductDetail_WithInvalidProductData_ShouldThrowException() {
        // Arrange: Create product with invalid data (missing required fields)
        Book invalidBook = new Book();
        invalidBook.setProductId(1000L);
        // Missing title and barcode - required fields
        invalidBook.setCurrentPrice(100.0);
        when(mockDataProvider.findById(1000L)).thenReturn(invalidBook);

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail("1000"),
                "Should throw ProductNotFoundException for invalid product data");

        assertTrue(exception.getMessage().contains("invalid"));
    }

    // ==================== TEST findProductById() ====================

    @Test
    @DisplayName("findProductById with valid ID should return product")
    void testFindProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        Book book = createSampleBook();
        when(mockDataProvider.findById(1L)).thenReturn(book);

        // Act
        Product result = controller.findProductById("1");

        // Assert
        assertNotNull(result);
        assertEquals(book.getProductId(), result.getProductId());
        assertEquals("Clean Code", result.getTitle());
    }

    @Test
    @DisplayName("findProductById with null ID should return null")
    void testFindProductById_WithNullId_ShouldReturnNull() {
        // Act
        Product result = controller.findProductById(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("findProductById with non-existent ID should return null")
    void testFindProductById_WithNonExistentId_ShouldReturnNull() {
        // Arrange
        when(mockDataProvider.findById(99999L)).thenReturn(null);

        // Act
        Product result = controller.findProductById("99999");

        // Assert
        assertNull(result);
    }

    // ==================== TEST getProductTypeDisplay() ====================

    @Test
    @DisplayName("getProductTypeDisplay for Book should return friendly name")
    void testGetProductTypeDisplay_ForBook_ShouldReturnFriendlyName() {
        // Arrange
        Book book = createSampleBook();

        // Act
        String result = controller.getProductTypeDisplay(book);

        // Assert
        assertEquals("Book", result);
    }

    @Test
    @DisplayName("getProductTypeDisplay for CD should return friendly name")
    void testGetProductTypeDisplay_ForCD_ShouldReturnFriendlyName() {
        // Arrange
        CD cd = createSampleCD();

        // Act
        String result = controller.getProductTypeDisplay(cd);

        // Assert
        assertEquals("Compact Disc (CD)", result);
    }

    @Test
    @DisplayName("getProductTypeDisplay for DVD should return friendly name")
    void testGetProductTypeDisplay_ForDVD_ShouldReturnFriendlyName() {
        // Arrange
        DVD dvd = createSampleDVD();

        // Act
        String result = controller.getProductTypeDisplay(dvd);

        // Assert
        assertEquals("Digital Video Disc (DVD)", result);
    }

    @Test
    @DisplayName("getProductTypeDisplay for Newspaper should return friendly name")
    void testGetProductTypeDisplay_ForNewspaper_ShouldReturnFriendlyName() {
        // Arrange
        Newspaper newspaper = createSampleNewspaper();

        // Act
        String result = controller.getProductTypeDisplay(newspaper);

        // Assert
        assertEquals("Newspaper", result);
    }

    @Test
    @DisplayName("getProductTypeDisplay for null should return Unknown")
    void testGetProductTypeDisplay_ForNull_ShouldReturnUnknown() {
        // Act
        String result = controller.getProductTypeDisplay(null);

        // Assert
        assertEquals("Unknown", result);
    }

    // ==================== TEST Data Validation ====================

    @Test
    @DisplayName("Product with negative price should be invalid")
    void testValidation_WithNegativePrice_ShouldBeInvalid() {
        // Arrange: Create product with negative price
        Book book = createSampleBook();
        book.setCurrentPrice(-100.0);
        when(mockDataProvider.findById(1L)).thenReturn(book);

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> controller.getProductDetail("1"));
    }

    // ==================== TEST Data Merging ====================

    @Test
    @DisplayName("Merged data should contain both common and specific fields")
    void testMergedData_ShouldContainBothCommonAndSpecificFields() throws Exception {
        // Arrange
        Book book = createSampleBook();
        when(mockDataProvider.findById(1L)).thenReturn(book);

        // Act
        Map<String, Object> result = controller.getProductDetail("1");

        // Assert: Check common fields exist
        assertTrue(result.containsKey("title"));
        assertTrue(result.containsKey("category"));
        assertTrue(result.containsKey("currentPrice"));
        assertTrue(result.containsKey("barcode"));

        // Assert: Check specific fields exist with prefix
        assertTrue(result.containsKey("specific_author"));
        assertTrue(result.containsKey("specific_publisher"));
        assertTrue(result.containsKey("specific_pages"));

        // Assert: Check productType is added
        assertTrue(result.containsKey("productType"));
    }

    @Test
    @DisplayName("Specific fields should have 'specific_' prefix to avoid conflicts")
    void testSpecificFields_ShouldHavePrefix() throws Exception {
        // Arrange: Create products with potentially conflicting field names
        Newspaper newspaper = createSampleNewspaper();
        when(mockDataProvider.findById(4L)).thenReturn(newspaper);

        // Act
        Map<String, Object> result = controller.getProductDetail("4");

        // Assert: Publisher exists in both common and specific
        // Specific should be prefixed
        assertTrue(result.containsKey("specific_publisher"));
        assertNotNull(result.get("specific_publisher"));
    }

    // ==================== TEST DIP Compliance ====================

    @Test
    @DisplayName("Controller should use injected data provider (DIP compliance)")
    void testController_UseInjectedDataProvider_DIPCompliance() throws Exception {
        // Arrange
        Book book = createSampleBook();
        when(mockDataProvider.findById(1L)).thenReturn(book);

        // Act
        controller.getProductDetail("1");

        // Assert: Verify the injected mock was used, not a real implementation
        verify(mockDataProvider, times(1)).findById(1L);
        verifyNoMoreInteractions(mockDataProvider);
    }

    // ==================== Helper Methods for Test Data ====================

    private Book createSampleBook() {
        Book book = new Book();
        book.setProductId(1L);
        book.setBarcode("ISBN-123456");
        book.setTitle("Clean Code");
        book.setCategory("Programming");
        book.setOriginalPrice(450000.0);
        book.setCurrentPrice(450000.0);
        book.setDescription("A handbook of agile software craftsmanship");
        book.setWeight(0.8);
        book.setDimensions("23x15x3 cm");
        book.setStock(10);
        book.setStatus("Available");
        book.setVatRate(0.1);

        // Book-specific fields
        book.setAuthor("Robert C. Martin");
        book.setPublisher("Prentice Hall");
        book.setPublicationDate("2008-08-01");
        book.setPages(464);
        book.setLanguage("English");
        book.setCoverType("Paperback");

        return book;
    }

    private CD createSampleCD() {
        CD cd = new CD();
        cd.setProductId(2L);
        cd.setBarcode("CD-789012");
        cd.setTitle("Greatest Hits");
        cd.setCategory("Music");
        cd.setOriginalPrice(150000.0);
        cd.setCurrentPrice(150000.0);
        cd.setDescription("Best songs compilation");
        cd.setWeight(0.1);
        cd.setDimensions("14x12.5x1 cm");
        cd.setStock(20);
        cd.setStatus("Available");
        cd.setVatRate(0.1);

        // CD-specific fields
        cd.setArtist("The Beatles");
        cd.setRecordLabel("Apple Records");
        cd.setReleaseDate(new Date()); // Use Date type
        cd.setGenre("Rock");
        cd.setTrackCount(15);

        return cd;
    }

    private DVD createSampleDVD() {
        DVD dvd = new DVD();
        dvd.setProductId(3L);
        dvd.setBarcode("DVD-345678");
        dvd.setTitle("The Matrix");
        dvd.setCategory("Movie");
        dvd.setOriginalPrice(200000.0);
        dvd.setCurrentPrice(200000.0);
        dvd.setDescription("Sci-fi action movie");
        dvd.setWeight(0.15);
        dvd.setDimensions("19x13.5x1.5 cm");
        dvd.setStock(15);
        dvd.setStatus("Available");
        dvd.setVatRate(0.1);

        // DVD-specific fields
        dvd.setDirector("Wachowski Brothers");
        dvd.setDuration(136);
        dvd.setStudio("Warner Bros");
        dvd.setReleaseDate(new Date()); // Use Date type
        dvd.setSubtitle("Multiple");

        return dvd;
    }

    private Newspaper createSampleNewspaper() {
        Newspaper newspaper = new Newspaper();
        newspaper.setProductId(4L);
        newspaper.setBarcode("NEWS-901234");
        newspaper.setTitle("Daily News");
        newspaper.setCategory("News");
        newspaper.setOriginalPrice(15000.0);
        newspaper.setCurrentPrice(15000.0);
        newspaper.setDescription("Daily newspaper");
        newspaper.setWeight(0.2);
        newspaper.setDimensions("42x28 cm");
        newspaper.setStock(100);
        newspaper.setStatus("Available");
        newspaper.setVatRate(0.05);

        // Newspaper-specific fields
        newspaper.setIssn("2024-001");
        newspaper.setPublicationDate(new Date()); // Use Date type
        newspaper.setPublisher("News Corp");
        newspaper.setFrequency("Daily");

        return newspaper;
    }
}
