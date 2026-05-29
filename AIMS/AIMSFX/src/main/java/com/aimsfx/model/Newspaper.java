package com.aimsfx.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Newspaper - Concrete product type representing newspapers/periodicals
 * 
 * HIERARCHY: Product -> PhysicalProduct -> Newspaper
 * 
 * COHESION: Informational - All fields represent Newspaper-specific
 * attributes
 * COUPLING: Data Coupling - Inherits from PhysicalProduct, returns Map
 */
public class Newspaper extends PhysicalProduct {
    private String issn;
    private String frequency;
    private String editorInChief;   // Editor-in-chief name
    private String publisher;
    private Date publicationDate;
    private String language;         // Language(s) of the newspaper
    private String section;          // Sections (e.g., Politics, Economy, Sports)

    public Newspaper() {
        super();
    }

    public Newspaper(Long productId, String barcode, String title, String category, 
                     Double originalPrice, Double currentPrice,
                     String description, Double weight, String dimensions, 
                     Integer stock, String status, Double vatRate,
                     String issn, String frequency, String editorInChief,
                     String publisher, Date publicationDate, String language, String section) {
        super(productId, barcode, title, category, originalPrice, currentPrice, 
              description, weight, dimensions, stock, status, vatRate);
        this.issn = issn;
        this.frequency = frequency;
        this.editorInChief = editorInChief;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.language = language;
        this.section = section;
    }

    // -------- Getter and Setter Methods --------

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getEditorInChief() {
        return editorInChief;
    }

    public void setEditorInChief(String editorInChief) {
        this.editorInChief = editorInChief;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    /**
     * OCP SOLUTION: Returns this product's type via polymorphism
     */
    @Override
    public ProductType getProductType() {
        return ProductType.NEWSPAPER;
    }

    @Override
    public Map<String, Object> getSpecificDetail() {
        Map<String, Object> details = new HashMap<>();
        details.put("issn", this.issn);
        details.put("frequency", this.frequency);
        details.put("editorInChief", this.editorInChief);
        details.put("publisher", this.publisher);
        details.put("publicationDate", this.publicationDate);
        details.put("language", this.language);
        details.put("section", this.section);
        return details;
    }

    /**
     * OCP SOLUTION: Polymorphic equality - compares Newspaper-specific fields
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        Newspaper other = (Newspaper) obj;
        return Objects.equals(issn, other.issn) &&
               Objects.equals(frequency, other.frequency) &&
               Objects.equals(editorInChief, other.editorInChief) &&
               Objects.equals(publisher, other.publisher) &&
               Objects.equals(publicationDate, other.publicationDate) &&
               Objects.equals(language, other.language) &&
               Objects.equals(section, other.section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), issn, frequency, editorInChief,
                           publisher, publicationDate, language, section);
    }

    /**
     * OCP SOLUTION: Implements Prototype Pattern for Newspaper
     * Creates a deep copy of this Newspaper with all attributes
     * 
     * @return A new Newspaper instance with identical attributes
     */
    @Override
    public Product copy() {
        Newspaper copy = new Newspaper();
        // Copy common attributes using parent helper method
        copyCommonAttributesTo(copy);
        // Copy Newspaper-specific attributes
        copy.setIssn(this.issn);
        copy.setFrequency(this.frequency);
        copy.setPublisher(this.publisher);
        copy.setPublicationDate(this.publicationDate);
        return copy;
    }

}
