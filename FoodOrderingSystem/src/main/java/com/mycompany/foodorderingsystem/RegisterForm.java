package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.regex.*;

public class RegisterForm extends JFrame {

    // ================== Components ==================
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeBox;
    private JButton registerButton;

    // ================== Constructor ==================
    public RegisterForm() {
        initializeFrame();
        initializeComponents();
        registerEvents();

        setVisible(true);
    }

    // ================== UI Setup ==================
    private void initializeFrame() {
        setTitle("User Registration");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));
    }

    private void initializeComponents() {
        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel typeLabel = new JLabel("User Role:");
        String[] userRoles = {"customer", "restaurant", "admin"};
        userTypeBox = new JComboBox<>(userRoles);

        registerButton = new JButton("Register");

        add(nameLabel); add(nameField);
        add(emailLabel); add(emailField);
        add(passwordLabel); add(passwordField);
        add(typeLabel); add(userTypeBox);
        add(new JLabel()); add(registerButton);
    }

    private void registerEvents() {
        registerButton.addActionListener(e -> registerUser());
    }

    // ================== Database ==================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/foodorderingsystem",
                "root",
                "Sa3597@35"
        );
    }

    // ================== Register Logic ==================
    private void registerUser() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword()).trim();
        String role = userTypeBox.getSelectedItem().toString();

        // ===== Validation =====
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showMessage("All fields are required.");
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Invalid email format.");
            return;
        }

        if (!isStrongPassword(password)) {
            showMessage("Password must be at least 8 characters, include uppercase, number, and special character.");
            return;
        }

        String insertUserQuery = "INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();

            if (!generatedKeys.next()) {
                showMessage("Registration failed.");
                return;
            }

            int userId = generatedKeys.getInt(1);

            showMessage("User registered successfully!");

            // ===== Restaurant إضافي =====
            if (role.equalsIgnoreCase("restaurant")) {
                addRestaurantDetails(con, userId, name);
            }

            dispose();
            new LoginForm().setVisible(true);

        } catch (SQLIntegrityConstraintViolationException ex) {
            showMessage("Email already registered.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Database Error: " + ex.getMessage());
        }
    }

    // ================== Restaurant Logic ==================
    private void addRestaurantDetails(Connection con, int userId, String name) throws SQLException {

        String[] types = {"Italian", "Indian", "American", "Saudi", "International", "Asian", "Marine", "Arab", "French"};
        String[] locations = {"Riyadh", "Qassim", "Jeddah", "Mecca", "Khobar", "Dammam"};

        JComboBox<String> typeBox = new JComboBox<>(types);
        JComboBox<String> locationBox = new JComboBox<>(locations);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Restaurant Category:"));
        panel.add(typeBox);
        panel.add(new JLabel("Location:"));
        panel.add(locationBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Restaurant Details", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String category = (String) typeBox.getSelectedItem();
        String location = (String) locationBox.getSelectedItem();

        String insertRestaurantQuery = "INSERT INTO Restaurants (name, location, category, user_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(insertRestaurantQuery)) {

            ps.setString(1, name);
            ps.setString(2, location);
            ps.setString(3, category);
            ps.setInt(4, userId);

            ps.executeUpdate();
        }
    }

    // ================== Validation ==================
    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$");
    }

    // ================== Helper ==================
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}