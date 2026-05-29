package com.aimsfx.repository.mapper;

import com.aimsfx.model.DVD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * DVDJdbcMapper - JDBC Mapper for DVD products
 * 
 * SOLID PRINCIPLES:
 * - SRP: Only responsible for mapping DVD data to/from database
 * - OCP: Can add new mappers without modifying existing code
 * - LSP: Correctly implements ProductJdbcMapper contract
 * - ISP: Interface is focused and minimal
 * - DIP: Depends on abstraction (Product, not concrete types)
 * 
 * COLUMNS HANDLED (with values):
 * - genre (VARCHAR) - in Book columns section (shared)
 * - release_date (TIMESTAMP) - in CD columns section (shared)
 * - director (VARCHAR)
 * - studio (VARCHAR)
 * - subtitle (VARCHAR)
 * - disc_type (VARCHAR)
 * - duration (INTEGER)
 * 
 * COLUMNS SET TO NULL:
 * - Book columns (except genre): author, publisher, publication_date, pages, language, cover_type
 * - CD columns (except release_date): artist, record_label, track_count
 * - Newspaper columns: issue_number, frequency, editor_in_chief, section
 */
public class DVDJdbcMapper implements ProductJdbcMapper<DVD> {
    
    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, DVD dvd) throws SQLException {
        int idx = startIndex;
        
        // ===== Book columns (7) - SET TO NULL except genre =====
        stmt.setNull(idx++, Types.VARCHAR);    // author
        stmt.setNull(idx++, Types.VARCHAR);    // publisher
        stmt.setNull(idx++, Types.VARCHAR);    // publication_date
        stmt.setNull(idx++, Types.INTEGER);    // pages
        stmt.setNull(idx++, Types.VARCHAR);    // language
        stmt.setNull(idx++, Types.VARCHAR);    // cover_type
        stmt.setString(idx++, dvd.getGenre()); // genre (shared - SET WITH VALUE)
        
        // ===== CD columns (4) - SET TO NULL except release_date =====
        stmt.setNull(idx++, Types.VARCHAR);    // artist
        stmt.setNull(idx++, Types.VARCHAR);    // record_label
        stmt.setNull(idx++, Types.INTEGER);    // track_count
        if (dvd.getReleaseDate() != null) {
            stmt.setTimestamp(idx++, new Timestamp(dvd.getReleaseDate().getTime())); // release_date (shared)
        } else {
            stmt.setNull(idx++, Types.TIMESTAMP);
        }
        
        // ===== DVD columns (5) - SET WITH VALUES =====
        stmt.setString(idx++, dvd.getDirector());
        stmt.setString(idx++, dvd.getStudio());
        stmt.setString(idx++, dvd.getSubtitle());
        stmt.setString(idx++, dvd.getDiscType());
        if (dvd.getDuration() != null) {
            stmt.setInt(idx++, dvd.getDuration());
        } else {
            stmt.setNull(idx++, Types.INTEGER);
        }
        
        // ===== Newspaper columns (4) - SET TO NULL =====
        stmt.setNull(idx++, Types.VARCHAR);    // issue_number
        stmt.setNull(idx++, Types.VARCHAR);    // frequency
        stmt.setNull(idx++, Types.VARCHAR);    // editor_in_chief
        stmt.setNull(idx++, Types.VARCHAR);    // section
        
        return idx;
    }
    
    @Override
    public DVD mapRow(ResultSet rs) throws SQLException {
        DVD dvd = new DVD();
        populateFromResultSet(rs, dvd);
        return dvd;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, DVD dvd) throws SQLException {
        dvd.setDirector(rs.getString("director"));
        dvd.setStudio(rs.getString("studio"));
        dvd.setSubtitle(rs.getString("subtitle"));
        dvd.setDiscType(rs.getString("disc_type"));
        
        int duration = rs.getInt("duration");
        if (!rs.wasNull()) {
            dvd.setDuration(duration);
        }
        
        dvd.setGenre(rs.getString("genre"));
        
        Timestamp releaseDate = rs.getTimestamp("release_date");
        if (releaseDate != null) {
            dvd.setReleaseDate(new java.util.Date(releaseDate.getTime()));
        }
    }
}
