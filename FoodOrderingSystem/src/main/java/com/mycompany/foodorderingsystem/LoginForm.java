package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {

    // ================== Components ==================
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, forgotPasswordButton;

    // ================== Constructor ==================
    public LoginForm() {
        initializeFrame();
        initializeComponents();
        registerEvents();

        setVisible(true);
    }

    // ================== UI Setup ==================
    private void initializeFrame() {
        setTitle("Login");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));
    }

    private void initializeComponents() {
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        forgotPasswordButton = new JButton("Forgot Password?");

        add(emailLabel); 
        add(emailField);
        add(passwordLabel); 
        add(passwordField);
        add(registerButton); 
        add(loginButton);
        add(forgotPasswordButton);
    }

    // ================== Events ==================
    private void registerEvents() {

        loginButton.addActionListener(e -> loginUser());

        registerButton.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });

        forgotPasswordButton.addActionListener(e -> {
            dispose();
            new ForgotPasswordForm().setVisible(true);
        });
    }

    // ================== Database ==================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/foodorderingsystem",
                "root",
                "Sa3597@35"
        );
    }

    // ================== Login Logic ==================
    private void loginUser() {
        String email = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword()).trim();

        // ===== Validation =====
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.");
            return;
        }

        String query = "SELECT * FROM Users WHERE email=? AND password=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                showMessage("Invalid email or password.");
                return;
            }

            // ===== User Data =====
            String name = rs.getString("name");
            String role = rs.getString("role");
            int userId = rs.getInt("user_id");

            showMessage("Welcome, " + name + " (" + role + ")");
            dispose();

            // ===== Role Handling =====
            handleUserRole(role, userId, con);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Database Error: " + ex.getMessage());
        }
    }

    // ================== Role Logic ==================
    private void handleUserRole(String role, int userId, Connection con) throws SQLException {

        switch (role.toLowerCase()) {

            case "admin":
                new AdminDashboard();
                break;

            case "customer":
                new CustomerDashboard(userId);
                break;

            case "restaurant":
                openRestaurantDashboard(userId, con);
                break;

            default:
                showMessage("Unknown role.");
                break;
        }
    }

    // ================== Restaurant Logic ==================
    private void openRestaurantDashboard(int userId, Connection con) throws SQLException {

        String query = "SELECT restaurant_id FROM Restaurants WHERE user_id = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int restaurantId = rs.getInt("restaurant_id");

                System.out.println("Opening dashboard for restaurant_id = " + restaurantId);

                new RestaurantDashboard(userId); // نفس كودك (ما غيرته)
            } else {
                showMessage("Restaurant ID not found.");
            }
        }
    }

    // ================== Helper ==================
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}