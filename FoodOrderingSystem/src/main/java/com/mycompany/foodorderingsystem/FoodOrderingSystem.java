package com.mycompany.foodorderingsystem;

import javax.swing.*;

public class FoodOrderingSystem {

    public static void main(String[] args) {

        // ================== Database Test ==================
        if (DatabaseConnection.getConnection() == null) {
            JOptionPane.showMessageDialog(null, "Database connection failed!");
            return;
        }

        // ================== Start UI ==================
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}