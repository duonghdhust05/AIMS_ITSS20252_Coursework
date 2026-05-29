package com.aimsfx.repository.mapper;

import com.aimsfx.model.CD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * CDJdbcMapper - JDBC Mapper for CD products
 * 
 * SOLID PRINCIPLES:
 * - SRP: Only responsible for mapping CD data to/from database
 * - OCP: Can add new mappers without modifying existing code
 * - LSP: Correctly implements ProductJdbcMapper contract
 * - ISP: Interface is focused and minimal
 * - DIP: Depends on abstraction (Product, not concrete types)
 * 
 * COLUMNS HANDLED (with values):
 * - genre (VARCHAR) - in Book columns section (shared)
 * - artist (VARCHAR)
 * - record_label (VARCHAR)
 * - track_count (INTEGER)
 * - release_date (TIMESTAMP)
 * 
 * COLUMNS SET TO NULL:
 * - Book columns (except genre): author, publisher, publication_date, pages, language, cover_type
 * - DVD columns: director, studio, subtitle, disc_type, duration
 * - Newspaper columns: issue_number, frequency, editor_in_chief, section
 */
public class CDJdbcMapper implements ProductJdbcMapper<CD> {
    
    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, CD cd) throws SQLException {
        int idx = startIndex;
        
        // ===== Book columns (7) - SET TO NULL except genre =====
        stmt.setNull(idx++, Types.VARCHAR);    // author
        stmt.setNull(idx++, Types.VARCHAR);    // publisher
        stmt.setNull(idx++, Types.VARCHAR);    // publication_date
        stmt.setNull(idx++, Types.INTEGER);    // pages
        stmt.setNull(idx++, Types.VARCHAR);    // language
        stmt.setNull(idx++, Types.VARCHAR);    // cover_type
        stmt.setString(idx++, cd.getGenre());  // genre (shared - SET WITH VALUE)
        
        // ===== CD columns (4) - SET WITH VALUES =====
        stmt.setString(idx++, cd.getArtist());
        stmt.setString(idx++, cd.getRecordLabel());
        if (cd.getTrackCount() != null) {
            stmt.setInt(idx++, cd.getTrackCount());
        } else {
            stmt.setNull(idx++, Types.INTEGER);
        }
        if (cd.getReleaseDate() != null) {
            stmt.setTimestamp(idx++, new Timestamp(cd.getReleaseDate().getTime()));
        } else {
            stmt.setNull(idx++, Types.TIMESTAMP);
        }
        
        // ===== DVD columns (5) - SET TO NULL =====
        stmt.setNull(idx++, Types.VARCHAR);    // director
        stmt.setNull(idx++, Types.VARCHAR);    // studio
        stmt.setNull(idx++, Types.VARCHAR);    // subtitle
        stmt.setNull(idx++, Types.VARCHAR);    // disc_type
        stmt.setNull(idx++, Types.INTEGER);    // duration
        
        // ===== Newspaper columns (4) - SET TO NULL =====
        stmt.setNull(idx++, Types.VARCHAR);    // issue_number
        stmt.setNull(idx++, Types.VARCHAR);    // frequency
        stmt.setNull(idx++, Types.VARCHAR);    // editor_in_chief
        stmt.setNull(idx++, Types.VARCHAR);    // section
        
        return idx;
    }
    
    @Override
    public CD mapRow(ResultSet rs) throws SQLException {
        CD cd = new CD();
        populateFromResultSet(rs, cd);
        return cd;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, CD cd) throws SQLException {
        cd.setArtist(rs.getString("artist"));
        cd.setRecordLabel(rs.getString("record_label"));
        cd.setGenre(rs.getString("genre"));
        
        int trackCount = rs.getInt("track_count");
        if (!rs.wasNull()) {
            cd.setTrackCount(trackCount);
        }
        
        Timestamp releaseDate = rs.getTimestamp("release_date");
        if (releaseDate != null) {
            cd.setReleaseDate(new java.util.Date(releaseDate.getTime()));
        }
    }
}
