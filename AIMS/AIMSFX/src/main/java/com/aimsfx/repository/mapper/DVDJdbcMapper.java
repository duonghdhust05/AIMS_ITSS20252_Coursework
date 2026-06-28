package com.aimsfx.repository.mapper;

import com.aimsfx.model.DVD;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * DVDJdbcMapper - JDBC Mapper for DVD products
 * 
 * DESIGN IMPROVEMENT (JSONB Migration):
 * - Eliminates sparse table columns by using a unified JSONB structure.
 * - This mapper is responsible for serializing specific details into JSON
 *   and deserializing them back from the `attributes` JSONB column.
 */
public class DVDJdbcMapper implements ProductJdbcMapper<DVD> {
    
    private static final Gson gson = new Gson();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, DVD product) throws SQLException {
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
    public DVD mapRow(ResultSet rs) throws SQLException {
        DVD product = new DVD();
        populateFromResultSet(rs, product);
        return product;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, DVD product) throws SQLException {
        String attributesJson = rs.getString("attributes");
        if (attributesJson != null && !attributesJson.isEmpty()) {
            Map<String, Object> details = gson.fromJson(attributesJson, new TypeToken<Map<String, Object>>(){}.getType());
            if (details != null) {
                if (details.containsKey("director") && details.get("director") != null) {
                    product.setDirector(String.valueOf(details.get("director")));
                }
                if (details.containsKey("studio") && details.get("studio") != null) {
                    product.setStudio(String.valueOf(details.get("studio")));
                }
                if (details.containsKey("subtitle") && details.get("subtitle") != null) {
                    product.setSubtitle(String.valueOf(details.get("subtitle")));
                }
                if (details.containsKey("disc_type") && details.get("disc_type") != null) {
                    product.setDiscType(String.valueOf(details.get("disc_type")));
                }
                if (details.containsKey("duration") && details.get("duration") != null) {
                    product.setDuration(((Number) details.get("duration")).intValue());
                }
                if (details.containsKey("genre") && details.get("genre") != null) {
                    product.setGenre(String.valueOf(details.get("genre")));
                }
                if (details.containsKey("release_date") && details.get("release_date") != null) {
                    try {
                        product.setReleaseDate(sdf.parse(String.valueOf(details.get("release_date"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
