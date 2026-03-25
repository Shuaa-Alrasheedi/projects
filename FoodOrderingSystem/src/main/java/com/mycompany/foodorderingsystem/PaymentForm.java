package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class PaymentForm extends JFrame {

    private double totalAmount;
    private int userId;
    private int restaurantId;

    public PaymentForm(double totalAmount, int userId, int restaurantId) {
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.restaurantId = restaurantId;

        initializeUI();
        setVisible(true);
    }

    // ================== UI ==================
    private void initializeUI() {

        setTitle("Payment");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2, 10, 10));

        JTextField cardField = new JTextField();
        JTextField expiryField = new JTextField();
        JTextField cvvField = new JTextField();
        JTextField nameField = new JTextField();

        JLabel amountLabel = new JLabel("Total: $" + totalAmount);

        JButton payBtn = new JButton("Pay");
        JButton backBtn = new JButton("Back");

        add(new JLabel("Card Number:")); add(cardField);
        add(new JLabel("Expiry (MM/YY):")); add(expiryField);
        add(new JLabel("CVV:")); add(cvvField);
        add(new JLabel("Name:")); add(nameField);
        add(new JLabel()); add(amountLabel);
        add(payBtn); add(backBtn);

        // ===== Back =====
        backBtn.addActionListener(e -> {
            dispose();
            new CartForm(userId, restaurantId);
        });

        // ===== Pay =====
        payBtn.addActionListener(e -> processPayment(
                cardField.getText(),
                expiryField.getText(),
                cvvField.getText(),
                nameField.getText()
        ));
    }

    // ================== Payment Logic ==================
    private void processPayment(String card, String expiry, String cvv, String name) {

        if (card.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || name.isEmpty()) {
            showMsg("Fill all fields");
            return;
        }

        if (!card.matches("\\d{13,16}")) {
            showMsg("Invalid card number");
            return;
        }

        if (!cvv.matches("\\d{3}")) {
            showMsg("Invalid CVV");
            return;
        }

        try {
            YearMonth.parse(expiry, DateTimeFormatter.ofPattern("MM/yy"));
        } catch (Exception e) {
            showMsg("Invalid expiry format");
            return;
        }

        try (Connection con = DatabaseConnection.getConnection()) {

            con.setAutoCommit(false);

            // ===== Create Order =====
            PreparedStatement order = con.prepareStatement(
                    "INSERT INTO orders(user_id, restaurant_id, total, status) VALUES(?,?,?, 'Pending')",
                    Statement.RETURN_GENERATED_KEYS
            );

            order.setInt(1, userId);
            order.setInt(2, restaurantId);
            order.setDouble(3, totalAmount);
            order.executeUpdate();

            ResultSet rs = order.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            // ===== Get cart items =====
            PreparedStatement cart = con.prepareStatement(
                    "SELECT dish_id, quantity FROM cart WHERE user_id=?"
            );
            cart.setInt(1, userId);
            ResultSet items = cart.executeQuery();

            while (items.next()) {

                int dishId = items.getInt("dish_id");
                int qty = items.getInt("quantity");

                PreparedStatement priceStmt = con.prepareStatement(
                        "SELECT price FROM dishes WHERE dish_id=?"
                );
                priceStmt.setInt(1, dishId);

                ResultSet pr = priceStmt.executeQuery();
                if (pr.next()) {

                    PreparedStatement insert = con.prepareStatement(
                            "INSERT INTO order_items(order_id,dish_id,quantity,price) VALUES(?,?,?,?)"
                    );

                    insert.setInt(1, orderId);
                    insert.setInt(2, dishId);
                    insert.setInt(3, qty);
                    insert.setDouble(4, pr.getDouble("price"));
                    insert.executeUpdate();
                }
            }

            // ===== Save Payment =====
            PreparedStatement payment = con.prepareStatement(
                    "INSERT INTO payments(order_id, amount, payment_method) VALUES(?,?,?)"
            );

            payment.setInt(1, orderId);
            payment.setDouble(2, totalAmount);
            payment.setString(3, "Card");
            payment.executeUpdate();

            // ===== Clear cart =====
            PreparedStatement clear = con.prepareStatement(
                    "DELETE FROM cart WHERE user_id=?"
            );
            clear.setInt(1, userId);
            clear.executeUpdate();

            con.commit();

            showMsg("Payment Successful ✅");

            dispose();

            // 🔥 الانتقال للتتبع
            new CustomerOrderTrackingForm(userId, restaurantId);

        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Payment failed");
        }
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}