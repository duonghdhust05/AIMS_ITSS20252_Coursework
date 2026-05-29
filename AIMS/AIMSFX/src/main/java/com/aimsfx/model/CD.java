package com.aimsfx.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CD - Concrete product type representing compact discs
 * 
 * HIERARCHY: Product -> PhysicalProduct -> CD
 * 
 * COHESION: Informational  - All fields represent CD-specific attributes
 * COUPLING: Data Coupling  - Inherits from PhysicalProduct, returns Map
 */
public class CD extends PhysicalProduct {
    private String artist;
    private String recordLabel;  // Record label/company
    private String genre;
    private Integer trackCount;
    private Date releaseDate;

    public CD() {
        super();
    }

    public CD(Long productId, String barcode, String title, String category, 
              Double originalPrice, Double currentPrice,
              String description, Double weight, String dimensions, 
              Integer stock, String status, Double vatRate,
              String artist, String recordLabel, String genre, Integer trackCount, Date releaseDate) {
        super(productId, barcode, title, category, originalPrice, currentPrice, 
              description, weight, dimensions, stock, status, vatRate);
        this.artist = artist;
        this.recordLabel = recordLabel;
        this.genre = genre;
        this.trackCount = trackCount;
        this.releaseDate = releaseDate;
    }

    // -------- Getter and Setter Methods --------

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getRecordLabel() {
        return recordLabel;
    }

    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
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
        return ProductType.CD;
    }

    @Override
    public Map<String, Object> getSpecificDetail() {
        Map<String, Object> details = new HashMap<>();
        details.put("artist", this.artist);
        details.put("recordLabel", this.recordLabel);
        details.put("genre", this.genre);
        details.put("trackCount", this.trackCount);
        details.put("releaseDate", this.releaseDate);
        return details;
    }

    /**
     * OCP SOLUTION: Polymorphic equality - compares CD-specific fields
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        CD other = (CD) obj;
        return Objects.equals(artist, other.artist) &&
               Objects.equals(recordLabel, other.recordLabel) &&
               Objects.equals(genre, other.genre) &&
               Objects.equals(trackCount, other.trackCount) &&
               Objects.equals(releaseDate, other.releaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), artist, recordLabel, genre,
                           trackCount, releaseDate);
    }

    /**
     * OCP SOLUTION: Implements Prototype Pattern for CD
     * Creates a deep copy of this CD with all attributes
     * 
     * @return A new CD instance with identical attributes
     */
    @Override
    public Product copy() {
        CD copy = new CD();
        // Copy common attributes using parent helper method
        copyCommonAttributesTo(copy);
        // Copy CD-specific attributes
        copy.setArtist(this.artist);
        copy.setRecordLabel(this.recordLabel);
        copy.setGenre(this.genre);
        copy.setTrackCount(this.trackCount);
        copy.setReleaseDate(this.releaseDate);
        return copy;
    }

}
