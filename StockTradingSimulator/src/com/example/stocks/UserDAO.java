package com.example.stocks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.Optional;

public class UserDAO {

    public User save(User user) throws SQLException{
        String sql="INSERT INTO Users (username, password_hash,balance) VALUES (?,?,?)";
        // Use try-with-resources for Connection and PreparedStatement to ensure they are closed.
        try(Connection connection=DatabaseManager.getConnection();
            // Prepare the statement. Statement.RETURN_GENERATED_KEYS is important to get the auto-generated ID.
            PreparedStatement stmt=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            // Set the values for the placeholders in the SQL statement.
            // Parameters are 1-indexed in JDBC.
            stmt.setString(1,user.getUsername());
            stmt.setString(2,user.getPasswordHash());
            stmt.setBigDecimal(3,user.getBalance());

            int affectedRows=stmt.executeUpdate(); // Execute the INSERT statement.
                                                    // executeUpdate() returns the number of rows affected.
            if(affectedRows==0){
                throw new SQLException("Creating user failed, no rows affected");
            }

            try(ResultSet generatedKeys=stmt.getGeneratedKeys()){
                if(generatedKeys.next()){
                    int id=generatedKeys.getInt(1);
                    return new User(id, user.getUsername(),user.getPasswordHash(),user.getBalance());
                }else{
                    throw new SQLException("Creating user Failed, no ID obtained");
                }
            }
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException{
        String sql="SELECT id, username,password_hash,balance FROM Users WHERE username=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setString(1,username);
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){
                    int id=rs.getInt("id");
                    String foundUsername=rs.getString("username");
                    String passwordHash=rs.getString("password_hash");
                    BigDecimal balance=rs.getBigDecimal("balance");
                    return Optional.of(new User(id,foundUsername,passwordHash,balance));
                }
            }

        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException{
        String sql="SELECT id, username,password_hash, balance FROM Users WHERE id=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,id); // Set the ID parameter.
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){ // If a row is returned (user found).
                    int foundId=rs.getInt("id");
                    String username=rs.getString("username");
                    String passwordHash=rs.getString("password_hash");
                    BigDecimal balance=rs.getBigDecimal("balance");
                    return Optional.of(new User(foundId,username,passwordHash,balance));
                }
            }
        }// Connection, PreparedStatement, and ResultSet are automatically closed here.
        return Optional.empty();
    }


    public boolean updateBalance(User user) throws SQLException{
        String sql="UPDATE Users SET balance= ? WHERE id=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setBigDecimal(1,user.getBalance());
            stmt.setInt(2,user.getId());

            int affectedRows=stmt.executeUpdate();
            return affectedRows>0;
        }
    }

    public boolean delete(int id) throws SQLException{
        String sql="DELETE FROM Users Where id=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,id);

            int affectedRows=stmt.executeUpdate();
            return affectedRows>0;
        }
    }
}
