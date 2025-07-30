package com.example.stocks;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {
    private static final String JDBC_URL="jdbc:h2:./data/stock_market"; //   - "jdbc:h2:" is the standard prefix for connecting to an H2 database.
    private static final String USER="sa"; // Default H2 user (System Administrator).
                                            // For learning, this is fine. In production, you'd use specific users.
    private static final String PASSWORD=""; // Default H2 password (empty).
                                             // Again, for learning, this is fine. In production, never empty.

    private DatabaseManager(){
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException{
        // The DriverManager will automatically find the correct H2 JDBC driver (because you added the JAR).
        return DriverManager.getConnection(JDBC_URL,USER,PASSWORD);

    }
    public static void initializeDatabase() throws SQLException{
        try(Connection connection=getConnection();// Get a new database connection.
        Statement statement=connection.createStatement()){ // Create a Statement object to execute SQL commands.

            statement.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL UNIQUE," + // CORRECTED: Added comma here
                    "password_hash VARCHAR(255) NOT NULL," +
                    "balance DECIMAL(19, 4) NOT NULL" +
                    ")");
            System.out.println("Table 'Users' checked/created.");


            statement.execute("CREATE TABLE IF NOT EXISTS Stocks (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "symbol VARCHAR(10) NOT NULL UNIQUE," +
                    "company_name VARCHAR(255) NOT NULL," +
                    "current_price DECIMAL(19, 4) NOT NULL" +
                    ")");
            System.out.println("Table 'Stocks' checked/created.");

            statement.execute("CREATE TABLE IF NOT EXISTS Orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "stock_id INT NOT NULL," +
                    "order_type VARCHAR(4) NOT NULL," +
                    "price DECIMAL(19, 4) NOT NULL," +
                    "quantity INT NOT NULL," +
                    "status VARCHAR(20) NOT NULL," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES Users(id)," +
                    "FOREIGN KEY (stock_id) REFERENCES Stocks(id)" +
                    ")");
            System.out.println("Table 'Orders' checked/created.");

            statement.execute("CREATE TABLE IF NOT EXISTS Transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "buyer_user_id INT NOT NULL," +
                    "seller_user_id INT NOT NULL," +
                    "stock_id INT NOT NULL," +
                    "price DECIMAL(19, 4) NOT NULL," +
                    "quantity INT NOT NULL," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (buyer_user_id) REFERENCES Users(id)," +
                    "FOREIGN KEY (seller_user_id) REFERENCES Users(id)," +
                    "FOREIGN KEY (stock_id) REFERENCES Stocks(id)" +
                    ")");
            System.out.println("Table 'Transactions' checked/created.");


            statement.execute("CREATE TABLE IF NOT EXISTS Portfolios (" +
                    "user_id INT NOT NULL," +
                    "stock_id INT NOT NULL," +
                    "quantity INT NOT NULL," +
                    "PRIMARY KEY (user_id, stock_id)," +
                    "FOREIGN KEY (user_id) REFERENCES Users(id)," +
                    "FOREIGN KEY (stock_id) REFERENCES Stocks(id)" +
                    ")");
            System.out.println("Table 'Portfolios' checked/created.");

            System.out.println("Database schema initialized successfully.");




        }catch (SQLException e){
            System.err.println("Error initializing database schema: "+e.getMessage());
            throw e;
        }
    }
}
