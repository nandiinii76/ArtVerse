ALTER TABLE orders ADD COLUMN IF NOT EXISTS seller_id UUID;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS auction_id UUID;
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_auction_id ON orders(auction_id) WHERE auction_id IS NOT NULL;
