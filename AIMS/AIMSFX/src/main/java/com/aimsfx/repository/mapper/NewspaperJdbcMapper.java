package com.aimsfx.repository.mapper;

import com.aimsfx.model.Newspaper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * NewspaperJdbcMapper - JDBC Mapper for Newspaper products
 * 
 * SOLID PRINCIPLES:
 * - SRP: Only responsible for mapping Newspaper data to/from database
 * - OCP: Can add new mappers without modifying existing code
 * - LSP: Correctly implements ProductJdbcMapper contract
 * - ISP: Interface is focused and minimal
 * - DIP: Depends on abstraction (Product, not concrete types)
 * 
 * COLUMNS HANDLED (with values):
 * - publisher (VARCHAR) - in Book columns section (shared)
 * - language (VARCHAR) - in Book columns section (shared)
 * - issue_number (VARCHAR)
 * - frequency (VARCHAR)
 * - editor_in_chief (VARCHAR)
 * - section (VARCHAR)
 * 
 * COLUMNS SET TO NULL:
 * - Book columns (except publisher, language): author, publication_date, pages, cover_type, genre
 * - CD columns: artist, record_label, track_count, release_date
 * - DVD columns: director, studio, subtitle, disc_type, duration
 */
public class NewspaperJdbcMapper implements ProductJdbcMapper<Newspaper> {
    
    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, Newspaper newspaper) throws SQLException {
        int idx = startIndex;
        
        // ===== Book columns (7) - SET TO NULL except publisher, language =====
        stmt.setNull(idx++, Types.VARCHAR);            // author
        stmt.setString(idx++, newspaper.getPublisher()); // publisher (shared - SET WITH VALUE)
        stmt.setNull(idx++, Types.VARCHAR);            // publication_date (Book uses VARCHAR, we skip)
        stmt.setNull(idx++, Types.INTEGER);            // pages
        stmt.setString(idx++, newspaper.getLanguage()); // language (shared - SET WITH VALUE)
        stmt.setNull(idx++, Types.VARCHAR);            // cover_type
        stmt.setNull(idx++, Types.VARCHAR);            // genre
        
        // ===== CD columns (4) - SET TO NULL =====
        stmt.setNull(idx++, Types.VARCHAR);    // artist
        stmt.setNull(idx++, Types.VARCHAR);    // record_label
        stmt.setNull(idx++, Types.INTEGER);    // track_count
        stmt.setNull(idx++, Types.TIMESTAMP);  // release_date
        
        // ===== DVD columns (5) - SET TO NULL =====
        stmt.setNull(idx++, Types.VARCHAR);    // director
        stmt.setNull(idx++, Types.VARCHAR);    // studio
        stmt.setNull(idx++, Types.VARCHAR);    // subtitle
        stmt.setNull(idx++, Types.VARCHAR);    // disc_type
        stmt.setNull(idx++, Types.INTEGER);    // duration
        
        // ===== Newspaper columns (4) - SET WITH VALUES =====
        stmt.setString(idx++, newspaper.getIssn());
        stmt.setString(idx++, newspaper.getFrequency());
        stmt.setString(idx++, newspaper.getEditorInChief());
        stmt.setString(idx++, newspaper.getSection());
        
        return idx;
    }
    
    @Override
    public Newspaper mapRow(ResultSet rs) throws SQLException {
        Newspaper newspaper = new Newspaper();
        populateFromResultSet(rs, newspaper);
        return newspaper;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, Newspaper newspaper) throws SQLException {
        newspaper.setIssn(rs.getString("issn"));
        newspaper.setFrequency(rs.getString("frequency"));
        newspaper.setEditorInChief(rs.getString("editor_in_chief"));
        newspaper.setPublisher(rs.getString("publisher"));
        
        Timestamp publicationDate = rs.getTimestamp("publication_date");
        if (publicationDate != null) {
            newspaper.setPublicationDate(new java.util.Date(publicationDate.getTime()));
        }
        
        newspaper.setLanguage(rs.getString("language"));
        newspaper.setSection(rs.getString("section"));
    }
}
