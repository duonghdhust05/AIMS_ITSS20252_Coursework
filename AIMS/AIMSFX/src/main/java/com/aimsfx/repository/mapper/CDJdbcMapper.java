package com.aimsfx.repository.mapper;

import com.aimsfx.model.CD;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * CDJdbcMapper - JDBC Mapper for CD products
 * 
 * DESIGN IMPROVEMENT (JSONB Migration):
 * - Eliminates sparse table columns by using a unified JSONB structure.
 * - This mapper is responsible for serializing specific details into JSON
 *   and deserializing them back from the `attributes` JSONB column.
 */
public class CDJdbcMapper implements ProductJdbcMapper<CD> {
    
    private static final Gson gson = new Gson();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public int setAllTypeSpecificColumns(PreparedStatement stmt, int startIndex, CD product) throws SQLException {
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
    public CD mapRow(ResultSet rs) throws SQLException {
        CD product = new CD();
        populateFromResultSet(rs, product);
        return product;
    }
    
    @Override
    public void populateFromResultSet(ResultSet rs, CD product) throws SQLException {
        String attributesJson = rs.getString("attributes");
        if (attributesJson != null && !attributesJson.isEmpty()) {
            Map<String, Object> details = gson.fromJson(attributesJson, new TypeToken<Map<String, Object>>(){}.getType());
            if (details != null) {
                if (details.containsKey("artist") && details.get("artist") != null) {
                    product.setArtist(String.valueOf(details.get("artist")));
                }
                if (details.containsKey("record_label") && details.get("record_label") != null) {
                    product.setRecordLabel(String.valueOf(details.get("record_label")));
                }
                if (details.containsKey("genre") && details.get("genre") != null) {
                    product.setGenre(String.valueOf(details.get("genre")));
                }
                if (details.containsKey("track_count") && details.get("track_count") != null) {
                    product.setTrackCount(((Number) details.get("track_count")).intValue());
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
