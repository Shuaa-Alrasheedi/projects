package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DishListForm extends JFrame implements ActionListener {

    // ================== Fields ==================
    private DefaultTableModel tableModel;
    private JTable dishTable;

    private JButton addToCartButton;
    private JButton goToCartButton;
    private JButton backButton;

    private int restaurantId;
    private int userId;

    // ================== Constructor ==================
    public DishListForm(int restaurantId, int userId) {
        this.restaurantId = restaurantId;
        this.userId = userId;

        initializeUI();
        loadDishes();

        setVisible(true);
    }

    // ================== UI ==================
    private void initializeUI() {

        setTitle("Dish List");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
                new String[]{"Dish ID", "Dish Name", "Description", "Price"}, 0
        );

        dishTable = new JTable(tableModel);
        dishTable.setRowHeight(30);
        dishTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(dishTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();

        addToCartButton = new JButton("Add to Cart");
        goToCartButton = new JButton("Go to Cart");
        backButton = new JButton("Back");

        bottomPanel.add(addToCartButton);
        bottomPanel.add(goToCartButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        addToCartButton.addActionListener(this);
        goToCartButton.addActionListener(this);

        backButton.addActionListener(e -> {
            dispose();
            new RestaurantListingForm(userId);
        });
    }

    // ================== Actions ==================
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == addToCartButton) {
            addSelectedDishToCart();
        }

        else if (e.getSource() == goToCartButton) {
            goToCart();
        }
    }

    // ================== Load Dishes ==================
    private void loadDishes() {

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM Dishes WHERE restaurant_id = ?"
             )) {

            ps.setInt(1, restaurantId);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("dish_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price")
                });
            }

            dishTable.setDefaultEditor(Object.class, null);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load dishes");
        }
    }

    // ================== Add to Cart ==================
    private void addSelectedDishToCart() {

        int row = dishTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a dish first");
            return;
        }

        int dishId = (int) tableModel.getValueAt(row, 0);

        String input = JOptionPane.showInputDialog("Enter quantity:");

        try {
            int qty = Integer.parseInt(input);

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be > 0");
                return;
            }

            try (Connection con = DatabaseConnection.getConnection()) {

                // 🔥 تحقق إذا موجود
                String check = "SELECT quantity FROM cart WHERE user_id=? AND dish_id=?";
                PreparedStatement checkStmt = con.prepareStatement(check);
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, dishId);

                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int oldQty = rs.getInt("quantity");

                    PreparedStatement update = con.prepareStatement(
                            "UPDATE cart SET quantity=? WHERE user_id=? AND dish_id=?"
                    );
                    update.setInt(1, oldQty + qty);
                    update.setInt(2, userId);
                    update.setInt(3, dishId);
                    update.executeUpdate();

                } else {
                    PreparedStatement insert = con.prepareStatement(
                            "INSERT INTO cart(user_id,dish_id,quantity) VALUES(?,?,?)"
                    );
                    insert.setInt(1, userId);
                    insert.setInt(2, dishId);
                    insert.setInt(3, qty);
                    insert.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Added to cart ✅");

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    // ================== Navigation ==================
    private void goToCart() {
        dispose();
        new CartForm(userId, restaurantId);
    }
}