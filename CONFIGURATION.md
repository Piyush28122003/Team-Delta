# Configuration Guide

## 📁 Configuration File Location

All application configuration is stored in:
```
src/main/resources/application.yml
```

## 🔧 How to Configure

### Option 1: Direct Configuration (Quick Setup)

Edit `src/main/resources/application.yml` directly:

```yaml
spring:
  datasource:
    username: your_mysql_username
    password: your_mysql_password

external:
  stock-api:
    alpha-vantage:
      api-key: your_alpha_vantage_key_here
    finnhub:
      api-key: your_finnhub_key_here
  gemini:
    api-key: your_gemini_api_key_here
```

### Option 2: Environment Variables (Recommended for Production)

The application already supports environment variables. Set them in your system:

**On Linux/Mac:**
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export ALPHA_VANTAGE_API_KEY=your_key
export FINNHUB_API_KEY=your_key
export GEMINI_API_KEY=your_key
```

**On Windows (Command Prompt):**
```cmd
set DB_USERNAME=root
set DB_PASSWORD=your_password
set ALPHA_VANTAGE_API_KEY=your_key
set FINNHUB_API_KEY=your_key
set GEMINI_API_KEY=your_key
```

**On Windows (PowerShell):**
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:ALPHA_VANTAGE_API_KEY="your_key"
$env:FINNHUB_API_KEY="your_key"
$env:GEMINI_API_KEY="your_key"
```

### Option 3: Using .env File (with Spring Boot plugin)

1. Create a `.env` file in the project root (see `.env.example`)
2. Install `spring-boot-dotenv` plugin (optional)
3. Or use a tool like `dotenv-java`

## 🔑 Where to Get API Keys

### 1. Alpha Vantage API Key (Stock Market Data)
- **URL**: https://www.alphavantage.co/support/#api-key
- **Free Tier**: 5 API calls per minute, 500 calls per day
- **Usage**: Real-time and historical stock data

### 2. Finnhub API Key (Alternative Stock Data)
- **URL**: https://finnhub.io/
- **Free Tier**: 60 API calls per minute
- **Usage**: Alternative stock market data provider

### 3. Google Gemini API Key (AI Chatbot)
- **URL**: https://makersuite.google.com/app/apikey
- **Free Tier**: Limited free usage
- **Usage**: AI-powered chatbot for portfolio queries

## 🗄️ Database Configuration

### MySQL Setup

1. **Install MySQL** (if not already installed)
2. **Create Database**:
   ```sql
   CREATE DATABASE portfolio_db;
   ```
3. **Update credentials** in `application.yml`:
   ```yaml
   spring:
     datasource:
       username: your_mysql_username
       password: your_mysql_password
   ```

### Default Database Settings
- **Host**: localhost
- **Port**: 3306
- **Database**: portfolio_db
- **Username**: root (default)
- **Password**: root (default - CHANGE THIS!)

## 📝 Configuration Sections Explained

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/portfolio_db
    username: ${DB_USERNAME:root}  # Uses env var or defaults to 'root'
    password: ${DB_PASSWORD:root}  # Uses env var or defaults to 'root'
```

### Stock API Configuration
```yaml
external:
  stock-api:
    provider: alpha-vantage  # Choose: alpha-vantage, finnhub, or yahoo-finance
    alpha-vantage:
      api-key: ${ALPHA_VANTAGE_API_KEY:demo}
    finnhub:
      api-key: ${FINNHUB_API_KEY:demo}
```

### Gemini Chatbot Configuration
```yaml
external:
  gemini:
    api-key: ${GEMINI_API_KEY:your-gemini-api-key-here}
    model: gemini-pro
```

## ⚠️ Security Best Practices

1. **Never commit API keys to Git**
   - Add `.env` to `.gitignore`
   - Use environment variables in production

2. **Use Strong Database Passwords**
   - Don't use default 'root' password in production

3. **Rotate API Keys Regularly**
   - Especially if keys are exposed

4. **Use Different Keys for Dev/Prod**
   - Separate API keys for development and production

## 🚀 Quick Start Configuration

For quick testing, you can use these minimal settings:

```yaml
spring:
  datasource:
    username: root
    password: root  # Change this!

external:
  stock-api:
    alpha-vantage:
      api-key: demo  # Will use mock data
  gemini:
    api-key: your-gemini-api-key-here  # Required for chatbot
```

**Note**: Without API keys, the application will use mock data for stock prices, which is fine for testing.

## 🔍 Verifying Configuration

After setting up, verify your configuration:

1. **Check Database Connection**: Application will fail to start if DB credentials are wrong
2. **Test Stock API**: Try fetching a stock price via `/api/stocks/price/AAPL`
3. **Test Chatbot**: Try sending a message via `/api/chatbot/chat`

## 📚 Additional Resources

- Spring Boot Configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
- Environment Variables: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config

