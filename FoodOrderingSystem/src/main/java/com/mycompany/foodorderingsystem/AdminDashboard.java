package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private JPanel contentPanel;

    public AdminDashboard() {

        setTitle("Admin Dashboard");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Sidebar =====
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 10, 10));
        sidebar.setBackground(new Color(30, 30, 30));
        sidebar.setPreferredSize(new Dimension(200, 0));

        JButton usersBtn = createButton("Users");
        JButton restaurantsBtn = createButton("Restaurants");
        JButton ordersBtn = createButton("Orders");
        JButton statsBtn = createButton("Statistics");

        sidebar.add(usersBtn);
        sidebar.add(restaurantsBtn);
        sidebar.add(ordersBtn);
        sidebar.add(statsBtn);

        add(sidebar, BorderLayout.WEST);

        // ===== Content =====
        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // ===== Events =====
        usersBtn.addActionListener(e -> showUsers());
        restaurantsBtn.addActionListener(e -> showRestaurants());
        ordersBtn.addActionListener(e -> showOrders());
        statsBtn.addActionListener(e -> showStats());

        showStats();

        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(30, 144, 255));
        btn.setForeground(Color.BLACK);
        return btn;
    }

    private void showUsers() {
        contentPanel.removeAll();
        contentPanel.add(new AdminUsersPanel());
        refresh();
    }

    private void showRestaurants() {
        contentPanel.removeAll();
        contentPanel.add(new AdminRestaurantsPanel());
        refresh();
    }

    private void showOrders() {
        contentPanel.removeAll();
        contentPanel.add(new AdminOrdersPanel());
        refresh();
    }

    private void showStats() {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20));

        panel.add(createCard("Orders", "Auto"));
        panel.add(createCard("Revenue", "Auto"));
        panel.add(createCard("Users", "Auto"));

        contentPanel.add(panel, BorderLayout.NORTH);
        refresh();
    }

    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(50, 50, 50));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        JLabel v = new JLabel(value, SwingConstants.CENTER);

        t.setForeground(Color.WHITE);
        v.setForeground(Color.WHITE);

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);

        return card;
    }

    private void refresh() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}