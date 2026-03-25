package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class CustomerOrderTrackingForm extends JFrame {

    // ================== Fields ==================
    private JLabel statusLabel;
    private int userId;
    private int restaurantId;
    private Timer timer;
    private int step = 0;

    // ================== Constructor ==================
    public CustomerOrderTrackingForm(int userId, int restaurantId) {
        this.userId = userId;
        this.restaurantId = restaurantId;

        initializeUI();
        startTracking();

        setVisible(true);
    }

    // ================== UI ==================
    private void initializeUI() {
        setTitle("Order Tracking");
        setSize(450, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Checking your order status...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        add(statusLabel, BorderLayout.CENTER);
    }

    // ================== Tracking Logic ==================
    private void startTracking() {

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                SwingUtilities.invokeLater(() -> updateStatus());

            }
        }, 3000, 3000); // كل 3 ثواني
    }

    private void updateStatus() {

        step++;

        switch (step) {

            case 1:
                statusLabel.setText("Your order is being prepared.");
                break;

            case 2:
                statusLabel.setText("Your order is on the way!");
                break;

            case 3:
                statusLabel.setText("Your order has been delivered.");

                // إيقاف التايمر
                timer.cancel();

                // تأخير بسيط ثم فتح التقييم
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            new ReviewAndTrackingForm(userId, restaurantId); // ✅ صحيح
                        });
                    }
                }, 2000);

                break;
        }
    }
}