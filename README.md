# Stock-Trading-simulator
Real-time Stock Trading Simulator
A sophisticated desktop application developed in Java, simulating a basic stock exchange. This project integrates a graphical user interface (GUI) with a robust backend, featuring real-time market data updates, concurrent order matching, and persistent data storage using a relational database. It serves as a comprehensive demonstration of industry-level Java application development.

Key Features:
User Management: Register new users, secure login/logout functionality.

Real-time Market Data: Stock prices dynamically update every few seconds, simulating live market fluctuations.

Order Placement: Users can place BUY and SELL orders for various stocks at desired prices and quantities.

Automated Order Matching Engine: A dedicated background thread continuously scans for matching buy and sell orders, executing trades automatically when conditions are met.

Atomic Trade Execution: All trade-related operations (updating user balances, modifying portfolios, changing order statuses, recording transactions) are wrapped in a single database transaction (ACID compliant), ensuring data integrity.

User Portfolio Management: View current stock holdings, quantities, and their real-time total value.

Order & Trade History: Track all placed orders and executed trades for a logged-in user.

Persistent Data Storage: All user, stock, order, trade, and portfolio data is stored in an H2 embedded relational database.

Layered Architecture: Clear separation of Presentation (GUI), Service (Business Logic), and Data Access (DAO) layers.

Multithreading & Concurrency: Extensive use of ScheduledExecutorService for background tasks (market data updates, order matching) and SwingUtilities.invokeLater for safe UI updates from background threads.

Robust Error Handling: Comprehensive exception handling for database errors, invalid user input, and business rule violations.

Financial Precision: Uses java.math.BigDecimal for all monetary calculations to prevent floating-point inaccuracies.

Graphical User Interface (GUI): Built with Java Swing, providing an intuitive visual experience for users.

Technologies Used:
Language: Java (Core Java, Java 8+)

GUI Framework: Java Swing

Database: H2 Database (Embedded Mode)

Database Connectivity: JDBC (Java Database Connectivity)

Concurrency: java.util.concurrent.ScheduledExecutorService, java.util.concurrent.Executors, SwingUtilities.invokeLater

Date & Time: java.time.LocalDateTime, java.time.format.DateTimeFormatter

IDE: IntelliJ IDEA

Project Structure
StockTradingSimulator/
├── .idea/                                 # IntelliJ IDEA's internal project files
├── src/                                   # Your Java source code root
│   └── com/
│       └── example/
│           └── stocks/                    # Your main Java package
│               ├── DatabaseManager.java   # Manages DB connection & schema initialization
│               ├── User.java              # User data model
│               ├── Stock.java             # Stock data model
│               ├── Order.java             # Order data model (BUY/SELL, PENDING/EXECUTED)
│               ├── Trade.java             # Executed trade data model
│               ├── PortfolioItem.java     # User's stock holding data model
│               ├── UserDAO.java           # Data Access Object for User entity
│               ├── StockDAO.java          # Data Access Object for Stock entity
│               ├── OrderDAO.java          # Data Access Object for Order entity
│               ├── TradeDAO.java          # Data Access Object for Trade entity
│               ├── PortfolioDAO.java      # Data Access Object for PortfolioItem entity
│               ├── UserService.java       # Business logic for User operations
│               ├── StockService.java      # Business logic for Stock operations
│               ├── OrderService.java      # Business logic for Order placement
│               ├── TradeService.java      # Business logic for Trade execution (transactional)
│               ├── PortfolioService.java  # Business logic for User portfolio views
│               ├── MarketDataSimulator.java # Background thread for real-time price updates
│               ├── OrderMatchingEngine.java # Background thread for real-time order matching
│               ├── LoginPanel.java        # GUI panel for user login
│               ├── RegistrationPanel.java # GUI panel for new user registration
│               ├── DashboardPanel.java    # Main GUI panel for logged-in users (market, portfolio, orders, trades)
│               └── MainApplication.java   # Main GUI application entry point (JFrame, manages panel switching)
├── lib/                                   # External libraries (JARs)
│   └── h2-x.x.x.jar                       # H2 Database JDBC driver
└── data/                                  # (Automatically created) Stores your H2 database files
    └── stock_market.mv.db                 # Your actual H2 database file
    └── stock_market.trace.db              # (Optional) H2 trace file

How to Run Locally
Clone the Repository:

git clone <your-repo-url>/StockTradingSimulator.git
cd StockTradingSimulator

Download H2 Database JAR:

Go to h2database.com and download the latest stable H2 release (e.g., h2-2.2.224.zip).

Unzip the downloaded file and locate the h2-x.x.x.jar file inside its bin folder.

Add H2 JAR to Project:

In your StockTradingSimulator project directory, create a new folder named lib.

Copy the h2-x.x.x.jar file into this lib folder.

In IntelliJ IDEA:

Open your StockTradingSimulator project.

Go to File > Project Structure... (Ctrl+Alt+Shift+S).

Select Modules on the left, then the Dependencies tab.

Click the + button on the right, select JARs or Directories....

Navigate to your lib folder, select h2-x.x.x.jar, and click OK.

Ensure "Export" is checked for the H2 JAR. Click Apply and OK.

Clean and Rebuild Project:

In IntelliJ IDEA, go to Build > Rebuild Project. This ensures all changes are compiled correctly.

Run the Application:

Open src/com/example/stocks/MainApplication.java.

Right-click anywhere in the main method and select Run 'MainApplication.main()'.

Interact with the GUI:

The application window will appear, starting with the Login Panel.

Register a new user first (e.g., username: testuser, password: password123).

Then, log in with your new credentials.

Explore the Dashboard Panel:

Observe real-time stock price changes in the "Market Data" table.

Place Buy and Sell orders.

Check your "Portfolio", "Order History", and "Trade History" tabs for updates.

Observe your balance change after trades.

Click "Logout" to return to the login screen.
