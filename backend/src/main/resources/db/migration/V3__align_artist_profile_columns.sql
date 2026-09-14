ALTER TABLE artist_profiles RENAME COLUMN website TO website_url;
ALTER TABLE artist_profiles ADD COLUMN profile_image_url VARCHAR(1000);
