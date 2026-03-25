package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class OrderTrackingForm extends JFrame {

    // ================== Fields ==================
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    private JButton refreshButton, updateStatusButton, deleteOrderButton, backButton;
    private JComboBox<String> statusComboBox;

    private int restaurantId;
    private javax.swing.Timer timer;

    private Map<Integer, String> lastOrderStatuses = new HashMap<>();

    // ================== Constructor ==================
    public OrderTrackingForm(int restaurantId) {
        this.restaurantId = restaurantId;

        initializeUI();
        loadOrders(false);
        startAutoRefresh();

        setVisible(true);
    }

    // ================== UI ==================
    private void initializeUI() {

        setTitle("Order Tracking");
        setSize(850, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ===== Table =====
        tableModel = new DefaultTableModel(
                new String[]{"Order ID", "User ID", "Total", "Status", "Created At"}, 0
        );

        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(30);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 🎨 تلوين الحالة
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                String status = value.toString();

                if (!isSelected) {
                    switch (status) {
                        case "pending":
                            c.setBackground(Color.YELLOW);
                            break;
                        case "preparing":
                            c.setBackground(Color.CYAN);
                            break;
                        case "on_the_way":
                            c.setBackground(Color.ORANGE);
                            break;
                        case "delivered":
                            c.setBackground(Color.GREEN);
                            break;
                        case "cancelled":
                            c.setBackground(Color.RED);
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        // ===== Top Panel =====
        statusComboBox = new JComboBox<>(new String[]{
                "pending", "preparing", "on_the_way", "delivered", "cancelled"
        });

        updateStatusButton = new JButton("Update Status");
        deleteOrderButton = new JButton("Delete Order");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Status:"));
        topPanel.add(statusComboBox);
        topPanel.add(updateStatusButton);
        topPanel.add(deleteOrderButton);

        add(topPanel, BorderLayout.NORTH);

        // ===== Bottom Panel =====
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Events =====
        refreshButton.addActionListener(e -> loadOrders(false));
        updateStatusButton.addActionListener(e -> updateOrderStatus());
        deleteOrderButton.addActionListener(e -> deleteOrder());
        backButton.addActionListener(e -> dispose());
    }

    // ================== DB ==================
    private Connection connect() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // ================== Load Orders ==================
    private void loadOrders(boolean checkChanges) {

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM orders WHERE restaurant_id=?"
             )) {

            ps.setInt(1, restaurantId);
            ResultSet rs = ps.executeQuery();

            Map<Integer, String> currentStatuses = new HashMap<>();
            tableModel.setRowCount(0);

            while (rs.next()) {

                int orderId = rs.getInt("order_id");
                String status = rs.getString("status");

                currentStatuses.put(orderId, status);

                tableModel.addRow(new Object[]{
                        orderId,
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        status,
                        rs.getTimestamp("created_at")
                });

                if (checkChanges) {
                    String old = lastOrderStatuses.get(orderId);
                    if (old != null && !old.equals(status)) {
                        showNotification(orderId, old, status);
                    }
                }
            }

            lastOrderStatuses = currentStatuses;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== Update ==================
    private void updateOrderStatus() {

        int row = ordersTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order first");
            return;
        }

        int orderId = (int) tableModel.getValueAt(row, 0);

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE orders SET status=? WHERE order_id=? AND restaurant_id=?"
             )) {

            ps.setString(1, (String) statusComboBox.getSelectedItem());
            ps.setInt(2, orderId);
            ps.setInt(3, restaurantId);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Updated ✅");
                loadOrders(false);
            } else {
                JOptionPane.showMessageDialog(this, "Order not found");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating");
        }
    }

    // ================== Delete ==================
    private void deleteOrder() {

        int row = ordersTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order first");
            return;
        }

        int orderId = (int) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?");
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM orders WHERE order_id=? AND restaurant_id=?"
             )) {

            ps.setInt(1, orderId);
            ps.setInt(2, restaurantId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Deleted ✅");
            loadOrders(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting");
        }
    }

    // ================== Timer ==================
    private void startAutoRefresh() {
        timer = new javax.swing.Timer(60000, e -> loadOrders(true));
        timer.start();
    }

    // ================== Notification ==================
    private void showNotification(int id, String oldS, String newS) {
        JOptionPane.showMessageDialog(this,
                "Order #" + id + "\n" + oldS + " → " + newS);
    }

    @Override
    public void dispose() {
        if (timer != null) timer.stop();
        super.dispose();
    }
}