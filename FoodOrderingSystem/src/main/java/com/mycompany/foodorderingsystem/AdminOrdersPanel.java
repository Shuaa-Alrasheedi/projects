package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminOrdersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public AdminOrdersPanel() {

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"Order ID", "User", "Total", "Status"}, 0
        );

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadOrders();
    }

    private void loadOrders() {

        model.setRowCount(0);

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM orders")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getString("status")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}