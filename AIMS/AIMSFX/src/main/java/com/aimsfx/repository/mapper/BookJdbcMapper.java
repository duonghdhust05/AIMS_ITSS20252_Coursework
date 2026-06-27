package com.aimsfx.repository.mapper;

import com.aimsfx.model.Book;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * BookJdbcMapper - JDBC Mapper for Book products
 * 
 * DESIGN IMPROVEMENT (JSONB Migration):
 * - Eliminates sparse table columns by using a unified JSONB structure.
 * - This mapper is responsible for serializing specific details into JSON
 * and deserializing them back from the `attributes` JSONB column.
 */
public class BookJdbcMapper implements ProductJdbcMapper<Book> {

    private static final Gson gson = new Gson();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, Book product) throws SQLException {
        Map<String, Object> details = product.getSpecificDetail();
        // Format dates before serialization if needed
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if (entry.getValue() instanceof java.util.Date) {
                entry.setValue(sdf.format((java.util.Date) entry.getValue()));
            }
        }
        stmt.setString(startIndex, gson.toJson(details));
        return startIndex + 1;
    }

    @Override
    public Book mapRow(ResultSet rs) throws SQLException {
        Book product = new Book();
        populateFromResultSet(rs, product);
        return product;
    }

    @Override
    public void populateFromResultSet(ResultSet rs, Book product) throws SQLException {
        String attributesJson = rs.getString("attributes");
        if (attributesJson != null && !attributesJson.isEmpty()) {
            Map<String, Object> details = gson.fromJson(attributesJson, new TypeToken<Map<String, Object>>() {
            }.getType());
            if (details != null) {
                if (details.containsKey("author") && details.get("author") != null) {
                    product.setAuthor(String.valueOf(details.get("author")));
                }
                if (details.containsKey("publisher") && details.get("publisher") != null) {
                    product.setPublisher(String.valueOf(details.get("publisher")));
                }
                if (details.containsKey("publication_date") && details.get("publication_date") != null) {
                    product.setPublicationDate(String.valueOf(details.get("publication_date")));
                }
                if (details.containsKey("pages") && details.get("pages") != null) {
                    product.setPages(((Number) details.get("pages")).intValue());
                }
                if (details.containsKey("language") && details.get("language") != null) {
                    product.setLanguage(String.valueOf(details.get("language")));
                }
                if (details.containsKey("cover_type") && details.get("cover_type") != null) {
                    product.setCoverType(String.valueOf(details.get("cover_type")));
                }
                if (details.containsKey("genre") && details.get("genre") != null) {
                    product.setGenre(String.valueOf(details.get("genre")));
                }
            }
        }
    }
}
