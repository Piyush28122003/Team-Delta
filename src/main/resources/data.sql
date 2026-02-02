-- Sample Data for Portfolio Management System
-- Note: Passwords are hashed (BCrypt) - default password is "password123"

-- Insert sample users
INSERT INTO users (username, email, password, first_name, last_name, phone) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ3FK', 'John', 'Doe', '+1-555-0101'),
('jane_smith', 'jane.smith@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ3FK', 'Jane', 'Smith', '+1-555-0102'),
('bob_wilson', 'bob.wilson@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ3FK', 'Bob', 'Wilson', '+1-555-0103');

-- Insert bank accounts
INSERT INTO bank_accounts (user_id, account_number, bank_name, current_balance, account_type) VALUES
(1, 'ACC001234567', 'HSBC Bank', 50000.00, 'CHECKING'),
(2, 'ACC002345678', 'Chase Bank', 75000.00, 'CHECKING'),
(3, 'ACC003456789', 'Wells Fargo', 30000.00, 'CHECKING');

-- Insert portfolios
INSERT INTO portfolios (user_id, name, description) VALUES
(1, 'John\'s Investment Portfolio', 'Diversified portfolio focusing on tech and finance'),
(2, 'Jane\'s Growth Portfolio', 'Aggressive growth strategy with tech stocks'),
(3, 'Bob\'s Conservative Portfolio', 'Low-risk investments with stable returns');

-- Insert sample stocks
INSERT INTO stocks (symbol, company_name, sector, industry, exchange, currency) VALUES
('AAPL', 'Apple Inc.', 'Technology', 'Consumer Electronics', 'NASDAQ', 'USD'),
('GOOGL', 'Alphabet Inc.', 'Technology', 'Internet Content & Information', 'NASDAQ', 'USD'),
('MSFT', 'Microsoft Corporation', 'Technology', 'Software—Infrastructure', 'NASDAQ', 'USD'),
('AMZN', 'Amazon.com Inc.', 'Consumer Cyclical', 'Internet Retail', 'NASDAQ', 'USD'),
('TSLA', 'Tesla Inc.', 'Consumer Cyclical', 'Auto Manufacturers', 'NASDAQ', 'USD'),
('META', 'Meta Platforms Inc.', 'Technology', 'Internet Content & Information', 'NASDAQ', 'USD'),
('NVDA', 'NVIDIA Corporation', 'Technology', 'Semiconductors', 'NASDAQ', 'USD'),
('JPM', 'JPMorgan Chase & Co.', 'Financial Services', 'Banks—Diversified', 'NYSE', 'USD'),
('V', 'Visa Inc.', 'Financial Services', 'Credit Services', 'NYSE', 'USD'),
('JNJ', 'Johnson & Johnson', 'Healthcare', 'Drug Manufacturers—General', 'NYSE', 'USD'),
('WMT', 'Walmart Inc.', 'Consumer Defensive', 'Discount Stores', 'NYSE', 'USD'),
('PG', 'Procter & Gamble Co.', 'Consumer Defensive', 'Household & Personal Products', 'NYSE', 'USD');

-- Insert investments for John (User 1)
INSERT INTO investments (portfolio_id, stock_id, quantity, buy_price, buy_date, asset_type) VALUES
(1, 1, 10, 150.00, '2024-01-15', 'STOCK'),
(1, 2, 5, 140.00, '2024-02-01', 'STOCK'),
(1, 3, 8, 380.00, '2024-01-20', 'STOCK'),
(1, 7, 15, 450.00, '2024-03-10', 'STOCK'),
(1, 8, 20, 150.00, '2024-02-15', 'STOCK');

-- Insert investments for Jane (User 2)
INSERT INTO investments (portfolio_id, stock_id, quantity, buy_price, buy_date, asset_type) VALUES
(2, 4, 12, 120.00, '2024-01-10', 'STOCK'),
(2, 5, 8, 200.00, '2024-02-05', 'STOCK'),
(2, 6, 10, 300.00, '2024-01-25', 'STOCK'),
(2, 7, 20, 420.00, '2024-03-01', 'STOCK');

-- Insert investments for Bob (User 3)
INSERT INTO investments (portfolio_id, stock_id, quantity, buy_price, buy_date, asset_type) VALUES
(3, 9, 30, 220.00, '2024-01-05', 'STOCK'),
(3, 10, 25, 160.00, '2024-02-10', 'STOCK'),
(3, 11, 40, 150.00, '2024-01-30', 'STOCK'),
(3, 12, 35, 140.00, '2024-02-20', 'STOCK');

-- Insert risk profiles
INSERT INTO risk_profiles (user_id, risk_category, volatility_score, diversification_score, max_loss_tolerance, investment_horizon) VALUES
(1, 'MODERATE', 6.5, 7.2, 15.00, 'LONG'),
(2, 'AGGRESSIVE', 8.5, 6.8, 25.00, 'LONG'),
(3, 'CONSERVATIVE', 3.2, 8.5, 5.00, 'MEDIUM');

