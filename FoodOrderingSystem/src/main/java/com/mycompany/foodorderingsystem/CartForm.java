package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartForm extends JFrame {

    // ================== Fields ==================
    private final int userId;
    private final int restaurantId;

    private JTable table;
    private DefaultTableModel model;
    private JLabel totalLabel;

    private List<CartItem> items = new ArrayList<>();

    // ================== Constructor ==================
    public CartForm(int userId, int restaurantId) {
        this.userId = userId;
        this.restaurantId = restaurantId;

        initializeUI();
        loadCart();

        setVisible(true);
    }

    // ================== UI ==================
    private void initializeUI() {
        setTitle("Cart");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"Name", "Price", "Qty"}, 0);
        table = new JTable(model);

        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(totalLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel();

        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton checkoutBtn = new JButton("Checkout");

        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(checkoutBtn);

        add(panel, BorderLayout.SOUTH);

        // Events
        updateBtn.addActionListener(e -> updateQuantity());
        deleteBtn.addActionListener(e -> deleteItem());
        checkoutBtn.addActionListener(e -> checkout());
    }

    // ================== Database ==================
    private Connection connect() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // ================== Load Cart ==================
    private void loadCart() {
        model.setRowCount(0);
        items.clear();

        double total = 0;

        try (Connection con = connect()) {

            String sql = """
                    SELECT d.name, d.price, c.quantity, c.dish_id
                    FROM cart c
                    JOIN dishes d ON c.dish_id = d.dish_id
                    WHERE c.user_id=?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("quantity");
                int dishId = rs.getInt("dish_id");

                items.add(new CartItem(dishId, name, price, qty));
                model.addRow(new Object[]{name, price, qty});

                total += price * qty;
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading cart");
        }

        totalLabel.setText("Total: " + total);
    }

    // ================== Update ==================
    private void updateQuantity() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select item first");
            return;
        }

        String input = JOptionPane.showInputDialog("New quantity:");

        try {
            int qty = Integer.parseInt(input);

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be > 0");
                return;
            }

            CartItem item = items.get(row);

            try (Connection con = connect()) {

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE cart SET quantity=? WHERE user_id=? AND dish_id=?"
                );

                ps.setInt(1, qty);
                ps.setInt(2, userId);
                ps.setInt(3, item.getDishId());
                ps.executeUpdate();
            }

            loadCart();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    // ================== Delete ==================
    private void deleteItem() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select item first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this item?"
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        CartItem item = items.get(row);

        try (Connection con = connect()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM cart WHERE user_id=? AND dish_id=?"
            );

            ps.setInt(1, userId);
            ps.setInt(2, item.getDishId());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        loadCart();
    }

    // ================== Checkout ==================
    private void checkout() {

        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        try (Connection con = connect()) {

            // Create Order
            PreparedStatement order = con.prepareStatement(
                    "INSERT INTO orders(user_id,total,status) VALUES(?,?, 'Pending')",
                    Statement.RETURN_GENERATED_KEYS
            );

            order.setInt(1, userId);
            order.setDouble(2, total);
            order.executeUpdate();

            ResultSet rs = order.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            // Insert Order Items
            for (CartItem i : items) {

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO order_items(order_id,dish_id,quantity,price) VALUES(?,?,?,?)"
                );

                ps.setInt(1, orderId);
                ps.setInt(2, i.getDishId());
                ps.setInt(3, i.getQuantity());
                ps.setDouble(4, i.getPrice());
                ps.executeUpdate();
            }

            // Clear Cart
            PreparedStatement clear = con.prepareStatement(
                    "DELETE FROM cart WHERE user_id=?"
            );
            clear.setInt(1, userId);
            clear.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order placed ✅");

            // 🔥 الانتقال إلى التتبع
            dispose();
            new CustomerOrderTrackingForm(userId, restaurantId);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Checkout failed");
        }
    }
}

// ================== Model ==================
class CartItem {

    private int dishId;
    private String name;
    private double price;
    private int quantity;

    public CartItem(int dishId, String name, double price, int quantity) {
        this.dishId = dishId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getDishId() {
        return dishId;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}