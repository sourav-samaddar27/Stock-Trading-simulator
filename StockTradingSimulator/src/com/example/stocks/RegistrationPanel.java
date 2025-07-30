package com.example.stocks; // Package declaration.

import javax.swing.*;      // Imports Swing GUI components.
import java.awt.*;         // Imports AWT classes for layout and events.
import java.awt.event.ActionEvent; // For handling button click events.
import java.awt.event.ActionListener; // Interface for event listeners.
import java.sql.SQLException; // For handling database exceptions from UserService.

/**
 * Represents the Registration Panel of the Stock Trading Simulator GUI.
 * This panel allows new users to create an account by providing a username and password.
 */
public class RegistrationPanel extends JPanel { // Extends JPanel to create a reusable UI component.

    private final MainApplication parentFrame; // Reference to the main application window for navigation.
    private final UserService userService;     // Dependency: Needs UserService for registration logic.

    // --- UI Components ---
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField; // For confirming password.
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel messageLabel; // To display success or error messages to the user.

    /**
     * Constructor for RegistrationPanel.
     *
     * @param parentFrame The MainApplication instance to interact with (for navigation).
     * @param userService The UserService instance for handling user registration.
     */
    public RegistrationPanel(MainApplication parentFrame, UserService userService) {
        this.parentFrame = parentFrame;
        this.userService = userService;
        setupUI(); // Call helper method to set up the panel's components.
    }

    /**
     * Sets up the layout and components of the Registration Panel.
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
        usernameField = new JTextField(20);
        add(usernameField, gbc);

        // --- Password Label and Field ---
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // --- Confirm Password Label and Field ---
        gbc.gridx = 0; // Column 0
        gbc.gridy = 2; // Row 2
        add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 2; // Row 2
        confirmPasswordField = new JPasswordField(20);
        add(confirmPasswordField, gbc);

        // --- Message Label ---
        // Ensure messageLabel is initialized before adding.
        messageLabel = new JLabel(" "); // Initialize with a space to reserve height.
        messageLabel.setForeground(Color.RED); // Default to red for error messages.
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text.
        gbc.gridx = 0; // Spans across columns.
        gbc.gridy = 3; // Row 3
        gbc.gridwidth = 2; // Spans 2 columns.
        add(messageLabel, gbc); // Add the message label.


        // --- Register Button ---
        gbc.gridx = 0; // Column 0
        gbc.gridy = 4; // Row 4
        gbc.gridwidth = 1; // Reset to 1 column for buttons.
        gbc.anchor = GridBagConstraints.CENTER; // Center the button.
        registerButton = new JButton("Register");
        add(registerButton, gbc);

        // --- Back to Login Button ---
        gbc.gridx = 1; // Column 1
        gbc.gridy = 4; // Row 4
        backToLoginButton = new JButton("Back to Login");
        add(backToLoginButton, gbc);

        // --- Add Action Listeners to Buttons ---
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration(); // Call method to handle registration logic.
            }
        });

        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Switch back to the LoginPanel.
                parentFrame.showPanel("Login"); // Assuming "Login" panel is added to CardLayout.
                clearFields(); // Clear fields when navigating away.
            }
        });
    }

    /**
     * Handles the registration attempt when the register button is clicked.
     * Interacts with the UserService to register the new user.
     */
    private void performRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Clear previous messages.
        messageLabel.setText(" ");
        messageLabel.setForeground(Color.RED); // Reset to red for potential errors.

        // --- Input Validation (Client-side) ---
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }
        // Additional client-side validation (e.g., password strength regex) could go here.

        try {
            // Call the UserService to attempt registration.
            User registeredUser = userService.registerUser(username, password);

            messageLabel.setForeground(Color.BLUE); // Change color for success messages.
            messageLabel.setText("Registration successful! Welcome, " + registeredUser.getUsername() + "!");
            clearFields(); // Clear fields on successful registration.

            // --- IMPORTANT: Switch back to Login Panel (or directly to Dashboard) ---
            JOptionPane.showMessageDialog(parentFrame, "Registration successful! You can now log in.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
            parentFrame.showPanel("Login"); // After registration, go back to login screen.

        } catch (SQLException e) {
            // Handle database errors during registration.
            messageLabel.setText("Database error during registration: " + e.getMessage());
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // Handle validation errors from UserService (e.g., username already exists, password too short).
            messageLabel.setText("Registration error: " + e.getMessage());
            System.err.println("Registration validation error: " + e.getMessage());
        }
    }

    /**
     * Clears all input fields on the panel.
     */
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        messageLabel.setText(" ");
        messageLabel.setForeground(Color.RED); // Reset color.
    }
}
