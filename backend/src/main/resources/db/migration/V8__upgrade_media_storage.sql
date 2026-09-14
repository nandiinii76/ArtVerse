ALTER TABLE media_assets ADD COLUMN IF NOT EXISTS provider VARCHAR(30) NOT NULL DEFAULT 'local';
ALTER TABLE media_assets ADD COLUMN IF NOT EXISTS resource_url VARCHAR(1500) NOT NULL DEFAULT '';
UPDATE media_assets SET resource_url = '/api/v1/media/' || id WHERE resource_url = '';
CREATE INDEX IF NOT EXISTS idx_media_provider ON media_assets(provider);
