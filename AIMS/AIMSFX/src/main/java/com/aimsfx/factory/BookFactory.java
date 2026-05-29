package com.aimsfx.factory;

import com.aimsfx.model.Book;
import com.aimsfx.model.Product;
import com.aimsfx.model.ProductType;
import com.aimsfx.model.meta.AttributeMeta;
import com.aimsfx.model.meta.InputType;

import java.util.List;

/**
 * BookFactory - Concrete Factory for creating Book products
 * 
 * SOLID PRINCIPLES:
 * - SRP: Only responsible for creating Book instances
 * - OCP: Can add new factories without modifying existing code
 * - LSP: Implements PhysicalProductFactory contract correctly
 * - ISP: Interface is minimal and focused
 * - DIP: Depends on Product abstraction
 */
public class BookFactory implements PhysicalProductFactory {

    @Override
    public Product createPhysicalProduct(Long productId, String barcode, String title, String category,
            Double originalPrice, Double currentPrice,
            String description, Double weight, String dimensions,
            Integer stock, String status, Double vatRate,
            String... attributes) {

        // Extract book-specific attributes
        String author = attributes.length > 0 ? attributes[0] : null;
        String publisher = attributes.length > 1 ? attributes[1] : null;
        String publicationDate = attributes.length > 2 ? attributes[2] : null;
        Integer pages = attributes.length > 3 ? parseIntOrNull(attributes[3]) : null;
        String language = attributes.length > 4 ? attributes[4] : null;
        String coverType = attributes.length > 5 ? attributes[5] : null;
        String genre = attributes.length > 6 ? attributes[6] : null;

        // Create and return Book instance
        return new Book(productId, barcode, title, category, originalPrice, currentPrice,
                description, weight, dimensions, stock, status, vatRate,
                author, publisher, publicationDate, pages, language, coverType, genre);
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BOOK;
    }

    @Override
    public String[] getAttributeLabels() {
        return new String[] { "Author*:", "Publisher*:", "Publication Date*:", "Pages:", "Language:", "Cover Type*:",
                "Genre:" };
    }

    @Override
    public String[] getAttributeKeys() {
        return new String[] { "author", "publisher", "publicationDate", "pages", "language", "coverType", "genre" };
    }

    @Override
    public List<AttributeMeta> getAttributeConfig() {
        return List.of(
                new AttributeMeta("author", "Author*:", InputType.TEXT),
                new AttributeMeta("publisher", "Publisher*:", InputType.TEXT),
                new AttributeMeta("publicationDate", "Publication Date*:", InputType.DATE, "yyyy-MM-dd"),
                new AttributeMeta("pages", "Pages:", InputType.NUMBER),
                new AttributeMeta("language", "Language:", InputType.TEXT),
                // OCP: This metadata replaces hardcoded `if (selectedType.equals("BOOK") && i
                // == 5)`
                new AttributeMeta("coverType", "Cover Type*:", List.of("Paperback", "Hardcover")),
                new AttributeMeta("genre", "Genre:", InputType.TEXT));
    }

    @Override
    public Product createEmptyProduct() {
        return new Book();
    }

    private Integer parseIntOrNull(String text) {
        if (text == null || text.isBlank())
            return null;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
