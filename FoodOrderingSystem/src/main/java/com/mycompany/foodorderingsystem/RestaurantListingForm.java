package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RestaurantListingForm extends JFrame {

    // ================== Fields ==================
    private JTable restaurantTable;
    private JButton backButton;
    private int userId;

    // ================== Constructor ==================
    public RestaurantListingForm(int userId) {
        this.userId = userId;

        initializeFrame();
        initializeTable();
        initializeBottomPanel();
        loadRestaurants();
        registerEvents();

        setVisible(true);
    }

    // ================== UI Setup ==================
    private void initializeFrame() {
        setTitle("Restaurant Listing");
        setSize(800, 500);

        // 🔥 مهم: لا تقفل البرنامج كامل
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initializeTable() {
        restaurantTable = new JTable();

        // 🔥 تحسين UX
        restaurantTable.setRowHeight(30);
        restaurantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        restaurantTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader header = restaurantTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(restaurantTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initializeBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());

        backButton = new JButton("Back");
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ================== Events ==================
    private void registerEvents() {

        // زر الرجوع
        backButton.addActionListener(e -> {
            dispose();
            new CustomerDashboard(userId).setVisible(true);
        });

        // 🔥 Double Click بدل click عادي
        restaurantTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {

                    int row = restaurantTable.getSelectedRow();

                    if (row != -1) {
                        int restaurantId = (int) restaurantTable.getValueAt(row, 0);
                        openDishListForm(restaurantId);
                    }
                }
            }
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

    // ================== Load Data ==================
    private void loadRestaurants() {

        String[] columns = {"ID", "Name", "Location", "Cuisine", "Rating"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        String query = """
                SELECT r.restaurant_id, u.name, r.location, r.category, r.rating
                FROM Restaurants r
                JOIN Users u ON r.user_id = u.user_id
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("category"),
                        rs.getDouble("rating")
                });
            }

            restaurantTable.setModel(model);
            restaurantTable.setDefaultEditor(Object.class, null);

            // 🔥 تحسين عرض الأعمدة
            restaurantTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            restaurantTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Error loading restaurants: " + ex.getMessage());
        }
    }

    // ================== Navigation ==================
    private void openDishListForm(int restaurantId) {
        dispose();
        new DishListForm(restaurantId, userId);
    }

    // ================== Helper ==================
    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}