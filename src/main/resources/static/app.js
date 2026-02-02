const API_BASE_URL = 'http://localhost:8080/api';

let currentUserId = null;
let assetAllocationChart = null;
let portfolioGrowthChart = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    checkAuthentication();
    setupNavigation();
    loadDashboard();
    loadUserInfo();
});

// Authentication check
function checkAuthentication() {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    
    if (!token || !userId) {
        // Not authenticated, redirect to login
        window.location.href = 'login.html';
        return;
    }
    
    // Set current user ID
    currentUserId = parseInt(userId);
    
    // Display welcome message
    const firstName = localStorage.getItem('firstName') || 'User';
    const lastName = localStorage.getItem('lastName') || '';
    document.getElementById('welcomeMessage').textContent = `Welcome, ${firstName} ${lastName}`;
}

// Logout function
function handleLogout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('firstName');
        localStorage.removeItem('lastName');
        window.location.href = 'login.html';
    }
}

// Navigation
function setupNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            showPage(page);
        });
    });
}

function showPage(pageName) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    document.getElementById(pageName).classList.add('active');
    document.querySelector(`[data-page="${pageName}"]`).classList.add('active');
    
    if (pageName === 'dashboard') {
        loadDashboard();
    } else if (pageName === 'holdings') {
        loadHoldings();
    } else if (pageName === 'performance') {
        loadPerformance();
    } else if (pageName === 'settings') {
        loadUserInfo();
        loadBankAccountInfo();
    }
}

// Dashboard
function loadDashboard() {
    if (!currentUserId) {
        checkAuthentication();
        return;
    }
    loadPortfolio();
    loadTrendingStocks();
}

async function loadPortfolio() {
    try {
        const response = await fetch(`${API_BASE_URL}/portfolio/user/${currentUserId}`);
        const portfolio = await response.json();
        
        // Update summary cards
        document.getElementById('totalValue').textContent = formatCurrency(portfolio.totalValue);
        document.getElementById('totalCost').textContent = formatCurrency(portfolio.totalCost);
        
        const profitLoss = portfolio.totalProfitLoss;
        const profitLossElement = document.getElementById('profitLoss');
        profitLossElement.textContent = formatCurrency(profitLoss);
        profitLossElement.className = profitLoss >= 0 ? 'value positive' : 'value negative';
        
        const profitLossPercent = portfolio.totalProfitLossPercentage;
        const profitLossPercentElement = document.getElementById('profitLossPercent');
        profitLossPercentElement.textContent = formatPercent(profitLossPercent);
        profitLossPercentElement.className = profitLossPercent >= 0 ? 'value positive' : 'value negative';
        
        // Update holdings table
        updateHoldingsTable(portfolio.holdings);
        
        // Update charts
        updateAssetAllocationChart(portfolio.assetAllocation);
        updatePortfolioGrowthChart(portfolio.holdings);
    } catch (error) {
        console.error('Error loading portfolio:', error);
        alert('Error loading portfolio data');
    }
}

function updateHoldingsTable(holdings) {
    const tbody = document.getElementById('holdingsTableBody');
    tbody.innerHTML = '';
    
    holdings.slice(0, 10).forEach(holding => {
        const row = tbody.insertRow();
        row.innerHTML = `
            <td><strong>${holding.symbol}</strong></td>
            <td>${holding.companyName}</td>
            <td>${holding.quantity}</td>
            <td>${formatCurrency(holding.buyPrice)}</td>
            <td>${formatCurrency(holding.currentPrice)}</td>
            <td>${formatCurrency(holding.currentValue)}</td>
            <td class="${holding.profitLoss >= 0 ? 'positive' : 'negative'}">${formatCurrency(holding.profitLoss)}</td>
            <td class="${holding.profitLossPercentage >= 0 ? 'positive' : 'negative'}">${formatPercent(holding.profitLossPercentage)}</td>
        `;
    });
}

function updateAssetAllocationChart(allocation) {
    const ctx = document.getElementById('assetAllocationChart').getContext('2d');
    
    if (assetAllocationChart) {
        assetAllocationChart.destroy();
    }
    
    assetAllocationChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['Stocks', 'Bonds', 'Crypto', 'Cash'],
            datasets: [{
                data: [
                    allocation.stocks || 0,
                    allocation.bonds || 0,
                    allocation.crypto || 0,
                    allocation.cash || 0
                ],
                backgroundColor: [
                    '#667eea',
                    '#764ba2',
                    '#f093fb',
                    '#4facfe'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true
        }
    });
}

function updatePortfolioGrowthChart(holdings) {
    const ctx = document.getElementById('portfolioGrowthChart').getContext('2d');
    
    if (portfolioGrowthChart) {
        portfolioGrowthChart.destroy();
    }
    
    // Mock historical data (in real app, fetch from API)
    const labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];
    const data = [10000, 12000, 11500, 13000, 12500, 14000];
    
    portfolioGrowthChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Portfolio Value',
                data: data,
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            scales: {
                y: {
                    beginAtZero: false
                }
            }
        }
    });
}

// Holdings
async function loadHoldings() {
    try {
        const response = await fetch(`${API_BASE_URL}/portfolio/user/${currentUserId}`);
        const portfolio = await response.json();
        
        const tbody = document.getElementById('allHoldingsTableBody');
        tbody.innerHTML = '';
        
        portfolio.holdings.forEach(holding => {
            const row = tbody.insertRow();
            row.innerHTML = `
                <td><strong>${holding.symbol}</strong></td>
                <td>${holding.companyName}</td>
                <td>${holding.quantity}</td>
                <td>${formatCurrency(holding.buyPrice)}</td>
                <td>${holding.buyDate}</td>
                <td>${formatCurrency(holding.currentPrice)}</td>
                <td>${formatCurrency(holding.currentValue)}</td>
                <td class="${holding.profitLoss >= 0 ? 'positive' : 'negative'}">${formatCurrency(holding.profitLoss)}</td>
                <td class="${holding.profitLossPercentage >= 0 ? 'positive' : 'negative'}">${formatPercent(holding.profitLossPercentage)}</td>
                <td>
                    <button class="btn btn-secondary" onclick="showSellStockModal(${holding.investmentId}, ${holding.quantity})">Sell</button>
                </td>
            `;
        });
    } catch (error) {
        console.error('Error loading holdings:', error);
        alert('Error loading holdings');
    }
}

// Performance
async function loadPerformance() {
    try {
        const response = await fetch(`${API_BASE_URL}/risk/analyze/${currentUserId}`);
        const analysis = await response.json();
        
        document.getElementById('riskCategory').textContent = analysis.riskCategory;
        document.getElementById('volatilityScore').textContent = analysis.volatilityScore + '/10';
        document.getElementById('diversificationScore').textContent = analysis.diversificationScore + '/10';
        document.getElementById('maxLossTolerance').textContent = analysis.maxLossTolerance + '%';
        
        const detailsDiv = document.getElementById('riskDetails');
        detailsDiv.innerHTML = `
            <p><strong>Risk Level:</strong> ${analysis.riskLevel}</p>
            <p><strong>Recommendation:</strong> ${analysis.recommendation}</p>
            <h4>Risk Factors:</h4>
            <ul>
                ${analysis.riskFactors.map(factor => `<li>${factor}</li>`).join('')}
            </ul>
            <h4>Suggestions:</h4>
            <ul>
                ${analysis.suggestions.map(suggestion => `<li>${suggestion}</li>`).join('')}
            </ul>
        `;
    } catch (error) {
        console.error('Error loading performance:', error);
        alert('Error loading performance data');
    }
}

// Chatbot
function handleChatKeyPress(event) {
    if (event.key === 'Enter') {
        sendChatMessage();
    }
}

async function sendChatMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    // Add user message to chat
    addChatMessage(message, 'user');
    input.value = '';
    
    try {
        const response = await fetch(`${API_BASE_URL}/chatbot/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: parseInt(currentUserId),
                message: message,
                consentGiven: true
            })
        });
        
        const data = await response.json();
        addChatMessage(data.response, 'bot');
        
        // Update quick actions if provided
        if (data.quickActions) {
            updateQuickActions(data.quickActions);
        }
    } catch (error) {
        console.error('Error sending message:', error);
        addChatMessage('Sorry, I encountered an error. Please try again.', 'bot');
    }
}

function sendQuickAction(action) {
    document.getElementById('chatInput').value = action;
    sendChatMessage();
}

function addChatMessage(message, type) {
    const messagesDiv = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;
    messageDiv.innerHTML = `<p>${message.replace(/\n/g, '<br>')}</p>`;
    messagesDiv.appendChild(messageDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function updateQuickActions(actions) {
    const quickActionsDiv = document.getElementById('quickActions');
    quickActionsDiv.innerHTML = actions.map(action => 
        `<button class="btn btn-secondary" onclick="sendQuickAction('${action}')">${action}</button>`
    ).join('');
}

// Buy/Sell Stock
async function showBuyStockModal() {
    const modal = document.getElementById('buyStockModal');
    modal.style.display = 'block';
    
    // Load current balance
    try {
        const response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}`);
        if (response.ok) {
            const account = await response.json();
            document.getElementById('availableBalance').textContent = formatCurrency(account.currentBalance);
        } else {
            document.getElementById('availableBalance').textContent = 'No account found';
        }
    } catch (error) {
        document.getElementById('availableBalance').textContent = 'Error loading balance';
    }
    
    // Calculate total cost as user types
    const quantityInput = document.getElementById('quantity');
    const priceInput = document.getElementById('buyPrice');
    const totalCostDiv = document.getElementById('totalCostInfo');
    const totalCostDisplay = document.getElementById('totalCostDisplay');
    
    function updateTotalCost() {
        const quantity = parseInt(quantityInput.value) || 0;
        const price = parseFloat(priceInput.value) || 0;
        if (quantity > 0 && price > 0) {
            const total = quantity * price;
            totalCostDisplay.textContent = formatCurrency(total);
            totalCostDiv.style.display = 'block';
        } else {
            totalCostDiv.style.display = 'none';
        }
    }
    
    quantityInput.addEventListener('input', updateTotalCost);
    priceInput.addEventListener('input', updateTotalCost);
}

function closeBuyStockModal() {
    document.getElementById('buyStockModal').style.display = 'none';
    document.getElementById('buyStockForm').reset();
    document.getElementById('totalCostInfo').style.display = 'none';
}

async function buyStock(event) {
    event.preventDefault();
    
    const symbol = document.getElementById('stockSymbol').value.toUpperCase();
    const quantity = parseInt(document.getElementById('quantity').value);
    const buyPrice = parseFloat(document.getElementById('buyPrice').value);
    
    // Check balance before attempting purchase
    try {
        const balanceResponse = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}`);
        if (balanceResponse.ok) {
            const account = await balanceResponse.json();
            const totalCost = buyPrice * quantity;
            
            if (account.currentBalance < totalCost) {
                alert(`Insufficient balance! You have ${formatCurrency(account.currentBalance)} but need ${formatCurrency(totalCost)}.`);
                return;
            }
        }
    } catch (error) {
        console.error('Error checking balance:', error);
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/portfolio/buy?userId=${currentUserId}&symbol=${symbol}&quantity=${quantity}&buyPrice=${buyPrice}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            alert('Stock purchased successfully!');
            closeBuyStockModal();
            loadDashboard();
            loadHoldings();
            if (document.getElementById('settings').classList.contains('active')) {
                loadBankAccountInfo();
            }
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Failed to buy stock'));
        }
    } catch (error) {
        console.error('Error buying stock:', error);
        alert('Error buying stock');
    }
}

function showSellStockModal(investmentId, maxQuantity) {
    const quantity = prompt(`Enter quantity to sell (max: ${maxQuantity}):`, maxQuantity);
    if (quantity && quantity > 0 && quantity <= maxQuantity) {
        sellStock(investmentId, parseInt(quantity));
    }
}

async function sellStock(investmentId, quantity) {
    try {
        const response = await fetch(`${API_BASE_URL}/portfolio/sell?userId=${currentUserId}&investmentId=${investmentId}&quantity=${quantity}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            alert('Stock sold successfully!');
            loadDashboard();
            loadHoldings();
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Failed to sell stock'));
        }
    } catch (error) {
        console.error('Error selling stock:', error);
        alert('Error selling stock');
    }
}

// User Info
async function loadUserInfo() {
    try {
        const response = await fetch(`${API_BASE_URL}/users/${currentUserId}`);
        const user = await response.json();
        
        document.getElementById('userInfo').innerHTML = `
            <p><strong>Name:</strong> ${user.firstName} ${user.lastName}</p>
            <p><strong>Email:</strong> ${user.email}</p>
            <p><strong>Username:</strong> ${user.username}</p>
            <p><strong>Phone:</strong> ${user.phone || 'N/A'}</p>
        `;
    } catch (error) {
        console.error('Error loading user info:', error);
    }
}

// Bank Account Info
async function loadBankAccountInfo() {
    try {
        const response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}`);
        
        if (response.ok) {
            const account = await response.json();
            
            document.getElementById('bankAccountInfo').innerHTML = `
                <p><strong>Account Number:</strong> ${account.accountNumber}</p>
                <p><strong>Bank Name:</strong> ${account.bankName}</p>
                <p><strong>Balance:</strong> <span style="font-size: 24px; color: #667eea; font-weight: bold;">${formatCurrency(account.currentBalance)}</span></p>
                <p><strong>Account Type:</strong> ${account.accountType}</p>
            `;
            
            // Show edit, deposit, and withdraw buttons
            document.getElementById('createAccountBtn').style.display = 'none';
            document.getElementById('editAccountBtn').style.display = 'inline-block';
            document.getElementById('depositBtn').style.display = 'inline-block';
            document.getElementById('withdrawBtn').style.display = 'inline-block';
            
            // Store account data for editing
            window.currentBankAccount = account;
        } else {
            // No bank account exists
            document.getElementById('bankAccountInfo').innerHTML = `
                <p style="color: #666;">No bank account found. Create one to start managing your funds.</p>
            `;
            
            // Show create button only
            document.getElementById('createAccountBtn').style.display = 'inline-block';
            document.getElementById('editAccountBtn').style.display = 'none';
            document.getElementById('depositBtn').style.display = 'none';
            document.getElementById('withdrawBtn').style.display = 'none';
        }
    } catch (error) {
        console.error('Error loading bank account info:', error);
        document.getElementById('bankAccountInfo').innerHTML = `
            <p style="color: #dc3545;">Error loading bank account information.</p>
        `;
    }
}

// Bank Account Modal Functions
function showBankAccountModal() {
    const modal = document.getElementById('bankAccountModal');
    const title = document.getElementById('bankAccountModalTitle');
    const form = document.getElementById('bankAccountForm');
    
    if (window.currentBankAccount) {
        // Edit mode
        title.textContent = 'Edit Bank Account';
        document.getElementById('accountNumber').value = window.currentBankAccount.accountNumber;
        document.getElementById('bankName').value = window.currentBankAccount.bankName;
        document.getElementById('accountType').value = window.currentBankAccount.accountType;
    } else {
        // Create mode
        title.textContent = 'Create Bank Account';
        form.reset();
    }
    
    modal.style.display = 'block';
}

function closeBankAccountModal() {
    document.getElementById('bankAccountModal').style.display = 'none';
    document.getElementById('bankAccountForm').reset();
}

async function saveBankAccount(event) {
    event.preventDefault();
    
    const accountNumber = document.getElementById('accountNumber').value.trim();
    const bankName = document.getElementById('bankName').value.trim();
    const accountType = document.getElementById('accountType').value;
    
    try {
        let response;
        if (window.currentBankAccount) {
            // Update existing account
            response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}/update`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    accountNumber: accountNumber,
                    bankName: bankName,
                    accountType: accountType
                })
            });
        } else {
            // Create new account
            response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    accountNumber: accountNumber,
                    bankName: bankName,
                    accountType: accountType
                })
            });
        }
        
        if (response.ok) {
            alert('Bank account saved successfully!');
            closeBankAccountModal();
            loadBankAccountInfo();
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Failed to save bank account'));
        }
    } catch (error) {
        console.error('Error saving bank account:', error);
        alert('Error saving bank account');
    }
}

// Deposit Modal Functions
function showDepositModal() {
    document.getElementById('depositModal').style.display = 'block';
}

function closeDepositModal() {
    document.getElementById('depositModal').style.display = 'none';
    document.getElementById('depositForm').reset();
}

async function depositMoney(event) {
    event.preventDefault();
    
    const amount = parseFloat(document.getElementById('depositAmount').value);
    const description = document.getElementById('depositDescription').value.trim();
    
    if (amount <= 0) {
        alert('Amount must be greater than 0');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}/deposit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                amount: amount,
                description: description || null
            })
        });
        
        if (response.ok) {
            const account = await response.json();
            alert(`Successfully deposited ${formatCurrency(amount)}. New balance: ${formatCurrency(account.currentBalance)}`);
            closeDepositModal();
            loadBankAccountInfo();
            loadDashboard(); // Refresh dashboard to show updated balance
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Failed to deposit money'));
        }
    } catch (error) {
        console.error('Error depositing money:', error);
        alert('Error depositing money');
    }
}

// Withdraw Modal Functions
async function showWithdrawModal() {
    const modal = document.getElementById('withdrawModal');
    modal.style.display = 'block';
    
    // Load current balance
    try {
        const response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}`);
        if (response.ok) {
            const account = await response.json();
            document.getElementById('withdrawAvailableBalance').textContent = formatCurrency(account.currentBalance);
        } else {
            document.getElementById('withdrawAvailableBalance').textContent = 'No account found';
        }
    } catch (error) {
        document.getElementById('withdrawAvailableBalance').textContent = 'Error loading balance';
    }
}

function closeWithdrawModal() {
    document.getElementById('withdrawModal').style.display = 'none';
    document.getElementById('withdrawForm').reset();
}

async function withdrawMoney(event) {
    event.preventDefault();
    
    const amount = parseFloat(document.getElementById('withdrawAmount').value);
    const description = document.getElementById('withdrawDescription').value.trim();
    
    if (amount <= 0) {
        alert('Amount must be greater than 0');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/bank-account/user/${currentUserId}/withdraw`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                amount: amount,
                description: description || null
            })
        });
        
        if (response.ok) {
            const account = await response.json();
            alert(`Successfully withdrew ${formatCurrency(amount)}. New balance: ${formatCurrency(account.currentBalance)}`);
            closeWithdrawModal();
            loadBankAccountInfo();
            loadDashboard(); // Refresh dashboard to show updated balance
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Failed to withdraw money'));
        }
    } catch (error) {
        console.error('Error withdrawing money:', error);
        alert('Error withdrawing money');
    }
}

// Trending Stocks
async function loadTrendingStocks() {
    try {
        const response = await fetch(`${API_BASE_URL}/stocks/trending`);
        const stocks = await response.json();
        // Can display trending stocks in a widget if needed
    } catch (error) {
        console.error('Error loading trending stocks:', error);
    }
}

// Utility Functions
function formatCurrency(value) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(value || 0);
}

function formatPercent(value) {
    return (value || 0).toFixed(2) + '%';
}

