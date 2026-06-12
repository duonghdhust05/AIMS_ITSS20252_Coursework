-- =========================================================================================================
-- DATABASE OPTIMIZATION: GIN INDEXES FOR AUTOCOMPLETE & SEARCH
-- =========================================================================================================
-- PREVIOUS PROBLEMS:
-- 1. Full Table Scans: The query `LOWER(title) LIKE LOWER('%...%')` forces PostgreSQL to scan 
--    every single row in the products table because standard B-Tree indexes cannot index leading wildcards.
-- 2. As the database grows to hundreds of thousands of records, autocomplete becomes sluggish.
--
-- DETAILED SOLUTION & IMPLEMENTATION:
-- 1. Enabled the `pg_trgm` extension which supports trigram matching.
-- 2. Created GIN (Generalized Inverted Index) indexes on `title`, `category`, and `barcode`.
-- 
-- EXPECTED RESULTS:
-- Search queries using `LIKE '%...%'` or `ILIKE` will now use the index, reducing query times 
-- from hundreds of milliseconds down to ~1-5ms even on large datasets.
-- =========================================================================================================

-- Enable the trigram extension (required for indexing LIKE '%word%')
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN index for product title
CREATE INDEX IF NOT EXISTS idx_products_title_trgm 
ON products USING gin (title gin_trgm_ops);

-- Create GIN index for product category
CREATE INDEX IF NOT EXISTS idx_products_category_trgm 
ON products USING gin (category gin_trgm_ops);

-- Create GIN index for product barcode (if searching by partial barcode is needed)
CREATE INDEX IF NOT EXISTS idx_products_barcode_trgm 
ON products USING gin (barcode gin_trgm_ops);

-- =========================================================================================================
-- DATABASE OPTIMIZATION: B-TREE INDEX FOR PRICE FILTERING
-- =========================================================================================================
-- CHANGELOG: Added B-Tree index on current_price to support fast range queries (>=, <) 
-- when combining search keywords with price filters.
CREATE INDEX IF NOT EXISTS idx_products_current_price 
ON products (current_price);
