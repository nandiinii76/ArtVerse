CREATE TABLE artworks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(180) NOT NULL,
    description VARCHAR(5000),
    image_url VARCHAR(1000),
    artist_id UUID NOT NULL REFERENCES users(id),
    category VARCHAR(80) NOT NULL,
    style VARCHAR(100),
    medium VARCHAR(120),
    year_created INTEGER,
    price NUMERIC(14,2),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    views BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_artworks_artist ON artworks(artist_id);
CREATE INDEX idx_artworks_category ON artworks(category);
CREATE INDEX idx_artworks_status ON artworks(status);

CREATE TABLE artist_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    biography VARCHAR(5000), location VARCHAR(180), website_url VARCHAR(500),
    social_links VARCHAR(2000), profile_image_url VARCHAR(1000),
    verified BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE favorites (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artwork_id UUID NOT NULL REFERENCES artworks(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(user_id, artwork_id)
);
CREATE TABLE follows (
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artist_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(follower_id, artist_id), CHECK(follower_id <> artist_id)
);
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), artwork_id UUID NOT NULL REFERENCES artworks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE marketplace_listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), artwork_id UUID NOT NULL UNIQUE REFERENCES artworks(id), seller_id UUID NOT NULL REFERENCES users(id),
    price NUMERIC(14,2) NOT NULL, currency VARCHAR(10) NOT NULL DEFAULT 'INR', status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), buyer_id UUID NOT NULL REFERENCES users(id), listing_id UUID NOT NULL REFERENCES marketplace_listings(id),
    amount NUMERIC(14,2) NOT NULL, currency VARCHAR(10) NOT NULL DEFAULT 'INR', status VARCHAR(30) NOT NULL DEFAULT 'PENDING', created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE artwork_ownership (
    artwork_id UUID PRIMARY KEY REFERENCES artworks(id) ON DELETE CASCADE, owner_id UUID NOT NULL REFERENCES users(id), acquired_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auctions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), artwork_id UUID NOT NULL UNIQUE REFERENCES artworks(id), seller_id UUID NOT NULL REFERENCES users(id),
    starting_price NUMERIC(14,2) NOT NULL, minimum_increment NUMERIC(14,2) NOT NULL DEFAULT 100,
    start_time TIMESTAMPTZ NOT NULL, end_time TIMESTAMPTZ NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', highest_bid NUMERIC(14,2), highest_bidder_id UUID REFERENCES users(id),
    CHECK(end_time > start_time)
);
CREATE TABLE bids (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), auction_id UUID NOT NULL REFERENCES auctions(id) ON DELETE CASCADE,
    bidder_id UUID NOT NULL REFERENCES users(id), amount NUMERIC(14,2) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_bids_auction_time ON bids(auction_id, created_at DESC);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(60) NOT NULL, title VARCHAR(180) NOT NULL, message VARCHAR(1000), read_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL, description VARCHAR(1000), is_public BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE collection_artworks (
    collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    artwork_id UUID NOT NULL REFERENCES artworks(id) ON DELETE CASCADE, PRIMARY KEY(collection_id, artwork_id)
);
