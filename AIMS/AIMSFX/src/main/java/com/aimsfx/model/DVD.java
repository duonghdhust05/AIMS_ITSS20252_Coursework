package com.aimsfx.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DVD - Concrete product type representing digital video discs
 * 
 * HIERARCHY: Product -> PhysicalProduct -> DVD
 * 
 * COHESION: Informational  - All fields represent DVD-specific attributes
 * COUPLING: Data Coupling  - Inherits from PhysicalProduct, returns Map
 */
public class DVD extends PhysicalProduct {
    private String director;
    private String studio;
    private String subtitle;
    private String discType;     // Disc type (e.g., DVD-9, Blu-ray)
    private Integer duration;
    private String genre;
    private Date releaseDate;

    public DVD() {
        super();
    }

    public DVD(Long productId, String barcode, String title, String category, 
               Double originalPrice, Double currentPrice,
               String description, Double weight, String dimensions, 
               Integer stock, String status, Double vatRate,
               String director, String studio, String subtitle, String discType,
               Integer duration, String genre, Date releaseDate) {
        super(productId, barcode, title, category, originalPrice, currentPrice, 
              description, weight, dimensions, stock, status, vatRate);
        this.director = director;
        this.studio = studio;
        this.subtitle = subtitle;
        this.discType = discType;
        this.duration = duration;
        this.genre = genre;
        this.releaseDate = releaseDate;
    }

    // -------- Getter and Setter Methods --------

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDiscType() {
        return discType;
    }

    public void setDiscType(String discType) {
        this.discType = discType;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * OCP SOLUTION: Returns this product's type via polymorphism
     */
    @Override
    public ProductType getProductType() {
        return ProductType.DVD;
    }

    @Override
    public Map<String, Object> getSpecificDetail() {
        Map<String, Object> details = new HashMap<>();
        details.put("director", this.director);
        details.put("studio", this.studio);
        details.put("subtitle", this.subtitle);
        details.put("discType", this.discType);
        details.put("duration", this.duration);
        details.put("genre", this.genre);
        details.put("releaseDate", this.releaseDate);
        return details;
    }

    /**
     * OCP SOLUTION: Polymorphic equality - compares DVD-specific fields
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        DVD other = (DVD) obj;
        return Objects.equals(director, other.director) &&
               Objects.equals(studio, other.studio) &&
               Objects.equals(subtitle, other.subtitle) &&
               Objects.equals(discType, other.discType) &&
               Objects.equals(duration, other.duration) &&
               Objects.equals(genre, other.genre) &&
               Objects.equals(releaseDate, other.releaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), director, studio, subtitle,
                           discType, duration, genre, releaseDate);
    }

    /**
     * OCP SOLUTION: Implements Prototype Pattern for DVD
     * Creates a deep copy of this DVD with all attributes
     * 
     * @return A new DVD instance with identical attributes
     */
    @Override
    public Product copy() {
        DVD copy = new DVD();
        // Copy common attributes using parent helper method
        copyCommonAttributesTo(copy);
        // Copy DVD-specific attributes
        copy.setDirector(this.director);
        copy.setGenre(this.genre);
        copy.setDuration(this.duration);
        copy.setStudio(this.studio);
        copy.setDiscType(this.discType);
        copy.setSubtitle(this.subtitle);
        copy.setReleaseDate(this.releaseDate);
        return copy;
    }

}
