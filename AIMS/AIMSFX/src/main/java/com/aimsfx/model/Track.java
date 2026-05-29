package com.aimsfx.model;

/**
 * Track - Represents a music track in a CD
 * 
 * DESIGN NOTE:
 * - Uses productBarcode instead of productId for soft reference
 * - No foreign key constraint in database (application manages integrity)
 * - Flexible design allows tracks to be loaded independently
 * 
 * RELATIONSHIP:
 * - Many tracks belong to one CD (via productBarcode)
 * - CD has trackCount field summarizing total tracks
 * - Individual tracks provide detailed information (title, duration)
 */
public class Track {
    
    private Long trackId;           // Unique identifier
    private String productBarcode;  // Soft reference to CD (no FK constraint)
    private String title;           // Track title
    private Integer duration;       // Duration in seconds

    public Track() {
    }

    /**
     * Full constructor for creating Track with all attributes
     * 
     * COUPLING: DATA COUPLING - receives primitive types and simple objects
     * 
     * @param trackId Unique identifier
     * @param productBarcode Barcode of the CD this track belongs to
     * @param title Track title
     * @param duration Duration in seconds
     */
    public Track(Long trackId, String productBarcode, String title, Integer duration) {
        this.trackId = trackId;
        this.productBarcode = productBarcode;
        this.title = title;
        this.duration = duration;
    }

    // -------- Getter and Setter Methods --------

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getFormattedDuration() {
        if (duration == null || duration < 0) {
            return "00:00";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return "Track{" +
                "trackId=" + trackId +
                ", productBarcode='" + productBarcode + '\'' +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                " (" + getFormattedDuration() + ")" +
                '}';
    }
}
