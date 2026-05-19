-- ==================================================
-- ArtConnect Database Schema
-- ==================================================

-- Drop existing database if it exists (for fresh start)
DROP DATABASE IF EXISTS artconnect_db;

-- Create the database
CREATE DATABASE artconnect_db;
USE artconnect_db;

-- ==================================================
-- TABLES
-- ==================================================

-- 1. ARTIST table
CREATE TABLE artist (
    name VARCHAR(100) PRIMARY KEY,
    bio TEXT,
    birth_year INT,
    contact_email VARCHAR(100),
    phone VARCHAR(20),
    city VARCHAR(50),
    website VARCHAR(255),
    social_media VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. GALLERY table
CREATE TABLE gallery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255),
    owner_name VARCHAR(100),
    opening_hours VARCHAR(100),
    contact_phone VARCHAR(20),
    rating DOUBLE DEFAULT 0.0,
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. EXHIBITION table
CREATE TABLE exhibition (
    title VARCHAR(100) PRIMARY KEY,
    start_date DATE,
    end_date DATE,
    description TEXT,
    gallery_name VARCHAR(100),
    curator_name VARCHAR(100),
    theme VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (gallery_name) REFERENCES gallery(name) ON DELETE SET NULL
);

-- 4. ARTWORK table
CREATE TABLE artwork (
    title VARCHAR(100) PRIMARY KEY,
    creation_year INT,
    type VARCHAR(50),
    medium VARCHAR(50),
    dimensions VARCHAR(50),
    description TEXT,
    price DOUBLE DEFAULT 0.0,
    status VARCHAR(20) DEFAULT 'FOR_SALE',
    artist_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (artist_name) REFERENCES artist(name) ON DELETE SET NULL
);

-- 5. COMMUNITY_MEMBER table
CREATE TABLE community_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    birth_year INT,
    phone VARCHAR(20),
    city VARCHAR(50),
    membership_type VARCHAR(20) DEFAULT 'free',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. WORKSHOP table
CREATE TABLE workshop (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    date DATETIME,
    duration_minutes INT,
    max_participants INT,
    price DOUBLE DEFAULT 0.0,
    instructor_name VARCHAR(100),
    location VARCHAR(255),
    description TEXT,
    level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instructor_name) REFERENCES artist(name) ON DELETE SET NULL
);

-- 7. DISCIPLINE table (for art disciplines)
CREATE TABLE discipline (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. ARTIST_DISCIPLINE junction table (many-to-many)
CREATE TABLE artist_discipline (
    artist_name VARCHAR(100) NOT NULL,
    discipline_id INT NOT NULL,
    PRIMARY KEY (artist_name, discipline_id),
    FOREIGN KEY (artist_name) REFERENCES artist(name) ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES discipline(id) ON DELETE CASCADE
);

-- 9. BOOKING table (for community members booking workshops)
CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    workshop_id BIGINT NOT NULL,
    booking_date DATE,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES community_member(id) ON DELETE CASCADE,
    FOREIGN KEY (workshop_id) REFERENCES workshop(id) ON DELETE CASCADE,
    UNIQUE KEY unique_booking (member_id, workshop_id)
);

-- 10. REVIEW table (for members reviewing artworks)
CREATE TABLE review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    artwork_title VARCHAR(100) NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES community_member(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_title) REFERENCES artwork(title) ON DELETE CASCADE
);

-- 11. ARTWORK_TAG table (for tagging artworks)
CREATE TABLE artwork_tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    artwork_title VARCHAR(100) NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (artwork_title) REFERENCES artwork(title) ON DELETE CASCADE,
    UNIQUE KEY unique_artwork_tag (artwork_title, tag_name)
);

-- ==================================================
-- INDEXES for Performance Optimization
-- ==================================================

-- Index on artist city for findByCity queries
CREATE INDEX idx_artist_city ON artist(city);

-- Index on artwork artist for findByArtistName queries
CREATE INDEX idx_artwork_artist ON artwork(artist_name);

-- Index on exhibition gallery for gallery queries
CREATE INDEX idx_exhibition_gallery ON exhibition(gallery_name);

-- Index on workshop instructor for instructor queries
CREATE INDEX idx_workshop_instructor ON workshop(instructor_name);

-- Index on community member city for location-based searches
CREATE INDEX idx_community_member_city ON community_member(city);

-- Index on booking dates for temporal queries
CREATE INDEX idx_booking_date ON booking(booking_date);

-- ==================================================
-- VIEWS
-- ==================================================

-- View: Active Artists
CREATE VIEW view_active_artists AS
SELECT * FROM artist WHERE is_active = TRUE;

-- View: Upcoming Exhibitions
CREATE VIEW view_upcoming_exhibitions AS
SELECT * FROM exhibition WHERE end_date >= CURDATE();

-- View: Available Workshops
CREATE VIEW view_available_workshops AS
SELECT * FROM workshop 
WHERE date >= NOW() 
AND (SELECT COUNT(*) FROM booking WHERE workshop_id = workshop.id) < max_participants;

-- View: Member Booking History
CREATE VIEW view_member_bookings AS
SELECT 
    cm.id as member_id,
    cm.name as member_name,
    w.title as workshop_title,
    b.booking_date,
    b.status
FROM community_member cm
JOIN booking b ON cm.id = b.member_id
JOIN workshop w ON b.workshop_id = w.id;

-- ==================================================
-- STORED PROCEDURES
-- ==================================================

-- Procedure: Add new member and get member ID
DELIMITER $$
CREATE PROCEDURE AddNewMember(
    IN p_name VARCHAR(100),
    IN p_email VARCHAR(100),
    IN p_city VARCHAR(50),
    OUT p_member_id BIGINT
)
BEGIN
    INSERT INTO community_member (name, email, city) VALUES (p_name, p_email, p_city);
    SET p_member_id = LAST_INSERT_ID();
END$$
DELIMITER ;

-- Procedure: Register member for workshop
DELIMITER $$
CREATE PROCEDURE RegisterMemberForWorkshop(
    IN p_member_id BIGINT,
    IN p_workshop_id BIGINT,
    OUT p_success BOOLEAN
)
BEGIN
    DECLARE v_current_count INT;
    DECLARE v_max_participants INT;
    
    SELECT max_participants INTO v_max_participants FROM workshop WHERE id = p_workshop_id;
    SELECT COUNT(*) INTO v_current_count FROM booking WHERE workshop_id = p_workshop_id;
    
    IF v_current_count < v_max_participants THEN
        INSERT INTO booking (member_id, workshop_id, booking_date) 
        VALUES (p_member_id, p_workshop_id, CURDATE());
        SET p_success = TRUE;
    ELSE
        SET p_success = FALSE;
    END IF;
END$$
DELIMITER ;

-- Procedure: Get artist by city
DELIMITER $$
CREATE PROCEDURE GetArtistsByCity(IN p_city VARCHAR(50))
BEGIN
    SELECT * FROM artist WHERE city = p_city AND is_active = TRUE;
END$$
DELIMITER ;

-- ==================================================
-- TRIGGERS
-- ==================================================

-- Trigger: Validate workshop date is in future
DELIMITER $$
CREATE TRIGGER trg_workshop_date_validation
BEFORE INSERT ON workshop
FOR EACH ROW
BEGIN
    IF NEW.date < NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Workshop date must be in the future';
    END IF;
END$$
DELIMITER ;

-- Trigger: Validate exhibition end date is after start date
DELIMITER $$
CREATE TRIGGER trg_exhibition_date_validation
BEFORE INSERT ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date < NEW.start_date THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Exhibition end date must be after start date';
    END IF;
END$$
DELIMITER ;

-- Trigger: Prevent negative artwork price
DELIMITER $$
CREATE TRIGGER trg_artwork_price_validation
BEFORE INSERT ON artwork
FOR EACH ROW
BEGIN
    IF NEW.price < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Artwork price cannot be negative';
    END IF;
END$$
DELIMITER ;

-- Trigger: Update artwork updated_at timestamp
DELIMITER $$
CREATE TRIGGER trg_artwork_update_timestamp
BEFORE UPDATE ON artwork
FOR EACH ROW
SET NEW.updated_at = CURRENT_TIMESTAMP$$
DELIMITER ;

-- ==================================================
-- SAMPLE DATA (Optional - for testing)
-- ==================================================

-- Insert some sample disciplines
INSERT INTO discipline (name, description) VALUES
('Painting', 'Traditional and modern painting techniques'),
('Sculpture', 'Three-dimensional art forms'),
('Photography', 'Art of capturing images'),
('Digital Art', 'Art created with digital tools');

-- Insert sample artists
INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES
('Leonardo Da Vinci', 'Renaissance master', 1452, 'leo@art.com', '+33-123456789', 'Paris', 'www.leo.com', '@leonardo_art', TRUE),
('Pablo Picasso', 'Cubism pioneer', 1881, 'pablo@art.com', '+34-987654321', 'Barcelona', 'www.pablo.com', '@picasso_modern', TRUE),
('Frida Kahlo', 'Surrealist artist', 1907, 'frida@art.com', '+52-555123456', 'Mexico City', 'www.frida.com', '@frida_kahlo', TRUE);

-- Insert sample galleries
INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
('Louvre Gallery', '123 Art Street, Paris', 'Jean Dupont', 'Mon-Sun 9AM-6PM', '+33-111222333', 4.8, 'www.louvre.com'),
('Prado Museum', '456 Museum Ave, Madrid', 'Maria Garcia', 'Tue-Sun 10AM-8PM', '+34-222333444', 4.9, 'www.prado.com');

-- Insert sample exhibitions
INSERT INTO exhibition (title, start_date, end_date, description, gallery_name, curator_name, theme) VALUES
('Modern Masters', '2024-06-01', '2024-08-31', 'Exhibition of modern art masters', 'Louvre Gallery', 'Jean Dupont', 'Modernism'),
('Surrealism Today', '2024-07-15', '2024-09-30', 'Contemporary surrealist works', 'Prado Museum', 'Maria Garcia', 'Surrealism');

-- Insert sample artworks
INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_name) VALUES
('Starry Night', 1889, 'Painting', 'Oil on canvas', '73.7 x 92.1 cm', 'A beautiful night sky', 50000.00, 'EXHIBITED', 'Leonardo Da Vinci'),
('Guernica', 1937, 'Painting', 'Oil on canvas', '349 x 776 cm', 'Anti-war masterpiece', 100000.00, 'EXHIBITED', 'Pablo Picasso'),
('Self-Portrait', 1940, 'Painting', 'Oil on canvas', '60 x 47 cm', 'Introspective self-portrait', 75000.00, 'FOR_SALE', 'Frida Kahlo');

-- Insert sample community members
INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES
('Alice Martin', 'alice@email.com', 1990, '+33-666777888', 'Paris', 'premium'),
('Bob Johnson', 'bob@email.com', 1985, '+44-555666777', 'London', 'free'),
('Catherine Chen', 'catherine@email.com', 1995, '+86-777888999', 'Beijing', 'premium');

-- Insert sample workshops
INSERT INTO workshop (title, date, duration_minutes, max_participants, price, instructor_name, location, description, level) VALUES
('Painting Basics', '2024-06-15 10:00:00', 120, 20, 50.00, 'Leonardo Da Vinci', 'Louvre Gallery', 'Learn basic painting techniques', 'beginner'),
('Advanced Sculpture', '2024-07-20 14:00:00', 180, 15, 100.00, 'Pablo Picasso', 'Prado Museum', 'Master sculpture techniques', 'advanced');

-- ==================================================
-- END OF SCRIPT
-- ==================================================
