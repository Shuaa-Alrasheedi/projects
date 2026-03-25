package com.mycompany.foodorderingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ReviewAndTrackingForm extends JFrame {

    // ================== Fields ==================
    private JLabel[] starLabels = new JLabel[5];
    private int selectedRating = 0;
    private JTextArea commentArea;
    private JButton submitButton;
    private JPanel reviewsContainer;

    private int userId;
    private int restaurantId; // 🔥 مهم جداً

    // ================== Constructor ==================
    public ReviewAndTrackingForm(int userId, int restaurantId) {
        this.userId = userId;
        this.restaurantId = restaurantId;

        initializeFrame();
        initializeUI();
        loadReviewsFromDatabase();

        setVisible(true);
    }

    // ================== UI ==================
    private void initializeFrame() {
        setTitle("Submit Review");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // ===== Stars =====
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        starsPanel.add(new JLabel("Rating:"));

        Font starFont = new Font("SansSerif", Font.PLAIN, 30);

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;

            starLabels[i] = new JLabel("☆");
            starLabels[i].setFont(starFont);
            starLabels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            starLabels[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    setRating(rating);
                }
            });

            starsPanel.add(starLabels[i]);
        }

        // ===== Comment =====
        commentArea = new JTextArea(3, 40);
        JScrollPane commentScroll = new JScrollPane(commentArea);

        // ===== Button =====
        submitButton = new JButton("Submit Review");

        submitButton.addActionListener(e -> submitReview());

        // ===== Reviews =====
        reviewsContainer = new JPanel();
        reviewsContainer.setLayout(new BoxLayout(reviewsContainer, BoxLayout.Y_AXIS));

        JScrollPane reviewsScroll = new JScrollPane(reviewsContainer);

        // ===== Add =====
        mainPanel.add(starsPanel);
        mainPanel.add(commentScroll);
        mainPanel.add(submitButton);
        mainPanel.add(new JLabel("All Reviews:"));
        mainPanel.add(reviewsScroll);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ================== Logic ==================
    private void submitReview() {

        if (selectedRating == 0) {
            showMessage("Please select a rating.");
            return;
        }

        String comment = commentArea.getText().trim();
        if (comment.isEmpty()) comment = "No comment";

        saveReviewToDatabase(selectedRating, comment);

        commentArea.setText("");
        setRating(0);

        reviewsContainer.removeAll();
        loadReviewsFromDatabase();
    }

    private void setRating(int rating) {
        selectedRating = rating;

        for (int i = 0; i < 5; i++) {
            starLabels[i].setText(i < rating ? "★" : "☆");
        }
    }

    // ================== Database ==================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/foodorderingsystem",
                "root",
                "Sa3597@35"
        );
    }

    private void saveReviewToDatabase(int rating, String comment) {

        String query = "INSERT INTO Reviews (user_id, restaurant_id, rating, comment) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);        // ✅ صحيح
            ps.setInt(2, restaurantId);  // ✅ صحيح
            ps.setInt(3, rating);
            ps.setString(4, comment);

            ps.executeUpdate();

            showMessage("Review submitted successfully!");

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Database error: " + ex.getMessage());
        }
    }

    private void loadReviewsFromDatabase() {

        String query = "SELECT rating, comment FROM Reviews WHERE restaurant_id=? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, restaurantId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                addReviewCard(
                        rs.getInt("rating"),
                        rs.getString("comment")
                );
            }

            reviewsContainer.revalidate();
            reviewsContainer.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Database error: " + ex.getMessage());
        }
    }

    // ================== UI Helpers ==================
    private void addReviewCard(int rating, String comment) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setBackground(Color.WHITE);

        card.add(new JLabel("Rating: " + rating + " ⭐"));
        card.add(new JLabel("<html>" + comment + "</html>"));

        reviewsContainer.add(card);
        reviewsContainer.add(Box.createVerticalStrut(10));
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}