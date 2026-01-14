-- =============================================
-- ISDN Management System - Sample Data
-- =============================================

-- =============================================
-- Insert RDCs (Regional Distribution Centers)
-- =============================================
INSERT INTO rdcs (name, region, address, contact_number, email, active) VALUES
('Northern RDC', 'NORTH', 'No. 45, Main Street, Jaffna', '+94212222222', 'north@isdn.lk', TRUE),
('Southern RDC', 'SOUTH', 'No. 78, Galle Road, Matara', '+94412345678', 'south@isdn.lk', TRUE),
('Eastern RDC', 'EAST', 'No. 123, Beach Road, Batticaloa', '+94652222333', 'east@isdn.lk', TRUE),
('Western RDC', 'WEST', 'No. 456, Negombo Road, Colombo', '+94112345678', 'west@isdn.lk', TRUE),
('Central RDC', 'CENTRAL', 'No. 789, Kandy Road, Kandy', '+94812345678', 'central@isdn.lk', TRUE);

-- =============================================
-- Insert Users
-- Password for all: password123 (hashed with BCrypt)
-- =============================================
INSERT INTO users (username, email, password, role, business_name, contact_person, phone_number, address, active) VALUES
-- Admin
('admin', 'admin@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'ADMIN', 'ISDN Head Office', 'Admin User', '+94112223333', 'Colombo 03', TRUE),

-- HO Manager
('manager1', 'manager@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'HO_MANAGER', 'ISDN Head Office', 'John Manager', '+94112224444', 'Colombo 03', TRUE),

-- RDC Staff
('rdc_staff1', 'staff1@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'RDC_STAFF', 'Western RDC', 'Staff Member 1', '+94112225555', 'Colombo', TRUE),
('rdc_staff2', 'staff2@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'RDC_STAFF', 'Central RDC', 'Staff Member 2', '+94812226666', 'Kandy', TRUE),

-- Logistics Officers
('logistics1', 'logistics@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'LOGISTICS_OFFICER', 'Western RDC', 'Logistics Officer', '+94112227777', 'Colombo', TRUE),

-- Drivers
('driver1', 'driver1@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'DRIVER', 'Western RDC', 'Driver 1', '+94771234567', 'Colombo', TRUE),
('driver2', 'driver2@isdn.lk', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'DRIVER', 'Central RDC', 'Driver 2', '+94772345678', 'Kandy', TRUE),

-- Customers
('customer1', 'customer1@gmail.com', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'CUSTOMER', 'Super Mart Pvt Ltd', 'John Customer', '+94773456789', '123 Main St, Colombo 05', TRUE),
('customer2', 'customer2@gmail.com', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'CUSTOMER', 'Quick Shop', 'Jane Doe', '+94774567890', '456 Galle Road, Colombo 03', TRUE),
('customer3', 'customer3@gmail.com', '$2a$10$slYQm/sMEuJM8JiwKM7KWOxxj1.xQFQgGdvpGu8VN/BZVxlH7L5gu', 'CUSTOMER', 'Mega Store', 'Mike Smith', '+94775678901', '789 Kandy Road, Kandy', TRUE);

-- =============================================
-- Insert Products
-- =============================================
INSERT INTO products (sku, name, description, category, unit_price, image_url, active) VALUES
-- Packaged Foods
('SKU001', 'Anchor Milk Powder 400g', 'Full cream milk powder, rich in calcium and vitamins', 'PACKAGED_FOODS', 850.00, '/images/products/anchor-milk.jpg', TRUE),
('SKU002', 'Anchor Milk Powder 1kg', 'Full cream milk powder - Family pack', 'PACKAGED_FOODS', 2100.00, '/images/products/anchor-milk-1kg.jpg', TRUE),
('SKU003', 'Munchee Lemon Puff 200g', 'Crispy lemon flavored cream biscuits', 'PACKAGED_FOODS', 180.00, '/images/products/lemon-puff.jpg', TRUE),
('SKU004', 'Munchee Cheese Crackers 190g', 'Delicious cheese flavored crackers', 'PACKAGED_FOODS', 220.00, '/images/products/cheese-crackers.jpg', TRUE),
('SKU005', 'MD Mango Pickle 400g', 'Authentic Sri Lankan mango pickle', 'PACKAGED_FOODS', 350.00, '/images/products/mango-pickle.jpg', TRUE),
('SKU006', 'Prima Kottu Mee 400g', 'Instant noodles - Kottu flavor', 'PACKAGED_FOODS', 280.00, '/images/products/kottu-mee.jpg', TRUE),
('SKU007', 'Rathna Rice 5kg', 'Premium quality white rice', 'PACKAGED_FOODS', 1250.00, '/images/products/rice.jpg', TRUE),
('SKU008', 'MDK Noodles 400g', 'Chicken flavored instant noodles', 'PACKAGED_FOODS', 160.00, '/images/products/noodles.jpg', TRUE),

-- Beverages
('SKU009', 'Coca Cola 1.5L', 'Carbonated soft drink - Original taste', 'BEVERAGES', 250.00, '/images/products/coca-cola.jpg', TRUE),
('SKU010', 'Sprite 1.5L', 'Lemon-lime flavored carbonated drink', 'BEVERAGES', 250.00, '/images/products/sprite.jpg', TRUE),
('SKU011', 'Fanta Orange 1.5L', 'Orange flavored carbonated drink', 'BEVERAGES', 250.00, '/images/products/fanta.jpg', TRUE),
('SKU012', 'Elephant House Cream Soda 1.5L', 'Classic cream soda drink', 'BEVERAGES', 220.00, '/images/products/cream-soda.jpg', TRUE),
('SKU013', 'MD Mango Juice 1L', '100% pure mango juice', 'BEVERAGES', 450.00, '/images/products/mango-juice.jpg', TRUE),
('SKU014', 'Lipton Yellow Label Tea 200g', 'Premium quality black tea', 'BEVERAGES', 680.00, '/images/products/lipton-tea.jpg', TRUE),
('SKU015', 'Nescafe Classic 50g', 'Pure instant coffee', 'BEVERAGES', 550.00, '/images/products/nescafe.jpg', TRUE),

-- Home Cleaning
('SKU016', 'Sunlight Washing Powder 1kg', 'Powerful cleaning detergent powder', 'HOME_CLEANING', 420.00, '/images/products/sunlight.jpg', TRUE),
('SKU017', 'Sunlight Liquid Detergent 1L', 'Liquid washing detergent - lemon fresh', 'HOME_CLEANING', 480.00, '/images/products/sunlight-liquid.jpg', TRUE),
('SKU018', 'Vim Dishwash Liquid 500ml', 'Concentrated dishwashing liquid', 'HOME_CLEANING', 380.00, '/images/products/vim.jpg', TRUE),
('SKU019', 'Harpic Toilet Cleaner 500ml', 'Powerful toilet bowl cleaner', 'HOME_CLEANING', 320.00, '/images/products/harpic.jpg', TRUE),
('SKU020', 'Domex Floor Cleaner 1L', 'Multi-surface floor cleaner', 'HOME_CLEANING', 450.00, '/images/products/domex.jpg', TRUE),

-- Personal Care
('SKU021', 'Pears Soap 125g', 'Transparent glycerin soap - gentle on skin', 'PERSONAL_CARE', 150.00, '/images/products/pears-soap.jpg', TRUE),
('SKU022', 'Lux Soap 100g', 'Beauty soap with floral fragrance', 'PERSONAL_CARE', 120.00, '/images/products/lux.jpg', TRUE),
('SKU023', 'Sunsilk Shampoo 400ml', 'Hair fall solution shampoo', 'PERSONAL_CARE', 680.00, '/images/products/sunsilk.jpg', TRUE),
('SKU024', 'Colgate Toothpaste 120g', 'Cavity protection toothpaste', 'PERSONAL_CARE', 280.00, '/images/products/colgate.jpg', TRUE),
('SKU025', 'Signal Toothpaste 120g', 'Complete oral care toothpaste', 'PERSONAL_CARE', 260.00, '/images/products/signal.jpg', TRUE),
('SKU026', 'Fair & Lovely Cream 50g', 'Fairness cream with multivitamins', 'PERSONAL_CARE', 420.00, '/images/products/fair-lovely.jpg', TRUE),
('SKU027', 'Ponds Powder 100g', 'Talcum powder with natural oils', 'PERSONAL_CARE', 380.00, '/images/products/ponds-powder.jpg', TRUE),
('SKU028', 'Vaseline Petroleum Jelly 100ml', 'Pure petroleum jelly for skin care', 'PERSONAL_CARE', 220.00, '/images/products/vaseline.jpg', TRUE);

-- =============================================
-- Insert Inventory (Stock for each RDC)
-- =============================================
-- Western RDC (Product ID 1-28)
INSERT INTO inventory (product_id, rdc_id, quantity_on_hand, reorder_level) VALUES
(1, 4, 500, 100), (2, 4, 300, 50), (3, 4, 800, 150), (4, 4, 600, 150),
(5, 4, 400, 80), (6, 4, 700, 120), (7, 4, 250, 50), (8, 4, 900, 150),
(9, 4, 600, 100), (10, 4, 600, 100), (11, 4, 550, 100), (12, 4, 500, 100),
(13, 4, 300, 60), (14, 4, 400, 80), (15, 4, 350, 70),
(16, 4, 450, 90), (17, 4, 400, 80), (18, 4, 500, 100), (19, 4, 450, 90), (20, 4, 350, 70),
(21, 4, 800, 150), (22, 4, 900, 150), (23, 4, 400, 80), (24, 4, 600, 120), (25, 4, 550, 110),
(26, 4, 350, 70), (27, 4, 400, 80), (28, 4, 500, 100);

-- Central RDC
INSERT INTO inventory (product_id, rdc_id, quantity_on_hand, reorder_level) VALUES
(1, 5, 400, 100), (2, 5, 250, 50), (3, 5, 700, 150), (4, 5, 500, 150),
(5, 5, 350, 80), (6, 5, 600, 120), (7, 5, 200, 50), (8, 5, 800, 150),
(9, 5, 500, 100), (10, 5, 500, 100), (11, 5, 450, 100), (12, 5, 400, 100),
(13, 5, 250, 60), (14, 5, 350, 80), (15, 5, 300, 70);

-- Northern RDC (Lower stock)
INSERT INTO inventory (product_id, rdc_id, quantity_on_hand, reorder_level) VALUES
(1, 1, 200, 100), (2, 1, 150, 50), (3, 1, 300, 150), (4, 1, 250, 150),
(9, 1, 300, 100), (10, 1, 300, 100), (16, 1, 250, 90), (21, 1, 400, 150);

-- =============================================
-- Insert Promotions
-- =============================================
INSERT INTO promotions (title, description, discount_percentage, start_date, end_date, active) VALUES
('New Year Special', 'Get 10% off on all packaged foods', 10.00, '2024-01-01', '2024-01-31', TRUE),
('Summer Drinks Sale', '15% discount on all beverages', 15.00, '2024-03-01', '2024-03-31', FALSE),
('Cleaning Products Offer', '20% off on all home cleaning products', 20.00, '2024-02-01', '2024-02-28', TRUE);

-- Link products to promotions
INSERT INTO product_promotions (product_id, promotion_id) VALUES
-- New Year Special (Promotion 1) - All packaged foods
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1),
-- Cleaning Products Offer (Promotion 3)
(16, 3), (17, 3), (18, 3), (19, 3), (20, 3);

-- =============================================
-- Insert Drivers
-- =============================================
INSERT INTO drivers (user_id, rdc_id, license_number, vehicle_number, vehicle_type, phone_number, active) VALUES
(6, 4, 'DL12345678', 'WP-CAB-1234', 'Van', '+94771234567', TRUE),
(7, 5, 'DL87654321', 'WP-CAB-5678', 'Truck', '+94772345678', TRUE);

-- =============================================
-- Insert Sample Orders
-- =============================================
INSERT INTO orders (order_number, user_id, rdc_id, status, total_amount, delivery_address, contact_number, payment_method, order_date, estimated_delivery_date) VALUES
('ORD-2024-0001', 8, 4, 'DELIVERED', 4250.00, '123 Main St, Colombo 05', '+94773456789', 'CASH_ON_DELIVERY', '2024-01-05 10:30:00', '2024-01-06'),
('ORD-2024-0002', 9, 4, 'OUT_FOR_DELIVERY', 1800.00, '456 Galle Road, Colombo 03', '+94774567890', 'ONLINE_PAYMENT', '2024-01-07 14:15:00', '2024-01-08'),
('ORD-2024-0003', 10, 5, 'CONFIRMED', 3500.00, '789 Kandy Road, Kandy', '+94775678901', 'CASH_ON_DELIVERY', '2024-01-07 16:45:00', '2024-01-09');

-- Insert order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
-- Order 1
(1, 1, 5, 850.00, 4250.00),
-- Order 2
(2, 9, 4, 250.00, 1000.00),
(2, 10, 2, 250.00, 500.00),
(2, 3, 2, 180.00, 360.00),
-- Order 3
(3, 2, 1, 2100.00, 2100.00),
(3, 7, 1, 1250.00, 1250.00),
(3, 16, 1, 420.00, 420.00);

-- =============================================
-- Insert Invoices
-- =============================================
INSERT INTO invoices (order_id, invoice_number, issue_date, due_date, total_amount, paid_amount, status) VALUES
(1, 'INV-2024-0001', '2024-01-05', '2024-02-04', 4250.00, 4250.00, 'PAID'),
(2, 'INV-2024-0002', '2024-01-07', '2024-02-06', 1800.00, 1800.00, 'PAID'),
(3, 'INV-2024-0003', '2024-01-07', '2024-02-06', 3500.00, 0.00, 'UNPAID');

-- Insert payments
INSERT INTO payments (invoice_id, payment_method, amount, transaction_id, status, payment_date) VALUES
(1, 'CASH', 4250.00, 'CASH-20240105-001', 'SUCCESS', '2024-01-06 15:30:00'),
(2, 'ONLINE', 1800.00, 'PAY-20240107-XYZ123', 'SUCCESS', '2024-01-07 14:20:00');

-- Insert deliveries
INSERT INTO deliveries (order_id, driver_id, status, scheduled_date, scheduled_time_slot, actual_delivery_date) VALUES
(1, 1, 'DELIVERED', '2024-01-06', '10:00-12:00', '2024-01-06 11:45:00'),
(2, 1, 'IN_TRANSIT', '2024-01-08', '14:00-16:00', NULL);

-- =============================================
-- Verification Queries
-- =============================================
-- Run these to verify data insertion
-- SELECT * FROM users;
-- SELECT * FROM rdcs;
-- SELECT * FROM products;
-- SELECT * FROM inventory;
-- SELECT * FROM orders;
-- SELECT * FROM v_product_inventory;
-- SELECT * FROM v_order_summary;
```

---

