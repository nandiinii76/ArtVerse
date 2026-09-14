ALTER TABLE orders ADD COLUMN seller_id UUID;
ALTER TABLE orders ADD COLUMN auction_id UUID;

CREATE UNIQUE INDEX uq_orders_auction_id ON orders(auction_id) WHERE auction_id IS NOT NULL;
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
