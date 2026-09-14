ALTER TABLE collections RENAME COLUMN user_id TO owner_id;
ALTER TABLE collections ADD COLUMN cover_artwork_id UUID REFERENCES artworks(id) ON DELETE SET NULL;
