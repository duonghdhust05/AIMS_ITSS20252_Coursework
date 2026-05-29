package com.aimsfx.repository;

import com.aimsfx.model.Track;
import com.aimsfx.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TrackRepository - Repository for CD tracks
 * 
 * DESIGN PATTERN: Repository Pattern
 * PURPOSE: Manages track data persistence
 */
public class TrackRepository {
    
    private final DatabaseConnection dbConnection;
    
    public TrackRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Save multiple tracks for a CD product
     * @param tracks List of tracks to save
     * @return true if all tracks saved successfully
     */
    public boolean saveTracks(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            return true; // Nothing to save
        }
        
        String sql = "INSERT INTO tracks (product_barcode, title, duration) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            for (Track track : tracks) {
                stmt.setString(1, track.getProductBarcode());
                stmt.setString(2, track.getTitle());
                stmt.setInt(3, track.getDuration());
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            
            // Check if all tracks were inserted
            for (int result : results) {
                if (result == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error saving tracks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all tracks for a CD by barcode
     * @param productBarcode CD product barcode
     * @return List of tracks
     */
    public List<Track> findByProductBarcode(String productBarcode) {
        List<Track> tracks = new ArrayList<>();
        String sql = "SELECT * FROM tracks WHERE product_barcode = ? ORDER BY track_id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productBarcode);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Track track = new Track();
                track.setTrackId(rs.getLong("track_id"));
                track.setProductBarcode(rs.getString("product_barcode"));
                track.setTitle(rs.getString("title"));
                track.setDuration(rs.getInt("duration"));
                tracks.add(track);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding tracks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tracks;
    }
    
    /**
     * Delete all tracks for a CD product
     * @param productBarcode CD product barcode
     * @return true if deleted successfully
     */
    public boolean deleteByProductBarcode(String productBarcode) {
        String sql = "DELETE FROM tracks WHERE product_barcode = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productBarcode);
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting tracks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
