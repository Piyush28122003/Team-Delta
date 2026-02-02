-- Portfolio Management System Database Schema
-- Fully normalized (3NF) with foreign key constraints

-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS investments;
DROP TABLE IF EXISTS portfolios;
DROP TABLE IF EXISTS risk_profiles;
DROP TABLE IF EXISTS bank_accounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS stocks;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bank Accounts table
CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    current_balance DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    account_type VARCHAR(20) DEFAULT 'CHECKING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_account_number (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Portfolios table
CREATE TABLE portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(100) DEFAULT 'My Portfolio',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stocks reference table (for stock metadata)
CREATE TABLE stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) UNIQUE NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    sector VARCHAR(100),
    industry VARCHAR(100),
    exchange VARCHAR(50),
    currency VARCHAR(10) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_symbol (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Investments table (user's stock holdings)
CREATE TABLE investments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    buy_price DECIMAL(10, 2) NOT NULL CHECK (buy_price > 0),
    buy_date DATE NOT NULL,
    asset_type VARCHAR(20) DEFAULT 'STOCK',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE RESTRICT,
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_stock_id (stock_id),
    INDEX idx_buy_date (buy_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Risk Profiles table
CREATE TABLE risk_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    risk_category VARCHAR(20) NOT NULL CHECK (risk_category IN ('CONSERVATIVE', 'MODERATE', 'AGGRESSIVE')),
    volatility_score DECIMAL(5, 2) DEFAULT 0.00,
    diversification_score DECIMAL(5, 2) DEFAULT 0.00,
    max_loss_tolerance DECIMAL(5, 2) DEFAULT 0.00,
    investment_horizon VARCHAR(20) DEFAULT 'MEDIUM',
    last_analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_risk_category (risk_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

