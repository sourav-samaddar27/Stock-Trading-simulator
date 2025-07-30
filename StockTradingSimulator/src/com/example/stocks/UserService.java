package com.example.stocks;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO=userDAO;
    }

    public User registerUser(String username,String password) throws SQLException{
        if(username==null || username.trim().isEmpty()){
            throw new IllegalArgumentException("User cannot be Empty");
        }
        if(password==null || password.length()<=6){
            throw new IllegalArgumentException("Password must be at least 6 characters long.");

        }

        Optional<User> existingUser=userDAO.findByUsername(username);
        if(existingUser.isPresent()){
            throw new IllegalArgumentException("Username "+username+" already exists. Please choose another.");
        }

        String passwordHash=password;
        User newUser=new User(username,passwordHash);
        User savedUser=userDAO.save(newUser);
        System.out.println("User "+savedUser.getUsername()+" registered successfully with ID: "+savedUser.getId());
        return savedUser;

    }

    public Optional<User> loginUser(String username, String password) throws SQLException{
        Optional<User> userOptional=userDAO.findByUsername(username);
        if(userOptional.isPresent()){
            User user=userOptional.get();
            if(user.getPasswordHash().equals(password)){
                System.out.println("User "+username+" logged in successfully");
                return Optional.of(user);
            }


        }
        System.out.println("Login failed for username "+username+" Invalid credentials.");
        return Optional.empty();
    }

    public Optional<User> getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            return Optional.empty(); // Invalid ID, cannot be found.
        }
        return userDAO.findById(userId);
    }

    public BigDecimal getUserBalance(int userId) throws SQLException{
        Optional<User> userOptional=userDAO.findById(userId);
        if(userOptional.isPresent()){
            return userOptional.get().getBalance();
        }else{
            throw new IllegalArgumentException("User with ID "+ userId+" not found.");
        }
    }
    public void updateBalance(User user) throws SQLException{
         if(user.getId()<=0){
             throw new IllegalArgumentException("Cannot update balance for user without a valid Id");
         }
         if(user.getBalance().compareTo(BigDecimal.ZERO)<0){
             throw new IllegalArgumentException("Balance cannot be negative");
         }
         boolean updated=userDAO.updateBalance(user);
         if(!updated){
             throw new SQLException("Failed to update balance for user ID: "+user.getId());
         }
    }
    // --- Custom Exception Classes (for better error handling) ---
    // These could be in separate files or nested here for simplicity.
    // For a real project, put them in a 'exceptions' package.

    /**
     * Custom exception for when a user tries to register with an already existing username.
     * (Not strictly used above as IllegalArgumentException is thrown, but good for future refinement)
     */

    public static class UsernameAlreadyExistsException extends IllegalArgumentException{
        public UsernameAlreadyExistsException(String message){
            super(message);
        }
    }
    /**
     * Custom exception for authentication failures.
     * (Not strictly used above as Optional.empty() is returned, but good for future refinement)
     */
    public static class InvalidCredentialsException extends IllegalArgumentException{
        public InvalidCredentialsException(String message){
            super(message);
        }
    }


}
