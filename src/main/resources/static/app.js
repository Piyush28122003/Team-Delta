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
    
    // Set user avatar initial
    const avatarEl = document.getElementById('userAvatar');
    if (avatarEl && firstName) {
        avatarEl.textContent = (firstName[0] || 'U').toUpperCase();
    }
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
    } else if (pageName === 'news') {
        loadNews();
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
        
        // Update charts (per-stock data + details)
        updateAssetAllocationChart(portfolio.assetAllocation, portfolio.holdings, portfolio.totalValue);
        updatePortfolioGrowthChart(portfolio.holdings, portfolio.totalValue);
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

function updateAssetAllocationChart(allocation, holdings, totalValue) {
    const ctx = document.getElementById('assetAllocationChart').getContext('2d');
    if (assetAllocationChart) assetAllocationChart.destroy();

    const total = Number(totalValue) || 1;
    const stockColors = ['#4c6fff', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'];

    if (holdings && holdings.length > 0) {
        const labels = holdings.map(h => h.symbol + (h.companyName ? ' · ' + (h.companyName.length > 12 ? h.companyName.substring(0, 12) + '…' : h.companyName) : ''));
        const data = holdings.map(h => Number(h.currentValue) || 0);
        const colors = holdings.map((_, i) => stockColors[i % stockColors.length]);

        assetAllocationChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                layout: { padding: { bottom: 8 } },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxWidth: 10, boxHeight: 10, font: { size: 11 }, padding: 6 }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const h = holdings[context.dataIndex];
                                const pct = total > 0 ? ((Number(h.currentValue) / total) * 100).toFixed(1) : 0;
                                const pl = Number(h.profitLoss) || 0;
                                const plPct = Number(h.profitLossPercentage) != null ? Number(h.profitLossPercentage).toFixed(1) : '-';
                                return [
                                    (h.companyName || h.symbol),
                                    'Value: ' + formatCurrency(h.currentValue) + ' (' + pct + '% of portfolio)',
                                    'P/L: ' + formatCurrency(pl) + ' (' + plPct + '%)'
                                ];
                            }
                        }
                    }
                }
            }
        });
    } else {
        const labels = ['Stocks', 'Bonds', 'Crypto', 'Cash'];
        const data = [
            Number(allocation && allocation.stocks) || 0,
            Number(allocation && allocation.bonds) || 0,
            Number(allocation && allocation.crypto) || 0,
            Number(allocation && allocation.cash) || 0
        ];
        assetAllocationChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: ['#667eea', '#764ba2', '#f093fb', '#4facfe'],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
            responsive: true,
            maintainAspectRatio: true,
            layout: { padding: { bottom: 8 } },
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { boxWidth: 10, boxHeight: 10, font: { size: 11 }, padding: 6 }
                }
            }
        }
        });
    }
}

function updatePortfolioGrowthChart(holdings, totalValue) {
    const ctx = document.getElementById('portfolioGrowthChart').getContext('2d');
    if (portfolioGrowthChart) portfolioGrowthChart.destroy();

    if (!holdings || holdings.length === 0) {
        portfolioGrowthChart = new Chart(ctx, {
            type: 'bar',
            data: { labels: ['No holdings'], datasets: [{ label: 'Value', data: [0], backgroundColor: '#e5e7eb' }] },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: { legend: { display: false }, tooltip: { enabled: false } },
                scales: { y: { beginAtZero: true } }
            }
        });
        return;
    }

    const labels = holdings.map(h => h.symbol);
    const currentValues = holdings.map(h => Number(h.currentValue) || 0);
    const costValues = holdings.map(h => (Number(h.quantity) || 0) * (Number(h.buyPrice) || 0));
    const total = Number(totalValue) || 1;

    portfolioGrowthChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Current value',
                    data: currentValues,
                    backgroundColor: 'rgba(79, 70, 229, 0.7)',
                    borderColor: '#4c6fff',
                    borderWidth: 1
                },
                {
                    label: 'Cost basis',
                    data: costValues,
                    backgroundColor: 'rgba(156, 163, 175, 0.6)',
                    borderColor: '#9ca3af',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            indexAxis: 'y',
            scales: {
                x: {
                    stacked: false,
                    beginAtZero: true,
                    ticks: { callback: function(v) { return '$' + (v >= 1000 ? (v/1000).toFixed(1) + 'k' : v); } }
                },
                y: {
                    stacked: false,
                    grid: { display: false }
                }
            },
            plugins: {
                legend: { position: 'top' },
                tooltip: {
                    callbacks: {
                        afterBody: function(context) {
                            const ctx = Array.isArray(context) ? context[0] : context;
                            const i = ctx && (ctx.dataIndex != null ? ctx.dataIndex : ctx.datasetIndex);
                            if (i == null || !holdings[i]) return [];
                            const h = holdings[i];
                            const pct = total > 0 ? ((Number(h.currentValue) / total) * 100).toFixed(1) : 0;
                            const plPct = h.profitLossPercentage != null ? Number(h.profitLossPercentage).toFixed(1) : '-';
                            return [
                                '──',
                                'Share of portfolio: ' + pct + '%',
                                'Quantity: ' + (h.quantity || 0),
                                'Buy price: ' + formatCurrency(h.buyPrice),
                                'Current price: ' + formatCurrency(h.currentPrice),
                                'P/L: ' + formatCurrency(h.profitLoss) + ' (' + plPct + '%)'
                            ];
                        },
                        title: function(context) {
                            const ctx = Array.isArray(context) ? context[0] : context;
                            const i = ctx && (ctx.dataIndex != null ? ctx.dataIndex : 0);
                            const h = holdings[i];
                            return h ? (h.companyName || h.symbol) + ' (' + h.symbol + ')' : '';
                        }
                    }
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
            <div class="risk-section">
                <h4>Your Risk Level — In Plain Terms</h4>
                <p>${analysis.riskLevelPlain || analysis.riskLevel}</p>
            </div>
            <div class="risk-section">
                <h4>Our Recommendation For You</h4>
                <p>${analysis.recommendation}</p>
            </div>
            <div class="risk-section">
                <h4>What Could Go Wrong? (Things to Watch)</h4>
                <p class="section-desc">These are potential weak spots in your portfolio. Knowing them helps you make better decisions.</p>
                <ul>
                    ${analysis.riskFactors.map(factor => `<li>${factor}</li>`).join('')}
                </ul>
            </div>
            <div class="risk-section">
                <h4>What You Can Do Next (Action Steps)</h4>
                <p class="section-desc">Simple, practical steps to strengthen your portfolio and feel more confident about your investments.</p>
                <ul>
                    ${analysis.suggestions.map(suggestion => `<li>${suggestion}</li>`).join('')}
                </ul>
            </div>
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

// News (stock-related from NewsAPI)
async function loadNews() {
    const grid = document.getElementById('newsGrid');
    const loadingEl = document.getElementById('newsLoading');
    if (!grid) return;
    if (loadingEl) loadingEl.textContent = 'Loading news...';
    grid.innerHTML = loadingEl ? loadingEl.outerHTML : '<p class="news-loading">Loading news...</p>';

    try {
        const response = await fetch(`${API_BASE_URL}/news/stocks`);
        const articles = await response.json();
        if (!Array.isArray(articles) || articles.length === 0) {
            grid.innerHTML = '<p class="news-error">No stock news available at the moment.</p>';
            return;
        }
        grid.innerHTML = articles.map(article => {
            const img = article.urlToImage
                ? `<img class="news-card-image" src="${(article.urlToImage || '').replace(/"/g, '&quot;')}" alt="">`
                : '<div class="news-card-image" style="display:flex;align-items:center;justify-content:center;color:#9ca3af;font-size:14px;">No image</div>';
            const title = escapeHtml(article.title || 'No title');
            const desc = escapeHtml(article.description || '');
            const url = (article.url || '#').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
            const meta = [article.sourceName, article.publishedAt].filter(Boolean).join(' · ');
            return `
                <article class="news-card">
                    <a href="${url}" target="_blank" rel="noopener noreferrer" style="text-decoration:none;color:inherit;">
                        ${img}
                        <div class="news-card-body">
                            <h3 class="news-card-title">${title}</h3>
                            ${meta ? `<p class="news-card-meta">${escapeHtml(meta)}</p>` : ''}
                            ${desc ? `<p class="news-card-description">${desc}</p>` : ''}
                            <span class="news-card-link">Read more →</span>
                        </div>
                    </a>
                </article>`;
        }).join('');
    } catch (error) {
        console.error('Error loading news:', error);
        grid.innerHTML = '<p class="news-error">Failed to load news. Please try again later.</p>';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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

// Floating chatbot icon handler
function openChatbotFromFab() {
    showPage('chatbot');
}

