package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class RestaurantDashboard extends JFrame {

    // ================== Fields ==================
    private JTextField dishNameField;
    private JTextField dishPriceField;
    private JTextArea dishDescriptionField;
    private JTable dishesTable;
    private DefaultTableModel tableModel;
    private Connection conn;
    private int restaurantId;

    // ================== Constructor ==================
    public RestaurantDashboard(int userId) {
        connectToDatabase();
        fetchRestaurantId(userId);

        initializeFrame();
        initializeUI();
        loadDishes();

        setVisible(true);
    }

    // ================== Frame ==================
    private void initializeFrame() {
        setTitle("Restaurant Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
    }

    // ================== UI ==================
    private void initializeUI() {

        // ===== Input Panel =====
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        dishNameField = new JTextField();
        dishPriceField = new JTextField();
        dishDescriptionField = new JTextArea();

        inputPanel.add(new JLabel("Dish Name:"));
        inputPanel.add(dishNameField);
        inputPanel.add(new JLabel("Dish Price:"));
        inputPanel.add(dishPriceField);
        inputPanel.add(new JLabel("Dish Description:"));
        inputPanel.add(new JScrollPane(dishDescriptionField));

        JButton addDishButton = new JButton("Add Dish");
        addDishButton.addActionListener(e -> addDish());
        inputPanel.add(addDishButton);

        add(inputPanel, BorderLayout.NORTH);

        // ===== Table =====
        tableModel = new DefaultTableModel(
                new String[]{"Dish Name", "Price", "Description"}, 0
        );
        dishesTable = new JTable(tableModel);

        add(new JScrollPane(dishesTable), BorderLayout.CENTER);

        // ===== Bottom Panel =====
        JPanel bottomPanel = new JPanel();

        JButton trackOrdersButton = new JButton("Track Orders");
        trackOrdersButton.addActionListener(e -> new OrderTrackingForm(restaurantId));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        bottomPanel.add(trackOrdersButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(exitButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ================== Database ==================
    private void connectToDatabase() {
        conn = DatabaseConnection.getConnection();

        if (conn == null) {
            showMessage("Failed to connect to database.");
            System.exit(1);
        }
    }

    private void fetchRestaurantId(int userId) {
        String query = "SELECT restaurant_id FROM Restaurants WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                restaurantId = rs.getInt("restaurant_id");
                System.out.println("Restaurant ID: " + restaurantId);
            } else {
                showMessage("Restaurant not found for this user.");
                dispose();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage());
        }
    }

    // ================== Add Dish ==================
    private void addDish() {

        String name = dishNameField.getText().trim();
        String priceText = dishPriceField.getText().trim();
        String description = dishDescriptionField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty() || description.isEmpty()) {
            showMessage("Please fill in all fields.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);

            if (price <= 0) {
                showMessage("Price must be greater than zero.");
                return;
            }

            String query = "INSERT INTO Dishes (name, description, price, restaurant_id) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, name);
                ps.setString(2, description);
                ps.setDouble(3, price);
                ps.setInt(4, restaurantId);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    showMessage("Dish added successfully.");
                    clearFields();
                    loadDishes();
                } else {
                    showMessage("Failed to add dish.");
                }
            }

        } catch (NumberFormatException e) {
            showMessage("Invalid price format.");
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage());
        }
    }

    // ================== Load Dishes ==================
    private void loadDishes() {

        String query = "SELECT name, price, description FROM Dishes WHERE restaurant_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, restaurantId);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================== Helpers ==================
    private void clearFields() {
        dishNameField.setText("");
        dishPriceField.setText("");
        dishDescriptionField.setText("");
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}