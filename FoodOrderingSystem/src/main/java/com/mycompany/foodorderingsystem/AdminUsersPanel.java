package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminUsersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public AdminUsersPanel() {

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Email", "Role"}, 0
        );

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete User");
        deleteBtn.addActionListener(e -> deleteUser());

        add(deleteBtn, BorderLayout.SOUTH);

        loadUsers();
    }

    private void loadUsers() {
        model.setRowCount(0);

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteUser() {

        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM users WHERE user_id=?"
             )) {

            ps.setInt(1, id);
            ps.executeUpdate();

            loadUsers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}