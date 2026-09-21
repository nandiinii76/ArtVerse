-- ArtVerse MySQL baseline schema.
-- UUID values are stored as CHAR(36) so the schema matches Hibernate's preferred UUID JDBC type.

CREATE TABLE users (
    id CHAR(36) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    bio VARCHAR(2000),
    avatar_url VARCHAR(500),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE artworks (
    id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(5000),
    image_url VARCHAR(1000),
    artist_id CHAR(36) NOT NULL,
    category VARCHAR(80) NOT NULL,
    style VARCHAR(100),
    medium VARCHAR(120),
    year_created INT,
    price DECIMAL(14,2),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    views BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_artworks_artist FOREIGN KEY (artist_id) REFERENCES users(id)
);

CREATE INDEX idx_artwork_artist ON artworks(artist_id);
CREATE INDEX idx_artwork_category ON artworks(category);
CREATE INDEX idx_artwork_status ON artworks(status);
CREATE INDEX idx_artwork_title ON artworks(title);

CREATE TABLE artist_profiles (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL UNIQUE,
    biography VARCHAR(5000),
    location VARCHAR(180),
    website_url VARCHAR(500),
    profile_image_url VARCHAR(1000),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_artist_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE artwork_favorites (
    artwork_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    PRIMARY KEY (artwork_id, user_id),
    CONSTRAINT fk_favorites_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_follows (
    follower_id CHAR(36) NOT NULL,
    following_id CHAR(36) NOT NULL,
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_follows_not_self CHECK (follower_id <> following_id)
);

CREATE TABLE artwork_comments (
    id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE marketplace_listings (
    id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL UNIQUE,
    seller_id CHAR(36) NOT NULL,
    price DECIMAL(14,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_listings_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id),
    CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE orders (
    id CHAR(36) NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL,
    seller_id CHAR(36),
    auction_id CHAR(36) UNIQUE,
    amount DECIMAL(14,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_reference VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_orders_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id)
);

CREATE TABLE artwork_ownership (
    id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL,
    owner_id CHAR(36) NOT NULL,
    acquired_at TIMESTAMP(6) NOT NULL,
    transaction_id CHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_ownership_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_ownership_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE auctions (
    id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL UNIQUE,
    seller_id CHAR(36) NOT NULL,
    starting_price DECIMAL(14,2) NOT NULL,
    current_price DECIMAL(14,2) NOT NULL,
    minimum_increment DECIMAL(14,2) NOT NULL DEFAULT 100.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    starts_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    winner_id CHAR(36),
    winner_notified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_auctions_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id),
    CONSTRAINT fk_auctions_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_auctions_winner FOREIGN KEY (winner_id) REFERENCES users(id),
    CONSTRAINT chk_auction_time CHECK (ends_at > starts_at)
);

CREATE TABLE auction_bids (
    id CHAR(36) NOT NULL,
    auction_id CHAR(36) NOT NULL,
    bidder_id CHAR(36) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_bids_bidder FOREIGN KEY (bidder_id) REFERENCES users(id)
);

CREATE INDEX idx_bids_auction_time ON auction_bids(auction_id, created_at DESC);

CREATE TABLE notifications (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);

CREATE TABLE collections (
    id CHAR(36) NOT NULL,
    owner_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    cover_artwork_id CHAR(36),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_collections_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_collections_cover FOREIGN KEY (cover_artwork_id) REFERENCES artworks(id) ON DELETE SET NULL
);

CREATE INDEX idx_collections_owner ON collections(owner_id);

CREATE TABLE collection_artworks (
    collection_id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL,
    PRIMARY KEY (collection_id, artwork_id),
    CONSTRAINT fk_collection_artworks_collection FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_collection_artworks_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
);

CREATE TABLE media_assets (
    id CHAR(36) NOT NULL,
    artwork_id CHAR(36) NOT NULL,
    owner_id CHAR(36) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    provider VARCHAR(30) NOT NULL DEFAULT 'local',
    resource_url VARCHAR(1500) NOT NULL DEFAULT '',
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_media_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_media_artwork ON media_assets(artwork_id);
CREATE INDEX idx_media_owner ON media_assets(owner_id);
CREATE INDEX idx_media_provider ON media_assets(provider);
