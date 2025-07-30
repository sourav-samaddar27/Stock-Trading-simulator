// DashboardPanel.java
package com.example.stocks; // Package declaration.

import javax.swing.*;          // Imports Swing GUI components.
import javax.swing.table.DefaultTableModel; // For managing table data.
import java.awt.*;             // Imports AWT classes for layout, events, and colors.
import java.awt.event.ActionEvent; // For handling button click events.
import java.awt.event.ActionListener; // Interface for event listeners.
import java.math.BigDecimal;   // For precise monetary calculations.
import java.sql.SQLException;  // For handling database exceptions from services.
import java.time.format.DateTimeFormatter; // For formatting timestamps.
import java.util.List;         // For handling lists of data.
import java.util.Optional;
import java.util.concurrent.Executors; // For scheduling UI updates.
import java.util.concurrent.ScheduledExecutorService; // For scheduling UI updates.
import java.util.concurrent.TimeUnit; // For specifying time units.

/**
 * Represents the main Dashboard Panel for a logged-in user in the Stock Trading Simulator GUI.
 * This panel displays real-time stock prices, user's portfolio, order placement forms,
 * and history of orders and trades.
 */
public class DashboardPanel extends JPanel {

    private final MainApplication parentFrame; // Reference to the main application window for navigation.
    private final UserService userService;     // Dependency: User service for balance, etc.
    private final StockService stockService;   // Dependency: Stock service for market data.
    private final OrderService orderService;   // Dependency: Order service for placing orders.
    private final TradeService tradeService;   // Dependency: Trade service (for context, not direct UI interaction here).
    private final PortfolioService portfolioService; // Dependency: Portfolio service for user holdings.

    // --- Current User Session ---
    private User currentUser; // The currently logged-in user.

    // --- UI Components ---
    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JButton logoutButton;

    // Stock Market Table
    private JTable stockMarketTable;
    private DefaultTableModel stockMarketTableModel;
    private ScheduledExecutorService stockUpdateScheduler; // For updating stock table periodically.

    // Order Placement Form
    private JTextField orderSymbolField;
    private JTextField orderQuantityField;
    private JTextField orderPriceField;
    private JButton buyButton;
    private JButton sellButton;
    private JLabel orderMessageLabel;

    // Portfolio Table
    private JTable portfolioTable;
    private DefaultTableModel portfolioTableModel;
    private ScheduledExecutorService portfolioUpdateScheduler; // For updating portfolio table periodically.

    // Order History Table
    private JTable orderHistoryTable;
    private DefaultTableModel orderHistoryTableModel;
    private ScheduledExecutorService orderHistoryUpdateScheduler; // For updating order history table periodically.


    // Trade History Table
    private JTable tradeHistoryTable;
    private DefaultTableModel tradeHistoryTableModel;
    private ScheduledExecutorService tradeHistoryUpdateScheduler; // For updating trade history table periodically.

    // DateTimeFormatter for displaying timestamps in tables
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor for DashboardPanel.
     *
     * @param parentFrame The MainApplication instance for navigation.
     * @param userService The UserService instance.
     * @param stockService The StockService instance.
     * @param orderService The OrderService instance.
     * @param tradeService The TradeService instance.
     * @param portfolioService The PortfolioService instance.
     */
    public DashboardPanel(MainApplication parentFrame, UserService userService, StockService stockService,
                          OrderService orderService, TradeService tradeService, PortfolioService portfolioService) {
        this.parentFrame = parentFrame;
        this.userService = userService;
        this.stockService = stockService;
        this.orderService = orderService;
        this.tradeService = tradeService;
        this.portfolioService = portfolioService;

        setupUI(); // Set up the panel's components.
    }

    /**
     * Sets up the layout and components of the Dashboard Panel.
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout for main layout with gaps.
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around the panel.

        // --- Top Panel: Welcome, Balance, Logout ---
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Welcome, Guest!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton = new JButton("Logout");

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(Box.createHorizontalStrut(20)); // Spacer
        userInfoPanel.add(balanceLabel);

        topPanel.add(userInfoPanel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        logoutButton.addActionListener(e -> performLogout());

        // --- Center Panel: Split into Left (Market, Order Form) and Right (Portfolio, History) ---
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.5); // Give equal weight to both sides.

        // Left Panel: Market Data & Order Placement
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Market Data & Order Placement"));

        // Market Data Table
        stockMarketTableModel = new DefaultTableModel(new Object[]{"Symbol", "Company", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable.
            }
        };
        stockMarketTable = new JTable(stockMarketTableModel);
        stockMarketTable.setFillsViewportHeight(true); // Table fills the height of its scroll pane.
        JScrollPane stockScrollPane = new JScrollPane(stockMarketTable);
        leftPanel.add(stockScrollPane, BorderLayout.CENTER);

        // Order Placement Form
        JPanel orderFormPanel = new JPanel(new GridBagLayout());
        orderFormPanel.setBorder(BorderFactory.createTitledBorder("Place Order"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; orderFormPanel.add(new JLabel("Symbol:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; orderSymbolField = new JTextField(8); orderFormPanel.add(orderSymbolField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; orderFormPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; orderQuantityField = new JTextField(5); orderFormPanel.add(orderQuantityField, gbc);
        gbc.gridx = 4; gbc.gridy = 0; orderFormPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; orderPriceField = new JTextField(8); orderFormPanel.add(orderPriceField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; buyButton = new JButton("Buy"); orderFormPanel.add(buyButton, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 3; sellButton = new JButton("Sell"); orderFormPanel.add(sellButton, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6; orderMessageLabel = new JLabel(" ");
        orderMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderFormPanel.add(orderMessageLabel, gbc);

        leftPanel.add(orderFormPanel, BorderLayout.SOUTH);
        centerSplitPane.setLeftComponent(leftPanel);

        // Right Panel: Portfolio & History
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Your Portfolio & History"));

        JTabbedPane historyTabbedPane = new JTabbedPane();

        // Portfolio Table
        portfolioTableModel = new DefaultTableModel(new Object[]{"Symbol", "Company", "Quantity", "Current Price", "Total Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        portfolioTable = new JTable(portfolioTableModel);
        portfolioTable.setFillsViewportHeight(true);
        JScrollPane portfolioScrollPane = new JScrollPane(portfolioTable);
        historyTabbedPane.addTab("Portfolio", portfolioScrollPane);

        // Order History Table
        orderHistoryTableModel = new DefaultTableModel(new Object[]{"ID", "Symbol", "Type", "Price", "Qty", "Status", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderHistoryTable = new JTable(orderHistoryTableModel);
        orderHistoryTable.setFillsViewportHeight(true);
        JScrollPane orderHistoryScrollPane = new JScrollPane(orderHistoryTable);
        historyTabbedPane.addTab("Order History", orderHistoryScrollPane);

        // Trade History Table
        tradeHistoryTableModel = new DefaultTableModel(new Object[]{"ID", "Stock", "Price", "Qty", "Buyer ID", "Seller ID", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tradeHistoryTable = new JTable(tradeHistoryTableModel);
        tradeHistoryTable.setFillsViewportHeight(true);
        JScrollPane tradeHistoryScrollPane = new JScrollPane(tradeHistoryTable);
        historyTabbedPane.addTab("Trade History", tradeHistoryScrollPane);


        rightPanel.add(historyTabbedPane, BorderLayout.CENTER);
        centerSplitPane.setRightComponent(rightPanel);

        add(centerSplitPane, BorderLayout.CENTER);

        // --- Add Action Listeners for Order Buttons ---
        buyButton.addActionListener(e -> placeOrder(Order.OrderType.BUY));
        sellButton.addActionListener(e -> placeOrder(Order.OrderType.SELL));
    }

    /**
     * Called by MainApplication when a user successfully logs in.
     * Sets the current user and starts data update schedulers.
     *
     * @param user The logged-in User object.
     */
    public void onUserLoggedIn(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        updateBalanceLabel(); // Update balance immediately.
        startDataUpdateSchedulers(); // Start all background UI updates.
        // Initial data load
        updateStockMarketTable();
        updatePortfolioTable();
        updateOrderHistoryTable();
        updateTradeHistoryTable();
    }

    /**
     * Called by MainApplication when a user logs out.
     * Clears user data and stops data update schedulers.
     */
    public void onUserLoggedOut() {
        this.currentUser = null;
        welcomeLabel.setText("Welcome, Guest!");
        balanceLabel.setText("Balance: $0.00");
        stopDataUpdateSchedulers(); // Stop all background UI updates.
        // Clear all table models
        stockMarketTableModel.setRowCount(0);
        portfolioTableModel.setRowCount(0);
        orderHistoryTableModel.setRowCount(0);
        tradeHistoryTableModel.setRowCount(0);
        clearOrderForm();
    }

    /**
     * Starts scheduled tasks to periodically update tables and labels.
     * These updates happen on the EDT via SwingUtilities.invokeLater.
     */
    private void startDataUpdateSchedulers() {
        // Stock Market Table updates
        stockUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        stockUpdateScheduler.scheduleAtFixedRate(this::updateStockMarketTable, 0, 3, TimeUnit.SECONDS);

        // Portfolio Table updates
        portfolioUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        portfolioUpdateScheduler.scheduleAtFixedRate(this::updatePortfolioTable, 0, 5, TimeUnit.SECONDS);

        // Order History Table updates
        orderHistoryUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        orderHistoryUpdateScheduler.scheduleAtFixedRate(this::updateOrderHistoryTable, 0, 5, TimeUnit.SECONDS);

        // Trade History Table updates
        tradeHistoryUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        tradeHistoryUpdateScheduler.scheduleAtFixedRate(this::updateTradeHistoryTable, 0, 5, TimeUnit.SECONDS);

        // Balance Label updates (can be less frequent or tied to other updates)
        ScheduledExecutorService balanceScheduler = Executors.newSingleThreadScheduledExecutor();
        balanceScheduler.scheduleAtFixedRate(this::updateBalanceLabel, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Stops all scheduled tasks for updating UI data.
     */
    private void stopDataUpdateSchedulers() {
        if (stockUpdateScheduler != null) stockUpdateScheduler.shutdownNow();
        if (portfolioUpdateScheduler != null) portfolioUpdateScheduler.shutdownNow();
        if (orderHistoryUpdateScheduler != null) orderHistoryUpdateScheduler.shutdownNow();
        if (tradeHistoryUpdateScheduler != null) tradeHistoryUpdateScheduler.shutdownNow();
        // Also stop the balance scheduler if it's a separate one.
        // For simplicity, we can shut down all schedulers here.
        // A more robust app might manage a single global scheduler for all UI updates.
    }

    /**
     * Updates the user's balance label.
     * Ensures UI update happens on EDT.
     */
    private void updateBalanceLabel() {
        if (currentUser == null) return;
        try {
            // Re-fetch user to get latest balance from DB
            Optional<User> updatedUserOptional = userService.getUserById(currentUser.getId());
            if (updatedUserOptional.isPresent()) {
                currentUser = updatedUserOptional.get(); // Update current user object
                BigDecimal balance = currentUser.getBalance();
                SwingUtilities.invokeLater(() -> balanceLabel.setText("Balance: $" + balance.setScale(2, BigDecimal.ROUND_HALF_UP)));
            }
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating balance: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating balance (user not found): " + e.getMessage()));
        }
    }

    /**
     * Updates the stock market table with current stock prices.
     * Ensures UI update happens on EDT.
     */
    private void updateStockMarketTable() {
        try {
            List<Stock> stocks = stockService.getAllStocks();
            SwingUtilities.invokeLater(() -> {
                stockMarketTableModel.setRowCount(0); // Clear existing rows.
                for (Stock stock : stocks) {
                    stockMarketTableModel.addRow(new Object[]{
                            stock.getSymbol(),
                            stock.getCompanyName(),
                            stock.getCurrentPrice().setScale(2, BigDecimal.ROUND_HALF_UP)
                    });
                }
            });
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating stock market table: " + e.getMessage()));
        }
    }

    /**
     * Updates the user's portfolio table.
     * Ensures UI update happens on EDT.
     */
    private void updatePortfolioTable() {
        if (currentUser == null) return;
        try {
            List<PortfolioService.PortfolioDetail> portfolio = portfolioService.getUserPortfolio(currentUser.getId());
            SwingUtilities.invokeLater(() -> {
                portfolioTableModel.setRowCount(0); // Clear existing rows.
                for (PortfolioService.PortfolioDetail detail : portfolio) {
                    portfolioTableModel.addRow(new Object[]{
                            detail.getStockSymbol(),
                            detail.getCompanyName(),
                            detail.getQuantity(),
                            detail.getCurrentPrice().setScale(2, BigDecimal.ROUND_HALF_UP),
                            detail.getTotalValue().setScale(2, BigDecimal.ROUND_HALF_UP)
                    });
                }
            });
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating portfolio table: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating portfolio (user not found): " + e.getMessage()));
        }
    }

    /**
     * Updates the user's order history table.
     * Ensures UI update happens on EDT.
     */
    private void updateOrderHistoryTable() {
        if (currentUser == null) return;
        try {
            // Fetch orders for the current user using the new OrderService method.
            List<Order> orders = orderService.getOrdersByUserId(currentUser.getId());
            SwingUtilities.invokeLater(() -> {
                orderHistoryTableModel.setRowCount(0); // Clear existing rows.
                for (Order order : orders) {
                    // To display stock symbol, we need to fetch the stock details.
                    Optional<Stock> orderedStock = null;
                    try {
                        orderedStock = stockService.getStockById(order.getStockId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    String stockSymbol = orderedStock.isPresent() ? orderedStock.get().getSymbol() : "UNKNOWN";

                    orderHistoryTableModel.addRow(new Object[]{
                            order.getId(),
                            stockSymbol, // Display symbol instead of ID
                            order.getOrderType().name(),
                            order.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP),
                            order.getQuantity(),
                            order.getStatus().name(),
                            order.getTimestamp() != null ? order.getTimestamp().format(DATE_TIME_FORMATTER) : ""
                    });
                }
            });
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating order history table: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating order history (user not found): " + e.getMessage()));
        }
    }

    /**
     * Updates the user's trade history table.
     * Ensures UI update happens on EDT.
     */
    private void updateTradeHistoryTable() {
        if (currentUser == null) return;
        try {
            List<Trade> trades = tradeService.findTradesByUserId(currentUser.getId()); // Fetch trades for current user.
            SwingUtilities.invokeLater(() -> {
                tradeHistoryTableModel.setRowCount(0); // Clear existing rows.
                for (Trade trade : trades) {
                    // Need to fetch stock symbol for display, as Trade only has stockId.
                    // This is a common pattern: fetch related data for display.
                    // For simplicity, we'll just show stock ID for now or fetch symbol.
                    Optional<Stock> tradedStock = null;
                    try {
                        tradedStock = stockService.getStockById(trade.getStockId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    String stockSymbol = tradedStock.isPresent() ? tradedStock.get().getSymbol() : "UNKNOWN";

                    tradeHistoryTableModel.addRow(new Object[]{
                            trade.getId(),
                            stockSymbol, // Display symbol instead of ID
                            trade.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP),
                            trade.getQuantity(),
                            trade.getBuyerUserId(),
                            trade.getSellerUserId(),
                            trade.getTimestamp() != null ? trade.getTimestamp().format(DATE_TIME_FORMATTER) : ""
                    });
                }
            });
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> System.err.println("Error updating trade history table: " + e.getMessage()));
        }
    }


    /**
     * Handles placing a buy or sell order.
     *
     * @param orderType The type of order (BUY or SELL).
     */
    private void placeOrder(Order.OrderType orderType) {
        if (currentUser == null) {
            showOrderMessage("Please log in to place an order.", Color.RED);
            return;
        }

        String symbol = orderSymbolField.getText().trim();
        String quantityStr = orderQuantityField.getText().trim();
        String priceStr = orderPriceField.getText().trim();

        // Clear previous message
        orderMessageLabel.setText(" ");
        orderMessageLabel.setForeground(Color.RED);

        try {
            int quantity = Integer.parseInt(quantityStr);
            BigDecimal price = new BigDecimal(priceStr);

            if (orderType == Order.OrderType.BUY) {
                orderService.placeBuyOrder(currentUser.getId(), symbol, quantity, price);
                showOrderMessage("Buy order placed successfully!", Color.BLUE);
            } else { // SELL order
                orderService.placeSellOrder(currentUser.getId(), symbol, quantity, price);
                showOrderMessage("Sell order placed successfully!", Color.BLUE);
            }
            clearOrderForm(); // Clear form on success.
            // Trigger immediate updates for portfolio and order history after placing an order
            updateBalanceLabel();
            updatePortfolioTable();
            updateOrderHistoryTable();

        } catch (NumberFormatException e) {
            showOrderMessage("Invalid quantity or price format.", Color.RED);
        } catch (IllegalArgumentException e) {
            showOrderMessage("Order error: " + e.getMessage(), Color.RED);
        } catch (SQLException e) {
            showOrderMessage("Database error placing order: " + e.getMessage(), Color.RED);
            System.err.println("Database error placing order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays a message in the order form's message label.
     * Ensures UI update happens on EDT.
     *
     * @param message The message to display.
     * @param color The color of the message.
     */
    private void showOrderMessage(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            orderMessageLabel.setForeground(color);
            orderMessageLabel.setText(message);
        });
    }

    /**
     * Clears the order placement form fields.
     */
    private void clearOrderForm() {
        orderSymbolField.setText("");
        orderQuantityField.setText("");
        orderPriceField.setText("");
        orderMessageLabel.setText(" ");
    }

    /**
     * Handles user logout.
     */
    private void performLogout() {
        parentFrame.clearCurrentUser(); // Clear user session in MainApplication.
        parentFrame.showPanel("Login"); // Navigate back to login screen.
    }
}
