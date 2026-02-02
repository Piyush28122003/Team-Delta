# Financial Portfolio Management System

A comprehensive Spring Boot application for managing financial portfolios with AI-powered chatbot assistance, risk analysis, and real-time stock market integration.

## 🚀 Features

- **User & Bank Account Management**: User registration, login, and bank account tracking
- **Portfolio Management**: Buy/sell stocks, track holdings, calculate portfolio value
- **Real-time Stock Data**: Integration with Alpha Vantage/Finnhub APIs for live stock prices
- **Risk Analysis**: Automated risk profiling (Conservative/Moderate/Aggressive)
- **AI Chatbot**: Google Gemini-powered chatbot for portfolio queries and recommendations
- **Analytics & Charts**: Portfolio visualization with Chart.js (pie charts, line charts)
- **RESTful API**: Clean API design with DTOs and proper exception handling

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.0, Maven
- **Database**: MySQL
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security (BCrypt password encoding)
- **Frontend**: HTML, CSS, JavaScript, Chart.js
- **External APIs**: 
  - Stock Market: Alpha Vantage / Finnhub
  - AI Chatbot: Google Gemini API

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Node.js (optional, for frontend development)

## 🔧 Setup Instructions

### 1. Database Setup

```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE portfolio_db;
exit;
```

### 2. Configure Application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/portfolio_db?createDatabaseIfNotExist=true
    username: your_username
    password: your_password

external:
  stock-api:
    alpha-vantage:
      api-key: YOUR_ALPHA_VANTAGE_API_KEY
    finnhub:
      api-key: YOUR_FINNHUB_API_KEY
  gemini:
    api-key: YOUR_GEMINI_API_KEY
```

**Note**: Get API keys from:
- Alpha Vantage: https://www.alphavantage.co/support/#api-key
- Finnhub: https://finnhub.io/
- Google Gemini: https://makersuite.google.com/app/apikey

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access the Frontend

Open your browser and navigate to:
```
http://localhost:8080/index.html
```

## 📁 Project Structure

```
portfolio-manager/
├── src/main/java/com/yourorg/portfolio/
│   ├── PortfolioManagerApplication.java
│   ├── config/          # Security, WebClient configuration
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic layer
│   ├── repository/       # Data access layer
│   ├── model/            # JPA entities
│   ├── dto/              # Data Transfer Objects
│   ├── util/             # Utility classes
│   └── exception/        # Exception handling
├── src/main/resources/
│   ├── application.yml   # Application configuration
│   ├── schema.sql        # Database schema
│   ├── data.sql          # Sample data
│   └── static/           # Frontend files (HTML, CSS, JS)
└── pom.xml
```

## 🔌 API Endpoints

### User Management
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Portfolio
- `GET /api/portfolio/user/{userId}` - Get portfolio by user ID
- `POST /api/portfolio/buy` - Buy stock (params: userId, symbol, quantity, buyPrice)
- `POST /api/portfolio/sell` - Sell stock (params: userId, investmentId, quantity)

### Stocks
- `GET /api/stocks/price/{symbol}` - Get current stock price
- `GET /api/stocks/trending` - Get top 10 trending stocks
- `GET /api/stocks/search?query={query}` - Search stocks
- `GET /api/stocks` - Get all stocks

### Risk Analysis
- `GET /api/risk/analyze/{userId}` - Analyze user's risk profile

### Chatbot
- `POST /api/chatbot/chat` - Send message to chatbot
- `POST /api/chatbot/clear-consent/{userId}` - Clear user consent

## 📊 Sample API Requests

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Get Portfolio
```bash
curl http://localhost:8080/api/portfolio/user/1
```

### Buy Stock
```bash
curl -X POST "http://localhost:8080/api/portfolio/buy?userId=1&symbol=AAPL&quantity=10&buyPrice=150.00"
```

### Chatbot Query
```bash
curl -X POST http://localhost:8080/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "message": "What is my portfolio value?",
    "consentGiven": true
  }'
```

## 🗄️ Database Schema

The application uses a normalized database design with the following tables:
- `users` - User accounts
- `bank_accounts` - Bank account information
- `portfolios` - User portfolios
- `stocks` - Stock reference data
- `investments` - User stock holdings
- `risk_profiles` - User risk profiles

See `src/main/resources/schema.sql` for complete schema.

## 🧪 Testing

Sample data is provided in `src/main/resources/data.sql`:
- 3 sample users (John Doe, Jane Smith, Bob Wilson)
- Bank accounts with initial balances
- Sample portfolios with investments
- Risk profiles

Default password for all users: `password123`

## 🔐 Security Notes

- Passwords are hashed using BCrypt
- Currently, all endpoints are open (for development)
- JWT authentication can be added for production
- API keys should be stored as environment variables in production

## 🚧 Future Enhancements

- JWT-based authentication
- Real-time WebSocket updates for stock prices
- Historical performance tracking
- Email notifications
- Mobile app support
- Quantum portfolio optimization (conceptual)

## 📝 License

This project is for training and evaluation purposes.

## 👥 Support

For issues or questions, please refer to the code comments or Spring Boot documentation.

---

**Built with ❤️ using Spring Boot and modern web technologies**

