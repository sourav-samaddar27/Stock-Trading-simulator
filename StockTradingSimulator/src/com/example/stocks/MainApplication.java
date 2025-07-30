package com.example.stocks;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
public class MainApplication extends JFrame{
    private UserService userService;
    private StockService stockService;
    private OrderService orderService;
    private TradeService tradeService;
    private PortfolioService portfolioService;
    private MarketDataSimulator marketDataSimulator;
    private OrderMatchingEngine orderMatchingEngine;
    private CardLayout cardLayout; // Manages switching between different panels (e.g., login, dashboard).
    private JPanel mainPanel; // The panel that uses CardLayout to hold other panels.
    private User currentUser; // Stores the currently logged-in user.

    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    private DashboardPanel dashboardPanel;
    public MainApplication(){
        super("Real-time Stock Trading Simulator");
        initializeApplication();
        setupUI();
    }

    private void initializeApplication(){
        try{
            // 1. Initialize Database Schema
            DatabaseManager.initializeDatabase();
            System.out.println("Database initialized successfully.");

            // 2. Initialize DAOs
            UserDAO userDAO = new UserDAO();
            StockDAO stockDAO = new StockDAO();
            OrderDAO orderDAO = new OrderDAO();
            TradeDAO tradeDAO = new TradeDAO();
            PortfolioDAO portfolioDAO = new PortfolioDAO();
            System.out.println("DAOs initialized.");

            // 3. Initialize Services (injecting DAOs)
            userService = new UserService(userDAO);
            stockService = new StockService(stockDAO);
            // OrderService needs PortfolioDAO now for sell order validation
            orderService = new OrderService(orderDAO, userDAO, stockDAO);
            tradeService = new TradeService(userDAO, stockDAO, orderDAO, tradeDAO, portfolioDAO);
            portfolioService = new PortfolioService(portfolioDAO, userDAO, stockDAO);
            System.out.println("Services initialized.");

            // 4. Start Background Threads (injecting services)
            marketDataSimulator=new MarketDataSimulator(stockService);
            marketDataSimulator.startSimulation(); // Start price updates.

            orderMatchingEngine=new OrderMatchingEngine(orderService, tradeService, stockService);
            orderMatchingEngine.startEngine();
            System.out.println("Background engines started");

            // 5. Add some initial stocks if the database is empty (for testing)
            if(stockService.getAllStocks().isEmpty()){
                System.out.println("Adding initial stocks...");
                stockService.addNewStock("AAPL", "Apple Inc.", new BigDecimal("175.00"));
                stockService.addNewStock("GOOGL", "Alphabet Inc.", new BigDecimal("1500.00"));
                stockService.addNewStock("MSFT", "Microsoft Corp.", new BigDecimal("400.00"));
                stockService.addNewStock("AMZN", "Amazon.com Inc.", new BigDecimal("180.00"));
                System.out.println("Initial stocks added.");
            }
        } catch (SQLException e) {
            System.err.println("FATAL ERROR: Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage() + "\nApplication will exit.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit application on fatal database error.
        } catch (IllegalArgumentException e) {
            System.err.println("FATAL ERROR: Application initialization failed: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Application Error: " + e.getMessage() + "\nApplication will exit.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit application on fatal configuration error.
        }
    }
    public void setupUI(){
        setTitle("Stock Trading Simulation");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the screen.

        cardLayout=new CardLayout();
        mainPanel=new JPanel(cardLayout); // mainPanel uses CardLayout.
        add(mainPanel); // Add mainPanel to the JFrame.

        loginPanel = new LoginPanel(this, userService);
        registrationPanel = new RegistrationPanel(this, userService);

        mainPanel.add(loginPanel, "Login"); // Add LoginPanel to card layout with name "Login".
        mainPanel.add(registrationPanel, "Register");

        cardLayout.show(mainPanel,"Login");

        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent){
                    System.out.println("Application closing. Stopping background engines...");
                    if(marketDataSimulator!=null){
                        marketDataSimulator.stopSimulation();
                    }
                    if(orderMatchingEngine!=null){
                        orderMatchingEngine.stopEngine();
                    }
                System.out.println("Background engines stopped. Goodbye!");
            }

        });
        setVisible(true);

    }

    public void showPanel(String panelName){
        if("Dashboard".equals(panelName) && dashboardPanel==null){
            dashboardPanel=new DashboardPanel(this, userService,stockService,orderService,tradeService,portfolioService);
            mainPanel.add(dashboardPanel,"Dashboard");
            System.out.println("DashboardPanel initialized and added.");
        }
        cardLayout.show(mainPanel,panelName);
    }


    public void setCurrentUser(User user){
        this.currentUser=user;
        if(dashboardPanel!=null){
            dashboardPanel.onUserLoggedIn(user);

        }
        System.out.println("Current user set to: "+user.getUsername());
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public void clearCurrentUser(){
        this.currentUser=null;
        if(dashboardPanel!=null){
            dashboardPanel.onUserLoggedOut();

        }
        System.out.println("Current user Logged out.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApplication::new);
    }



}
