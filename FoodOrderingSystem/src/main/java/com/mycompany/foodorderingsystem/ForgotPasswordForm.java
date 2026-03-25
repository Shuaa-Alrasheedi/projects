package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ForgotPasswordForm extends JFrame {

    // ================== Components ==================
    private JTextField emailField;
    private JButton resetPasswordButton;

    // ================== Constructor ==================
    public ForgotPasswordForm() {
        initializeFrame();
        initializeComponents();
        registerEvents();

        setVisible(true);
    }

    // ================== UI Setup ==================
    private void initializeFrame() {
        setTitle("Forgot Password");
        setSize(350, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 2, 10, 10));
    }

    private void initializeComponents() {
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        resetPasswordButton = new JButton("Reset Password");

        add(emailLabel); 
        add(emailField);
        add(new JLabel()); 
        add(resetPasswordButton);
    }

    private void registerEvents() {
        resetPasswordButton.addActionListener(e -> resetPassword());
    }

    // ================== Database ==================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/foodorderingsystem",
                "root",
                "Sa3597@35"
        );
    }

    // ================== Reset Logic ==================
    private void resetPassword() {

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showMessage("Please enter your email.");
            return;
        }

        String checkQuery = "SELECT * FROM Users WHERE email=?";
        String updateQuery = "UPDATE Users SET password=? WHERE email=?";

        try (Connection con = getConnection();
             PreparedStatement checkPs = con.prepareStatement(checkQuery)) {

            checkPs.setString(1, email);
            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                showMessage("Email not found.");
                return;
            }

            String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");

            if (newPassword == null || newPassword.trim().isEmpty()) return;

            if (!isStrongPassword(newPassword)) {
                showMessage("Password must contain uppercase, number, special character, and be at least 8 characters.");
                return;
            }

            try (PreparedStatement updatePs = con.prepareStatement(updateQuery)) {

                updatePs.setString(1, newPassword);
                updatePs.setString(2, email);
                updatePs.executeUpdate();

                showMessage("Password reset successfully!");

                dispose();
                new LoginForm().setVisible(true);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Database Error: " + ex.getMessage());
        }
    }

    // ================== Validation ==================
    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$");
    }

    // ================== Helper ==================
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}