# 🚀 How to Start the Portfolio Management Project

## Prerequisites Checklist

Before starting, ensure you have:
- ✅ Java 17 or higher installed
- ✅ Maven 3.6+ installed
- ✅ MySQL 8.0+ installed and running
- ✅ MySQL database `hsbc` created (or it will be auto-created)

## Step-by-Step Startup Guide

### Step 1: Verify Prerequisites

**Check Java Version:**
```bash
java -version
# Should show Java 17 or higher
```

**Check Maven:**
```bash
mvn -version
# Should show Maven 3.6 or higher
```

**Check MySQL:**
```bash
mysql --version
# Should show MySQL 8.0 or higher
```

### Step 2: Start MySQL Server

**On Mac/Linux:**
```bash
# Start MySQL service
sudo systemctl start mysql
# OR
brew services start mysql
```

**On Windows:**
- Open Services (services.msc)
- Find "MySQL" service
- Right-click → Start

**Verify MySQL is running:**
```bash
mysql -u root -p
# Enter your password: piyush@2812
# Type: exit; to leave MySQL prompt
```

### Step 3: Create Database (Optional - Auto-created)

The application will auto-create the database if it doesn't exist, but you can create it manually:

```bash
mysql -u root -p
```

Then in MySQL prompt:
```sql
CREATE DATABASE IF NOT EXISTS hsbc;
exit;
```

### Step 4: Configure Application (Already Done)

Your `application.yml` is configured with:
- Database: `hsbc`
- Username: `root`
- Password: `piyush@2812`

**Note**: If you need to change these, edit `src/main/resources/application.yml`

### Step 5: Build the Project

Navigate to project directory:
```bash
cd /Users/piyushsrivastava/Desktop/HSBC_PROJECT
```

Build with Maven:
```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the code
- Run tests (if any)
- Create the JAR file

**Expected output**: `BUILD SUCCESS`

### Step 6: Run the Application

**Option A: Using Maven (Recommended)**
```bash
mvn spring-boot:run
```

**Option B: Using Java directly**
```bash
java -jar target/portfolio-manager-1.0.0.jar
```

### Step 7: Verify Application is Running

Look for these messages in the console:
```
Started PortfolioManagerApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### Step 8: Access the Application

**Frontend Dashboard:**
```
http://localhost:8080/index.html
```

**API Health Check:**
```
http://localhost:8080/api/users
```

**Test API:**
```bash
curl http://localhost:8080/api/users/1
```

## 🎯 Quick Start Commands

```bash
# 1. Navigate to project
cd /Users/piyushsrivastava/Desktop/HSBC_PROJECT

# 2. Build project
mvn clean install

# 3. Run application
mvn spring-boot:run

# 4. Open browser
open http://localhost:8080/index.html
```

## 🔧 Troubleshooting

### Issue: Port 8080 already in use
**Solution:**
```bash
# Find process using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
# OR change port in application.yml
```

### Issue: Database connection failed
**Solution:**
1. Verify MySQL is running: `mysql -u root -p`
2. Check credentials in `application.yml`
3. Ensure database `hsbc` exists or allow auto-creation

### Issue: Maven build fails
**Solution:**
```bash
# Clean and rebuild
mvn clean
mvn install -U
```

### Issue: Dependencies not downloading
**Solution:**
```bash
# Update Maven dependencies
mvn dependency:resolve
```

## 📊 Verify Everything Works

1. **Check Database Tables:**
```bash
mysql -u root -p
USE hsbc;
SHOW TABLES;
# Should show: users, bank_accounts, portfolios, stocks, investments, risk_profiles
```

2. **Test API Endpoints:**
```bash
# Get all users
curl http://localhost:8080/api/users

# Get portfolio
curl http://localhost:8080/api/portfolio/user/1

# Get stock price
curl http://localhost:8080/api/stocks/price/AAPL
```

3. **Check Frontend:**
- Open `http://localhost:8080/index.html`
- Should see dashboard with sample data

## 🎉 Success Indicators

You'll know it's working when:
- ✅ Application starts without errors
- ✅ Database tables are created
- ✅ Frontend loads at `http://localhost:8080/index.html`
- ✅ API endpoints return data
- ✅ You can see sample users (John Doe, Jane Smith, Bob Wilson)

## 📝 Next Steps After Starting

1. **Test the Frontend:**
   - Select different users from dropdown
   - View portfolio holdings
   - Try the AI chatbot

2. **Test API Endpoints:**
   - Use Postman or curl
   - Try buying/selling stocks
   - Test risk analysis

3. **Configure API Keys (Optional):**
   - Add Alpha Vantage API key for real stock data
   - Add Gemini API key for chatbot
   - See `CONFIGURATION.md` for details

## 🆘 Need Help?

- Check logs in console for error messages
- Verify MySQL is running: `mysql -u root -p`
- Check port 8080 is available
- Review `application.yml` configuration

---

**Happy Coding! 🚀**

