package com.aimsfx.repository.mapper;

import com.aimsfx.model.Book;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * BookJdbcMapper - JDBC Mapper for Book products
 * 
 * SOLID PRINCIPLES:
 * - SRP: Only responsible for mapping Book data to/from database
 * - OCP: Can add new mappers without modifying existing code
 * - LSP: Correctly implements ProductJdbcMapper contract
 * - ISP: Interface is focused and minimal
 * - DIP: Depends on abstraction (Product, not concrete types)
 * 
 * COLUMNS HANDLED (with values):
 * - author (VARCHAR)
 * - publisher (VARCHAR)
 * - publication_date (VARCHAR)
 * - pages (INTEGER)
 * - language (VARCHAR)
 * - cover_type (VARCHAR)
 * - genre (VARCHAR)
 * 
 * COLUMNS SET TO NULL:
 * - CD columns: artist, record_label, track_count, release_date
 * - DVD columns: director, studio, subtitle, disc_type, duration
 * - Newspaper columns: issue_number, frequency, editor_in_chief, section
 */
public class BookJdbcMapper implements ProductJdbcMapper<Book> {
    
    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, Book book) throws SQLException {
        int idx = startIndex;
        
        // ===== Book columns (7) - SET WITH VALUES =====
        stmt.setString(idx++, book.getAuthor());
        stmt.setString(idx++, book.getPublisher());
        stmt.setString(idx++, book.getPublicationDate());
        if (book.getPages() != null) {
            stmt.setInt(idx++, book.getPages());
        } else {
            stmt.setNull(idx++, Types.INTEGER);
        }
        stmt.setString(idx++, book.getLanguage());
        stmt.setString(idx++, book.getCoverType());
        stmt.setString(idx++, book.getGenre());
        
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
        
        // ===== Newspaper columns (4) - SET TO NULL =====
        stmt.setNull(idx++, Types.VARCHAR);    // issue_number
        stmt.setNull(idx++, Types.VARCHAR);    // frequency
        stmt.setNull(idx++, Types.VARCHAR);    // editor_in_chief
        stmt.setNull(idx++, Types.VARCHAR);    // section
        
        return idx;
    }
    
    @Override
    public Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        populateFromResultSet(rs, book);
        return book;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, Book book) throws SQLException {
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationDate(rs.getString("publication_date"));
        
        int pages = rs.getInt("pages");
        if (!rs.wasNull()) {
            book.setPages(pages);
        }
        
        book.setLanguage(rs.getString("language"));
        book.setCoverType(rs.getString("cover_type"));
        book.setGenre(rs.getString("genre"));
    }
}
