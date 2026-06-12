-- AIMS Database Schema
-- PostgreSQL database schema for AIMS (Advanced Inventory Management System)

-- Drop existing tables if they exist (for fresh installation)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS tracks CASCADE;
DROP TABLE IF EXISTS stock_change_log CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS user_delete_count CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS order_action_logs CASCADE;

-- Users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hashed password
    roles VARCHAR(100) NOT NULL,  -- Multiple roles comma-separated: PRODUCT_MANAGER, ADMINISTRATOR
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, BLOCKED
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User delete count tracking (for daily deletion limits)
CREATE TABLE user_delete_count (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    day_id DATE NOT NULL,
    delete_count INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, day_id)
);

-- Create default administrator account (password: admin123)
-- Password is hashed using SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO users (username, password, roles, status, full_name) 
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRATOR,PRODUCT_MANAGER', 'ACTIVE', 'System Administrator'),
       ('pm', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'PRODUCT_MANAGER', 'ACTIVE', 'Product Manager'),
       ('admin_only', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRATOR', 'ACTIVE', 'Administrator Only');

-- Products table (single table inheritance - all product types in one table)
-- DESIGN IMPROVEMENT: Single table inheritance pattern
-- - Database auto-generates IDs (SERIAL) - no application-side ID management
-- - All product-specific fields in one table with nullable columns
-- - Eliminates joins for basic queries, simplifies persistence logic
-- - Trade-off: Some nullable columns, but much simpler architecture
-- - History tracking: barcode is NOT unique (multiple versions share same barcode)
--   Only (barcode, is_current=true) combination should be unique
CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    barcode VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    original_price DOUBLE PRECISION NOT NULL,
    current_price DOUBLE PRECISION NOT NULL,
    description VARCHAR(255),
    weight DOUBLE PRECISION,
    dimensions VARCHAR(255),
    stock INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(255) DEFAULT 'available' CHECK (status IN ('available', 'deactivated')),
    vat_rate DOUBLE PRECISION DEFAULT 0.1,
    product_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expired_date TIMESTAMP,
    is_current BOOLEAN NOT NULL DEFAULT true,

    -- Add Check Constraint to prevent negative stock
    CONSTRAINT check_stock_positive CHECK (stock >= 0),

    -- Book-specific fields
    author VARCHAR(255),
    publisher VARCHAR(255),
    publication_date VARCHAR(255),
    language VARCHAR(255),
    pages INTEGER,
    cover_type VARCHAR(255),
    genre VARCHAR(255),
    
    -- CD-specific fields
    artist VARCHAR(255),
    record_label VARCHAR(255),
    track_count INTEGER,
    release_date TIMESTAMP,
    
    -- DVD-specific fields
    director VARCHAR(255),
    studio VARCHAR(255),
    subtitle VARCHAR(255),
    disc_type VARCHAR(255),
    duration INTEGER,
    
    -- Newspaper-specific fields
    issn VARCHAR(255),
    frequency VARCHAR(255),
    editor_in_chief VARCHAR(255),
    section TEXT
);

CREATE TABLE tracks (
    track_id SERIAL PRIMARY KEY,
    product_barcode VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    duration INTEGER NOT NULL
);

CREATE TABLE stock_change_log (
    id SERIAL PRIMARY KEY,
    barcode VARCHAR(255) NOT NULL,
    from_stock INTEGER NOT NULL,
    to_stock INTEGER NOT NULL,
    change_reason VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Partial unique index: only one current version per barcode
CREATE UNIQUE INDEX unique_current_barcode ON products (barcode) 
    WHERE (is_current = true);

-- Carts table
CREATE TABLE carts (
    cart_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart items table
CREATE TABLE cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id INTEGER NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id)
);

-- Orders table
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE SET NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_name VARCHAR(100),
    delivery_phone VARCHAR(20),
    delivery_email VARCHAR(100),
    delivery_address TEXT NOT NULL,
    delivery_province VARCHAR(100),
    delivery_ward VARCHAR(100),
    delivery_instructions TEXT,
    shipping_fee DECIMAL(15,2) DEFAULT 0,
    rush_delivery BOOLEAN DEFAULT FALSE,
    rush_fee DECIMAL(15,2) DEFAULT 0,
    subtotal DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(50) DEFAULT 'Pending',
    order_status VARCHAR(50) DEFAULT 'Processing',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancel_reason VARCHAR(255)
);

-- Order items table
CREATE TABLE order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(product_id) ON DELETE SET NULL,
    product_title VARCHAR(255) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL
);

CREATE TABLE order_action_logs (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(order_id),
    action VARCHAR(50) NOT NULL,
    reason TEXT, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table (for VietQR and PayPal payments)
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,  -- 'VIETQR' or 'PAYPAL'
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED, CANCELLED
    currency VARCHAR(20) DEFAULT 'VND',   -- 'VND' or 'USD'
    failure_reason VARCHAR(255),
    
    -- VietQR callback fields (all required fields from Transaction Sync API)
    external_transaction_id VARCHAR(100), -- VietQR transactionid or PayPal Order ID
    bank_account VARCHAR(50),             -- bankaccount - bank account used for payment
    trans_type VARCHAR(10),               -- transType - D (debit) or C (credit)
    content VARCHAR(255),                 -- content - transfer message
    transaction_time TIMESTAMP,           -- transactiontime - when payment was made
    reference_number VARCHAR(100),        -- referencenumber - bank reference code
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Add transaction_id reference to orders table
ALTER TABLE orders ADD COLUMN transaction_id INTEGER REFERENCES transactions(transaction_id);

-- Indexes for better query performance
CREATE INDEX idx_transactions_order ON transactions(order_id);
CREATE INDEX idx_transactions_external ON transactions(external_transaction_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_method ON transactions(payment_method);
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_type ON products(product_type);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_current ON products(is_current);
CREATE INDEX idx_products_barcode_current ON products(barcode, is_current);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_user_delete_count_lookup ON user_delete_count(user_id, day_id);

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

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for automatic updated_at updates
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_delete_count_updated_at BEFORE UPDATE ON user_delete_count
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================================
-- Sample Data
-- ================================================================================

-- Sample Books (using single table inheritance)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     author, publisher, publication_date, language, pages, cover_type, genre)
VALUES
    ('BC00141231342', 'Clean Code', 'Programming', 450000.0, 450000.0,
     'A handbook of agile software craftsmanship', 0.5, '20x15x3', 50, 'available', 0.1, 'BOOK',
     'Robert C. Martin', 'Prentice Hall', '2008-08-01', 'English', 388, 'Paperback', 'Software Engineering'),
    ('BC000002', 'Design Patterns', 'Programming', 550000.0, 550000.0,
     'Elements of reusable object-oriented software', 0.6, '23x18x3', 40, 'available', 0.1, 'BOOK',
     'Gang of Four', 'Addison-Wesley', '1994-10-31', 'English', 395, 'Hardcover', 'Software Engineering'),
    ('BC000003', 'Effective Java', 'Programming', 520000.0, 520000.0,
     'Best practices for the Java platform', 0.55, '21x16x3', 60, 'available', 0.1, 'BOOK',
     'Joshua Bloch', 'Addison-Wesley', '2017-12-27', 'English', 412, 'Paperback', 'Programming'),
    ('BC000004', 'The Pragmatic Programmer', 'Programming', 480000.0, 480000.0,
     'Your journey to mastery', 0.52, '22x17x3', 45, 'available', 0.1, 'BOOK',
     'Andrew Hunt', 'Addison-Wesley', '2019-09-13', 'English', 352, 'Hardcover', 'Software Engineering');

-- Sample CDs (using single table inheritance)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     artist, record_label, genre, track_count, release_date)
VALUES
    ('CD000001', 'Greatest Hits Album', 'Music', 120000.0, 120000.0,
     'Collection of popular songs', 0.1, '14x12x1', 75, 'available', 0.1, 'CD',
     'Various Artists', 'Universal Music', 'Pop', 15, '2024-01-15'),
    ('CD000002', 'Classical Symphony', 'Music', 150000.0, 150000.0,
     'Best classical music collection', 0.1, '14x12x1', 50, 'available', 0.1, 'CD',
     'London Symphony Orchestra', 'Decca Records', 'Classical', 12, '2023-06-20'),
    ('CD000003', 'Jazz Essentials', 'Music', 130000.0, 130000.0,
     'Essential jazz recordings', 0.1, '14x12x1', 60, 'available', 0.1, 'CD',
     'Miles Davis', 'Columbia Records', 'Jazz', 10, '2023-03-10');

-- Sample DVDs (using single table inheritance)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     director, studio, subtitle, disc_type, duration, genre, release_date)
VALUES
    ('DVD00001', 'The Matrix Collection', 'Movie', 299000.0, 299000.0,
     'Complete Matrix trilogy', 0.2, '19x14x2', 25, 'available', 0.1, 'DVD',
     'Wachowski Sisters', 'Warner Bros', 'Vietnamese', 'DVD-9', 467, 'Sci-Fi', '2022-05-10'),
    ('DVD00002', 'The Lord of the Rings Trilogy', 'Movie', 450000.0, 450000.0,
     'Extended edition trilogy', 0.3, '19x14x3', 30, 'available', 0.1, 'DVD',
     'Peter Jackson', 'New Line Cinema', 'Vietnamese, Spanish', 'Blu-ray', 682, 'Fantasy', '2021-12-15'),
    ('DVD00003', 'Inception', 'Movie', 199000.0, 199000.0,
     'Mind-bending thriller', 0.15, '19x14x1', 40, 'available', 0.1, 'DVD',
     'Christopher Nolan', 'Warner Bros', 'Vietnamese', 'DVD-9', 148, 'Thriller', '2023-08-20');

-- Sample Newspapers (using single table inheritance)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     issn, frequency, editor_in_chief, publisher, publication_date, language, section)
VALUES
    ('NP000001', 'Daily News', 'Newspaper', 15000.0, 15000.0,
     'Daily newspaper', 0.2, '40x30x0.5', 500, 'available', 0.1, 'NEWSPAPER',
     '2026-001', 'Daily', 'Nguyen Van A', 'News Corp Vietnam', '2026-01-09', 'Vietnamese', 'Politics, Economy, Sports, Entertainment'),
    ('NP000002', 'Tech Weekly', 'Newspaper', 25000.0, 25000.0,
     'Weekly technology news', 0.3, '40x30x1', 400, 'available', 0.1, 'NEWSPAPER',
     'W01-2026', 'Weekly', 'Tran Thi B', 'Tech Media Group', '2026-01-05', 'Vietnamese, English', 'Technology, Innovation, Startups');

-- Sample Tracks for CDs
INSERT INTO tracks (product_barcode, title, duration)
VALUES
    -- Tracks for CD000001: Greatest Hits Album
    ('CD000001', 'Summer Nights', 240),
    ('CD000001', 'Dancing Queen', 230),
    ('CD000001', 'Sweet Dreams', 215),
    ('CD000001', 'Eternal Love', 265),
    ('CD000001', 'Midnight City', 220),
    ('CD000001', 'Golden Hour', 255),
    ('CD000001', 'Hearts on Fire', 210),
    ('CD000001', 'Starlight', 245),
    ('CD000001', 'Ocean Waves', 230),
    ('CD000001', 'Thunder Road', 275),
    ('CD000001', 'Moonlight Serenade', 235),
    ('CD000001', 'Crystal Clear', 200),
    ('CD000001', 'Rainbow Dreams', 225),
    ('CD000001', 'Forever Young', 260),
    ('CD000001', 'Silent Echo', 215),
    
    -- Tracks for CD000002: Classical Symphony
    ('CD000002', 'Symphony No. 5 - Movement I', 420),
    ('CD000002', 'Symphony No. 5 - Movement II', 380),
    ('CD000002', 'Symphony No. 5 - Movement III', 310),
    ('CD000002', 'Symphony No. 5 - Movement IV', 450),
    ('CD000002', 'Piano Concerto No. 21 - Andante', 390),
    ('CD000002', 'The Four Seasons - Spring', 350),
    ('CD000002', 'The Four Seasons - Summer', 340),
    ('CD000002', 'The Four Seasons - Autumn', 360),
    ('CD000002', 'The Four Seasons - Winter', 355),
    ('CD000002', 'Moonlight Sonata', 420),
    ('CD000002', 'Canon in D Major', 310),
    ('CD000002', 'Air on G String', 330),
    
    -- Tracks for CD000003: Jazz Essentials
    ('CD000003', 'So What', 544),
    ('CD000003', 'Freddie Freeloader', 588),
    ('CD000003', 'Blue in Green', 324),
    ('CD000003', 'All Blues', 691),
    ('CD000003', 'Flamenco Sketches', 563),
    ('CD000003', 'Round Midnight', 354),
    ('CD000003', 'Take Five', 324),
    ('CD000003', 'My Funny Valentine', 372),
    ('CD000003', 'Autumn Leaves', 426),
    ('CD000003', 'Summertime', 390);

-- More Mock Data (50 additional products)
-- Books (15 products)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     author, publisher, publication_date, language, pages, cover_type, genre)
VALUES
    ('BC100001', 'A Brief History of Time', 'Science', 250000.0, 250000.0, 'Stephen Hawkings book on cosmology', 0.4, '21x14x2', 100, 'available', 0.1, 'BOOK', 'Stephen Hawking', 'Bantam Books', '1988-03-01', 'English', 256, 'Paperback', 'Cosmology'),
    ('BC100002', 'Introduction to Algorithms', 'Programming', 1200000.0, 1200000.0, 'Classic textbook on algorithms', 1.8, '25x20x6', 30, 'available', 0.1, 'BOOK', 'Thomas H. Cormen', 'MIT Press', '2009-07-31', 'English', 1292, 'Hardcover', 'Computer Science'),
    ('BC100003', 'To Kill a Mockingbird', 'Fiction', 180000.0, 180000.0, 'Harper Lee classic novel', 0.35, '20x13x2', 80, 'available', 0.1, 'BOOK', 'Harper Lee', 'J. B. Lippincott & Co.', '1960-07-11', 'English', 281, 'Paperback', 'Classic'),
    ('BC100004', '1984', 'Fiction', 150000.0, 150000.0, 'George Orwell dystopian masterpiece', 0.3, '20x13x2', 120, 'available', 0.1, 'BOOK', 'George Orwell', 'Secker & Warburg', '1949-06-08', 'English', 328, 'Paperback', 'Dystopian'),
    ('BC100005', 'The Great Gatsby', 'Fiction', 160000.0, 160000.0, 'F. Scott Fitzgerald novel', 0.32, '20x13x2', 90, 'available', 0.1, 'BOOK', 'F. Scott Fitzgerald', 'Charles Scribners Sons', '1925-04-10', 'English', 180, 'Paperback', 'Classic'),
    ('BC100006', 'Sapiens', 'History', 350000.0, 350000.0, 'A brief history of humankind', 0.5, '22x15x3', 150, 'available', 0.1, 'BOOK', 'Yuval Noah Harari', 'Harper', '2015-02-10', 'English', 512, 'Paperback', 'History'),
    ('BC100007', 'Thinking, Fast and Slow', 'Psychology', 280000.0, 280000.0, 'Daniel Kahnemans work on psychology', 0.6, '22x15x4', 70, 'available', 0.1, 'BOOK', 'Daniel Kahneman', 'Farrar, Straus and Giroux', '2011-10-25', 'English', 499, 'Paperback', 'Psychology'),
    ('BC100008', 'The Hobbit', 'Fantasy', 220000.0, 220000.0, 'J.R.R. Tolkien fantasy classic', 0.4, '20x13x3', 85, 'available', 0.1, 'BOOK', 'J.R.R. Tolkien', 'George Allen & Unwin', '1937-09-21', 'English', 310, 'Hardcover', 'Fantasy'),
    ('BC100009', 'Atomic Habits', 'Self-help', 200000.0, 200000.0, 'An easy way to build good habits', 0.38, '21x14x2', 200, 'available', 0.1, 'BOOK', 'James Clear', 'Avery', '2018-10-16', 'English', 320, 'Paperback', 'Self-improvement'),
    ('BC100010', 'The Catcher in the Rye', 'Fiction', 170000.0, 170000.0, 'J.D. Salinger classic novel', 0.3, '20x13x2', 65, 'available', 0.1, 'BOOK', 'J.D. Salinger', 'Little, Brown and Company', '1951-07-16', 'English', 277, 'Paperback', 'Classic'),
    ('BC100011', 'Deep Work', 'Self-help', 190000.0, 190000.0, 'Rules for focused success in a distracted world', 0.35, '21x14x2', 110, 'available', 0.1, 'BOOK', 'Cal Newport', 'Grand Central Publishing', '2016-01-05', 'English', 304, 'Paperback', 'Productivity'),
    ('BC100012', 'Dune', 'Sci-Fi', 290000.0, 290000.0, 'Frank Herbert science fiction epic', 0.7, '23x15x4', 95, 'available', 0.1, 'BOOK', 'Frank Herbert', 'Chilton Books', '1965-08-01', 'English', 607, 'Paperback', 'Sci-Fi'),
    ('BC100013', 'Zero to One', 'Business', 210000.0, 210000.0, 'Notes on startups and how to build the future', 0.3, '20x14x2', 130, 'available', 0.1, 'BOOK', 'Peter Thiel', 'Crown Business', '2014-09-16', 'English', 224, 'Hardcover', 'Business'),
    ('BC100014', 'Principles', 'Business', 450000.0, 450000.0, 'Ray Dalio life and work principles', 0.9, '24x16x4', 50, 'available', 0.1, 'BOOK', 'Ray Dalio', 'Simon & Schuster', '2017-09-19', 'English', 592, 'Hardcover', 'Management'),
    ('BC100015', 'Guns, Germs, and Steel', 'History', 320000.0, 320000.0, 'Jared Diamond history of societies', 0.55, '21x14x3', 45, 'available', 0.1, 'BOOK', 'Jared Diamond', 'W. W. Norton & Company', '1997-03-01', 'English', 480, 'Paperback', 'History');

-- CDs (15 products)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     artist, record_label, genre, track_count, release_date)
VALUES
    ('CD100001', 'Thriller', 'Music', 180000.0, 180000.0, 'Michael Jackson classic pop album', 0.1, '14x12x1', 100, 'available', 0.1, 'CD', 'Michael Jackson', 'Epic Records', 'Pop', 9, '1982-11-30'),
    ('CD100002', 'Back in Black', 'Music', 170000.0, 170000.0, 'AC/DC rock album', 0.1, '14x12x1', 80, 'available', 0.1, 'CD', 'AC/DC', 'Albert Productions', 'Rock', 10, '1980-07-25'),
    ('CD100003', 'The Dark Side of the Moon', 'Music', 200000.0, 200000.0, 'Pink Floyd progressive rock', 0.1, '14x12x1', 90, 'available', 0.1, 'CD', 'Pink Floyd', 'Harvest Records', 'Rock', 10, '1973-03-01'),
    ('CD100004', 'Come Away With Me', 'Music', 150000.0, 150000.0, 'Norah Jones jazz pop', 0.1, '14x12x1', 70, 'available', 0.1, 'CD', 'Norah Jones', 'Blue Note', 'Jazz', 14, '2002-02-26'),
    ('CD100005', 'Abbey Road', 'Music', 190000.0, 190000.0, 'The Beatles final recorded album', 0.1, '14x12x1', 110, 'available', 0.1, 'CD', 'The Beatles', 'Apple Records', 'Rock', 17, '1969-09-26'),
    ('CD100006', 'Nevermind', 'Music', 160000.0, 160000.0, 'Nirvana grunge album', 0.1, '14x12x1', 120, 'available', 0.1, 'CD', 'Nirvana', 'DGC Records', 'Rock', 12, '1991-09-24'),
    ('CD100007', 'Random Access Memories', 'Music', 180000.0, 180000.0, 'Daft Punk electronic', 0.1, '14x12x1', 85, 'available', 0.1, 'CD', 'Daft Punk', 'Columbia Records', 'Electronic', 13, '2013-05-17'),
    ('CD100008', '21', 'Music', 150000.0, 150000.0, 'Adele soulful pop', 0.1, '14x12x1', 150, 'available', 0.1, 'CD', 'Adele', 'XL Recordings', 'Pop', 11, '2011-01-24'),
    ('CD100009', 'Rumours', 'Music', 170000.0, 170000.0, 'Fleetwood Mac classic rock', 0.1, '14x12x1', 60, 'available', 0.1, 'CD', 'Fleetwood Mac', 'Warner Bros. Records', 'Rock', 11, '1977-02-04'),
    ('CD100010', 'The Eminem Show', 'Music', 160000.0, 160000.0, 'Eminem hip hop album', 0.1, '14x12x1', 95, 'available', 0.1, 'CD', 'Eminem', 'Aftermath Entertainment', 'Hip Hop', 20, '2002-05-26'),
    ('CD100011', 'Continuum', 'Music', 150000.0, 150000.0, 'John Mayer blues rock', 0.1, '14x12x1', 65, 'available', 0.1, 'CD', 'John Mayer', 'Aware Records', 'Blues', 12, '2006-09-12'),
    ('CD100012', 'A Rush of Blood to the Head', 'Music', 160000.0, 160000.0, 'Coldplay alternative rock', 0.1, '14x12x1', 75, 'available', 0.1, 'CD', 'Coldplay', 'Parlophone', 'Rock', 11, '2002-08-26'),
    ('CD100013', 'Hotel California', 'Music', 180000.0, 180000.0, 'Eagles classic rock', 0.1, '14x12x1', 50, 'available', 0.1, 'CD', 'Eagles', 'Asylum Records', 'Rock', 9, '1976-12-08'),
    ('CD100014', 'Scorpion', 'Music', 220000.0, 220000.0, 'Drake double album', 0.1, '14x12x1', 80, 'available', 0.1, 'CD', 'Drake', 'Young Money', 'Hip Hop', 25, '2018-06-29'),
    ('CD100015', 'Folklore', 'Music', 200000.0, 200000.0, 'Taylor Swift indie folk', 0.1, '14x12x1', 140, 'available', 0.1, 'CD', 'Taylor Swift', 'Republic Records', 'Folk', 16, '2020-07-24');

-- DVDs (10 products)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     director, studio, subtitle, disc_type, duration, genre, release_date)
VALUES
    ('DVD10001', 'Interstellar', 'Movie', 250000.0, 250000.0, 'Sci-fi space travel epic', 0.2, '19x14x1', 50, 'available', 0.1, 'DVD', 'Christopher Nolan', 'Paramount Pictures', 'English, Vietnamese', 'Blu-ray', 169, 'Sci-Fi', '2014-11-07'),
    ('DVD10002', 'The Dark Knight', 'Movie', 240000.0, 240000.0, 'Batman superhero thriller', 0.2, '19x14x1', 70, 'available', 0.1, 'DVD', 'Christopher Nolan', 'Warner Bros. Pictures', 'English', 'DVD-9', 152, 'Action', '2008-07-18'),
    ('DVD10003', 'Pulp Fiction', 'Movie', 220000.0, 220000.0, 'Quentin Tarantino crime classic', 0.2, '19x14x1', 40, 'available', 0.1, 'DVD', 'Quentin Tarantino', 'Miramax Films', 'English', 'DVD-9', 154, 'Crime', '1994-10-14'),
    ('DVD10004', 'Fight Club', 'Movie', 210000.0, 210000.0, 'David Fincher psychological drama', 0.2, '19x14x1', 45, 'available', 0.1, 'DVD', 'David Fincher', '20th Century Fox', 'English', 'DVD-5', 139, 'Drama', '1999-10-15'),
    ('DVD10005', 'Spirited Away', 'Movie', 280000.0, 280000.0, 'Studio Ghibli animated fantasy', 0.2, '19x14x1', 60, 'available', 0.1, 'DVD', 'Hayao Miyazaki', 'Studio Ghibli', 'Japanese, English', 'DVD-9', 125, 'Anime', '2001-07-20'),
    ('DVD10006', 'Parasite', 'Movie', 260000.0, 260000.0, 'Bong Joon Ho thriller', 0.2, '19x14x1', 55, 'available', 0.1, 'DVD', 'Bong Joon-ho', 'Barunson E&A', 'Korean, English', 'Blu-ray', 132, 'Thriller', '2019-05-30'),
    ('DVD10007', 'The Godfather', 'Movie', 300000.0, 300000.0, 'Francis Ford Coppola crime masterpiece', 0.2, '19x14x1', 35, 'available', 0.1, 'DVD', 'Francis Ford Coppola', 'Paramount Pictures', 'English', '4K UHD', 175, 'Crime', '1972-03-24'),
    ('DVD10008', 'Gladiator', 'Movie', 230000.0, 230000.0, 'Ridley Scott historical epic', 0.2, '19x14x1', 42, 'available', 0.1, 'DVD', 'Ridley Scott', 'DreamWorks Pictures', 'English', 'DVD-9', 155, 'Action', '2000-05-05'),
    ('DVD10009', 'Spider-Man: Into the Spider-Verse', 'Movie', 250000.0, 250000.0, 'Animated superhero film', 0.2, '19x14x1', 80, 'available', 0.1, 'DVD', 'Bob Persichetti', 'Sony Pictures Releasing', 'English, Spanish', 'Blu-ray', 117, 'Animation', '2018-12-14'),
    ('DVD10010', 'The Shawshank Redemption', 'Movie', 220000.0, 220000.0, 'Frank Darabont prison drama', 0.2, '19x14x1', 65, 'available', 0.1, 'DVD', 'Frank Darabont', 'Castle Rock Entertainment', 'English', 'DVD-9', 142, 'Drama', '1994-09-23');

-- Newspapers (10 products)
INSERT INTO products (barcode, title, category, original_price, current_price, description,
                     weight, dimensions, stock, status, vat_rate, product_type,
                     issn, frequency, editor_in_chief, publisher, publication_date, language, section)
VALUES
    ('NP100001', 'The New York Times', 'Newspaper', 45000.0, 45000.0, 'International edition print', 0.25, '35x28x0.5', 100, 'available', 0.1, 'NEWSPAPER', '0362-4331', 'Daily', 'Dean Baquet', 'The New York Times Company', '2026-06-01', 'English', 'News, Opinion, Business'),
    ('NP100002', 'The Wall Street Journal', 'Newspaper', 50000.0, 50000.0, 'Financial and business news print', 0.25, '35x28x0.5', 90, 'available', 0.1, 'NEWSPAPER', '0099-9660', 'Daily', 'Matt Murray', 'Dow Jones & Company', '2026-06-01', 'English', 'Business, Economy, Tech'),
    ('NP100003', 'Financial Times', 'Newspaper', 60000.0, 60000.0, 'Global financial news printed daily', 0.25, '35x28x0.5', 80, 'available', 0.1, 'NEWSPAPER', '0307-1766', 'Daily', 'Roula Khalaf', 'FT Group', '2026-06-01', 'English', 'Finance, Politics, Global'),
    ('NP100004', 'The Guardian', 'Newspaper', 40000.0, 40000.0, 'UK daily newspaper', 0.22, '35x28x0.5', 75, 'available', 0.1, 'NEWSPAPER', '0261-3077', 'Daily', 'Katharine Viner', 'Guardian Media Group', '2026-06-01', 'English', 'News, Culture, Lifestyle'),
    ('NP100005', 'Le Monde', 'Newspaper', 55000.0, 55000.0, 'French national daily print', 0.24, '35x28x0.5', 50, 'available', 0.1, 'NEWSPAPER', '0395-2037', 'Daily', 'Jérôme Fenoglio', 'Société Éditrice du Monde', '2026-06-01', 'French', 'International, Opinion'),
    ('NP100006', 'Tuoi Tre Weekly', 'Newspaper', 12000.0, 12000.0, 'Tuoi Tre Weekend edition weekly newspaper', 0.3, '38x29x0.8', 250, 'available', 0.1, 'NEWSPAPER', 'T02-2026', 'Weekly', 'Le The Chieu', 'Tuoi Tre Publishing House', '2026-05-31', 'Vietnamese', 'Culture, Society, Literature'),
    ('NP100007', 'Thanh Nien Daily', 'Newspaper', 8000.0, 8000.0, 'Vietnamese daily printed newspaper', 0.2, '40x30x0.4', 400, 'available', 0.1, 'NEWSPAPER', 'T03-2026', 'Daily', 'Nguyen Ngoc Toan', 'Thanh Nien Publishing', '2026-06-01', 'Vietnamese', 'News, Law, Sports'),
    ('NP100008', 'Economist Weekly', 'Newspaper', 120000.0, 120000.0, 'Weekly analysis of global news and business', 0.35, '30x21x1', 60, 'available', 0.1, 'NEWSPAPER', '0013-0613', 'Weekly', 'Zanny Minton Beddoes', 'The Economist Group', '2026-05-30', 'English', 'World, Finance, Science'),
    ('NP100009', 'Time Magazine', 'Newspaper', 95000.0, 95000.0, 'Bi-weekly news printed magazine', 0.3, '30x21x0.8', 70, 'available', 0.1, 'NEWSPAPER', '0040-781X', 'Bi-weekly', 'Edward Felsenthal', 'Time USA, LLC', '2026-05-25', 'English', 'Politics, History, Society'),
    ('NP100010', 'Science Monthly', 'Newspaper', 150000.0, 150000.0, 'Monthly science advances and research journal', 0.4, '30x21x1.2', 40, 'available', 0.1, 'NEWSPAPER', '0036-8075', 'Monthly', 'Holden Thorp', 'AAAS', '2026-05-01', 'English', 'Research, Biology, Space');

-- Sample Tracks for newly added CDs (Must have at least 1 track per CD as per CDFactory rules)
INSERT INTO tracks (product_barcode, title, duration)
VALUES
    ('CD100001', 'Wanna Be Startin Somethin', 363),
    ('CD100002', 'Hells Bells', 312),
    ('CD100003', 'Speak to Me', 90),
    ('CD100004', 'Dont Know Why', 186),
    ('CD100005', 'Come Together', 259),
    ('CD100006', 'Smells Like Teen Spirit', 301),
    ('CD100007', 'Give Life Back to Music', 274),
    ('CD100008', 'Rolling in the Deep', 228),
    ('CD100009', 'Second Hand News', 163),
    ('CD100010', 'Without Me', 290),
    ('CD100011', 'Waiting on the World to Change', 201),
    ('CD100012', 'Politik', 318),
    ('CD100013', 'Hotel California', 390),
    ('CD100014', 'Survival', 227),
    ('CD100015', 'The 1', 210);
