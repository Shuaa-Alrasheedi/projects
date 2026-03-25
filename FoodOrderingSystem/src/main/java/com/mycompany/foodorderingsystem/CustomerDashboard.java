package com.mycompany.foodorderingsystem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerDashboard extends JFrame {

    // ================== Fields ==================
    private int userId;
    private String customerName;
    private JPanel contentPanel;
    private DefaultTableModel ordersModel;

    // ================== Constructor ==================
    public CustomerDashboard(int userId) {
        this.userId = userId;
        this.customerName = fetchCustomerName(userId);

        initializeFrame();
        initializeTopPanel();
        initializeSidePanel();
        initializeContentPanel();

        setVisible(true);
    }

    // ================== UI Setup ==================
    private void initializeFrame() {
        setTitle("Customer Dashboard");
        setSize(550, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initializeTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + customerName, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        JPanel controlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton logoutButton = new JButton("Logout");
        JButton exitButton = new JButton("Exit");

        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);

        controlButtons.add(logoutButton);
        controlButtons.add(exitButton);
        topPanel.add(controlButtons, BorderLayout.EAST);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        exitButton.addActionListener(e -> System.exit(0));

        add(topPanel, BorderLayout.NORTH);
    }

    private void initializeSidePanel() {
        JPanel sidePanel = new JPanel(new GridLayout(5, 1, 10, 10));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel restaurantLabel = createClickableLabel("Restaurant List");
        JLabel ordersLabel = createClickableLabel("My Orders");

        sidePanel.add(restaurantLabel);
        sidePanel.add(ordersLabel);

        restaurantLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new RestaurantListingForm(userId);
            }
        });

        ordersLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showOrders();
            }
        });

        add(sidePanel, BorderLayout.WEST);
    }

    private void initializeContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createClickableLabel(String text) {
        JLabel label = new JLabel(text);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    // ================== Database ==================
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/foodorderingsystem",
                "root",
                "Sa3597@35"
        );
    }

    private String fetchCustomerName(int userId) {
        String name = "Customer";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT name FROM users WHERE user_id = ?")) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                name = rs.getString("name");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return name;
    }

    // ================== Orders ==================
    private void showOrders() {

        contentPanel.removeAll();

        ordersModel = new DefaultTableModel(
                new String[]{"Order ID", "Status", "Price", "Action"}, 0
        );

        JTable table = new JTable(ordersModel);
        styleTable(table);

        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadOrdersData();

        table.getColumn("Status").setCellRenderer(new StatusColorRenderer());
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this, table));

        refreshUI();
    }

    private void loadOrdersData() {

        String query = "SELECT order_id, status, total FROM orders WHERE user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");

                if (status == null) status = "Pending";

                ordersModel.addRow(new Object[]{
                        rs.getInt("order_id"),
                        status,
                        rs.getDouble("total"),
                        "Completed".equals(status) ? "Done" : "Complete"
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ================== Update ==================
    public void updateOrderStatus(int orderId) {

        String query = "UPDATE orders SET status = 'Completed' WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order Completed ✅");
            showOrders();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ================== UI Helpers ==================
    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);
    }

    private void refreshUI() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
class StatusColorRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
        );

        String status = (value == null) ? "" : value.toString();

        if ("Pending".equals(status)) {
            c.setBackground(Color.YELLOW);
        } else if ("Completed".equals(status)) {
            c.setBackground(Color.GREEN);
        } else {
            c.setBackground(Color.WHITE);
        }

        return c;
    }
}
class ButtonRenderer extends JButton implements TableCellRenderer {

    public ButtonRenderer() {
        setBackground(Color.GREEN);
        setForeground(Color.WHITE);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        setText(value == null ? "" : value.toString());
        return this;
    }
}
class ButtonEditor extends DefaultCellEditor {

    private JButton button;
    private int orderId;
    private boolean clicked;
    private CustomerDashboard dashboard;
    private JTable table;

    public ButtonEditor(JCheckBox checkBox, CustomerDashboard dashboard, JTable table) {
        super(checkBox);
        this.dashboard = dashboard;
        this.table = table;

        button = new JButton();
        button.setBackground(Color.GREEN);
        button.setForeground(Color.WHITE);

        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {

        orderId = (int) table.getValueAt(row, 0);
        button.setText(value == null ? "" : value.toString());
        clicked = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (clicked) {
            Object val = table.getValueAt(table.getSelectedRow(), 1);
            String status = (val == null) ? "" : val.toString();

            if (!"Completed".equals(status)) {
                dashboard.updateOrderStatus(orderId);
            }
        }
        clicked = false;
        return button.getText();
    }
}