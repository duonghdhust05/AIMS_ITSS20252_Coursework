package com.aimsfx.repository.mapper;

import com.aimsfx.model.Newspaper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * NewspaperJdbcMapper - JDBC Mapper for Newspaper products
 * 
 * DESIGN IMPROVEMENT (JSONB Migration):
 * - Eliminates sparse table columns by using a unified JSONB structure.
 * - This mapper is responsible for serializing specific details into JSON
 *   and deserializing them back from the `attributes` JSONB column.
 */
public class NewspaperJdbcMapper implements ProductJdbcMapper<Newspaper> {
    
    private static final Gson gson = new Gson();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, Newspaper product) throws SQLException {
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
    public Newspaper mapRow(ResultSet rs) throws SQLException {
        Newspaper product = new Newspaper();
        populateFromResultSet(rs, product);
        return product;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, Newspaper product) throws SQLException {
        String attributesJson = rs.getString("attributes");
        if (attributesJson != null && !attributesJson.isEmpty()) {
            Map<String, Object> details = gson.fromJson(attributesJson, new TypeToken<Map<String, Object>>(){}.getType());
            if (details != null) {
                if (details.containsKey("issn") && details.get("issn") != null) {
                    product.setIssn(String.valueOf(details.get("issn")));
                }
                if (details.containsKey("frequency") && details.get("frequency") != null) {
                    product.setFrequency(String.valueOf(details.get("frequency")));
                }
                if (details.containsKey("editor_in_chief") && details.get("editor_in_chief") != null) {
                    product.setEditorInChief(String.valueOf(details.get("editor_in_chief")));
                }
                if (details.containsKey("section") && details.get("section") != null) {
                    product.setSection(String.valueOf(details.get("section")));
                }
                if (details.containsKey("publisher") && details.get("publisher") != null) {
                    product.setPublisher(String.valueOf(details.get("publisher")));
                }
                if (details.containsKey("publication_date") && details.get("publication_date") != null) {
                    try {
                        product.setPublicationDate(sdf.parse(String.valueOf(details.get("publication_date"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (details.containsKey("language") && details.get("language") != null) {
                    product.setLanguage(String.valueOf(details.get("language")));
                }
            }
        }
    }
}
