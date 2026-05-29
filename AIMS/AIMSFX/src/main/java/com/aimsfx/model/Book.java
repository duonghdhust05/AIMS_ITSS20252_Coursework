package com.aimsfx.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Book - Concrete product type representing books
 * 
 * INHERITANCE: Extends PhysicalProduct base class
 * - Inherits common product attributes (title, price, stock, weight,
 * dimensions, etc.)
 * - Adds book-specific attributes (author, publisher, pages, etc.)
 * - Implements abstract methods: getSpecificDetail(), getProductType()
 * 
 * HIERARCHY: Product -> PhysicalProduct -> Book
 * 
 * COHESION: Informational
 * - All fields represent book-specific attributes
 * - getters/setters + getSpecificDetail(): single purpose (describe a book)
 * 
 * COUPLING: Data Coupling
 * - Inherits from PhysicalProduct (necessary OOP coupling)
 * - getSpecificDetail() returns Map: minimal dependencies
 * 
 */
public class Book extends PhysicalProduct {

    private String author; // Book author(s)
    private String publisher; // Publishing company
    private String publicationDate; // When book was published
    private Integer pages; // Page count
    private String language; // Language of content
    private String coverType; // Paperback or Hardcover
    private String genre; // Book genre (e.g., Fiction, Non-fiction, etc.)

    public Book() {
        super();
    }

    public Book(Long productId, String barcode, String title, String category,
            Double originalPrice, Double currentPrice,
            String description, Double weight, String dimensions,
            Integer stock, String status, Double vatRate,
            String author, String publisher, String publicationDate,
            Integer pages, String language, String coverType, String genre) {
        // Call parent constructor with common fields
        super(productId, barcode, title, category, originalPrice, currentPrice,
                description, weight, dimensions, stock, status, vatRate);

        // Initialize book-specific fields
        this.author = author;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.pages = pages;
        this.language = language;
        this.coverType = coverType;
        this.genre = genre;
    }

    // -------- Getter and Setter Methods --------

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCoverType() {
        return coverType;
    }

    public void setCoverType(String coverType) {
        this.coverType = coverType;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.BOOK;
    }

    /**
     * POLYMORPHISM: Exports book-specific data as Map
     * Complements getCommonProductInfo() from parent
     * 
     * @return Map with book-specific attributes
     */
    @Override
    public Map<String, Object> getSpecificDetail() {
        Map<String, Object> details = new HashMap<>();
        details.put("author", this.author);
        details.put("publisher", this.publisher);
        details.put("publicationDate", this.publicationDate);
        details.put("pages", this.pages);
        details.put("language", this.language);
        details.put("coverType", this.coverType);
        details.put("genre", this.genre);
        return details;
    }

    /**
     * OCP SOLUTION: Polymorphic equality - compares Book-specific fields
     * Calls super.equals() for common fields, then compares book attributes
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        Book other = (Book) obj;
        return Objects.equals(author, other.author) &&
                Objects.equals(publisher, other.publisher) &&
                Objects.equals(publicationDate, other.publicationDate) &&
                Objects.equals(pages, other.pages) &&
                Objects.equals(language, other.language) &&
                Objects.equals(coverType, other.coverType) &&
                Objects.equals(genre, other.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author, publisher, publicationDate,
                pages, language, coverType, genre);
    }

    /**
     * OCP SOLUTION: Implements Prototype Pattern for Book
     * Creates a deep copy of this Book with all attributes
     * 
     * @return A new Book instance with identical attributes
     */
    @Override
    public Product copy() {
        Book copy = new Book();
        // Copy common attributes using parent helper method
        copyCommonAttributesTo(copy);
        // Copy book-specific attributes
        copy.setAuthor(this.author);
        copy.setPublisher(this.publisher);
        copy.setPublicationDate(this.publicationDate);
        copy.setPages(this.pages);
        copy.setLanguage(this.language);
        copy.setCoverType(this.coverType);
        return copy;
    }

}
