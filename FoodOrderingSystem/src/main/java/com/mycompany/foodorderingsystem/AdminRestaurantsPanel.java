package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminRestaurantsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public AdminRestaurantsPanel() {

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Location", "Category"}, 0
        );

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Restaurant");
        deleteBtn.addActionListener(e -> deleteRestaurant());

        add(deleteBtn, BorderLayout.SOUTH);

        loadRestaurants();
    }

    private void loadRestaurants() {

        model.setRowCount(0);

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT r.restaurant_id, u.name, r.location, r.category " +
                             "FROM restaurants r JOIN users u ON r.user_id=u.user_id"
             )) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("category")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteRestaurant() {

        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM restaurants WHERE restaurant_id=?"
             )) {

            ps.setInt(1, id);
            ps.executeUpdate();

            loadRestaurants();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}