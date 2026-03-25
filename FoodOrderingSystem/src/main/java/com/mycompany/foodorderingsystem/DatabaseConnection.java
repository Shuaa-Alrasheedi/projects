package com.mycompany.foodorderingsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/foodorderingsystem";
    private static final String USER = "root";
    private static final String PASSWORD = "Sa3597@35";

    // ================== Get Connection ==================
    public static Connection getConnection() {
        try {
            // تحميل الدرايفر (اختياري في Java الحديثة)
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }
        return null;
    }
}