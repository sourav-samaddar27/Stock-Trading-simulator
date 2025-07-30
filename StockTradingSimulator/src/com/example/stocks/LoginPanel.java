// LoginPanel.java
package com.example.stocks; // Package declaration.

import javax.swing.*;      // Imports Swing GUI components.
import java.awt.*;         // Imports AWT classes for layout and events.
import java.awt.event.ActionEvent; // For handling button click events.
import java.awt.event.ActionListener; // Interface for event listeners.
import java.sql.SQLException; // For handling database exceptions from UserService.
import java.util.Optional;   // For handling Optional return from UserService.

/**
 * Represents the Login Panel of the Stock Trading Simulator GUI.
 * This panel allows users to enter their username and password to log in,
 * or navigate to the registration screen.
 */
public class LoginPanel extends JPanel { // Extends JPanel to create a reusable UI component.

    private final MainApplication parentFrame; // Reference to the main application window for navigation.
    private final UserService userService;     // Dependency: Needs UserService for login/registration logic.

    // --- UI Components ---
    private JTextField usernameField;
    private JPasswordField passwordField; // JPasswordField is recommended for passwords (hides input).
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel; // To display success or error messages to the user.

    /**
     * Constructor for LoginPanel.
     *
     * @param parentFrame The MainApplication instance to interact with (for navigation).
     * @param userService The UserService instance for handling login/registration.
     */
    public LoginPanel(MainApplication parentFrame, UserService userService) {
        this.parentFrame = parentFrame;
        this.userService = userService;
        setupUI(); // Call helper method to set up the panel's components.
    }

    /**
     * Sets up the layout and components of the Login Panel.
     */
    private void setupUI() {
        setLayout(new GridBagLayout()); // Use GridBagLayout for flexible and precise component placement.
        GridBagConstraints gbc = new GridBagConstraints(); // Constraints for GridBagLayout.
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components.
        gbc.fill = GridBagConstraints.HORIZONTAL; // Components will fill their display area horizontally.

        // --- Username Label and Field ---
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 0; // Row 0
        usernameField = new JTextField(20); // 20 columns wide.
        add(usernameField, gbc);

        // --- Password Label and Field ---
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        passwordField = new JPasswordField(20); // 20 columns wide.
        add(passwordField, gbc);

        // --- Message Label ---
        gbc.gridx = 0; // Spans across columns.
        gbc.gridy = 2; // Row 2
        gbc.gridwidth = 2; // Spans 2 columns.
        messageLabel = new JLabel(" "); // Initialize with a space to reserve height.
        messageLabel.setForeground(Color.RED); // Default to red for error messages.
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text.
        add(messageLabel, gbc);

        // --- Login Button ---
        gbc.gridx = 0; // Column 0
        gbc.gridy = 3; // Row 3
        gbc.gridwidth = 1; // Reset to 1 column.
        gbc.anchor = GridBagConstraints.CENTER; // Center the button.
        loginButton = new JButton("Login");
        add(loginButton, gbc);

        // --- Register Button ---
        gbc.gridx = 1; // Column 1
        gbc.gridy = 3; // Row 3
        registerButton = new JButton("Register");
        add(registerButton, gbc);

        // --- Add Action Listeners to Buttons ---
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin(); // Call method to handle login logic.
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // CORRECTED: Remove JOptionPane and directly show the RegistrationPanel
                parentFrame.showPanel("Register"); // Switch to the RegistrationPanel.
            }
        });
    }

    /**
     * Handles the login attempt when the login button is clicked.
     * Interacts with the UserService to authenticate the user.
     */
    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword()); // Get password as String from JPasswordField.

        // Clear previous messages.
        messageLabel.setText(" ");
        messageLabel.setForeground(Color.RED); // Reset to red for potential errors.

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        try {
            // Call the UserService to attempt login.
            Optional<User> userOptional = userService.loginUser(username, password);

            if (userOptional.isPresent()) {
                User loggedInUser = userOptional.get();
                parentFrame.setCurrentUser(loggedInUser); // Set the current logged-in user in MainApplication.
                messageLabel.setForeground(Color.BLUE); // Change color for success messages.
                messageLabel.setText("Login successful! Welcome, " + loggedInUser.getUsername() + "!");

                // --- IMPORTANT: Switch to Dashboard Panel ---
                // After successful login, switch to the main dashboard.
                parentFrame.showPanel("Dashboard"); // Assuming "Dashboard" panel is added to CardLayout.

            } else {
                // Login failed (user not found or invalid credentials).
                messageLabel.setText("Invalid username or password.");
            }
        } catch (SQLException e) {
            // Handle database errors during login.
            messageLabel.setText("Database error during login: " + e.getMessage());
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // Handle validation errors from UserService (e.g., password too short, though loginUser doesn't throw this).
            messageLabel.setText("Login error: " + e.getMessage());
            System.err.println("Login validation error: " + e.getMessage());
        }
    }
}
