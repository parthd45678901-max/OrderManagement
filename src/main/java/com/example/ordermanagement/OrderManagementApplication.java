package com.example.ordermanagement;

import java.sql.*;
import java.util.Scanner;

public class OrderManagementApplication {

    static final String URL = "jdbc:mysql://localhost:3307/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "Parth@12345";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Database Connected Successfully!");

            int choice;

            do {
                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Three Orders");
                System.out.println("2. Display Orders by Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Display Total Order Value");
                System.out.println("6. Delete Cancelled Order");
                System.out.println("0. Exit");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        insertOrders(con);
                        break;

                    case 2:
                        displayOrders(con);
                        break;

                    case 3:
                        increaseQuantity(con);
                        break;

                    case 4:
                        updateStatus(con);
                        break;

                    case 5:
                        totalOrderValue(con);
                        break;

                    case 6:
                        deleteOrder(con);
                        break;

                    case 0:
                        System.out.println("Exiting Application...");
                        break;

                    default:
                        System.out.println("Invalid Choice!");
                }

            } while (choice != 0);

            con.close();
            sc.close();

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // A. INSERT THREE ORDERS

    static void insertOrders(Connection con) {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        Object[][] orders = {
                {101, "Rahul", "Laptop", 2, 55000.0, "Pending"},
                {102, "Aman", "Mobile", 5, 20000.0, "Shipped"},
                {103, "Riya", "Headphones", 4, 1500.0, "Delivered"}
        };

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (Object[] order : orders) {

                try {
                    ps.setInt(1, (Integer) order[0]);
                    ps.setString(2, (String) order[1]);
                    ps.setString(3, (String) order[2]);
                    ps.setInt(4, (Integer) order[3]);
                    ps.setDouble(5, (Double) order[4]);
                    ps.setString(6, (String) order[5]);

                    ps.executeUpdate();

                    System.out.println("Order " + order[0] + " inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID: " + order[0]);
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // B. DISPLAY ORDERS BY STATUS AND QUANTITY > 2

    static void displayOrders(Connection con) {

        System.out.print("Enter order status: ");
        String status = sc.next();

        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    System.out.println("\nOrder ID: " + rs.getInt("order_id"));
                    System.out.println("Customer: " + rs.getString("customer_name"));
                    System.out.println("Product: " + rs.getString("product_name"));
                    System.out.println("Quantity: " + rs.getInt("quantity"));
                    System.out.println("Unit Price: " + rs.getDouble("unit_price"));
                    System.out.println("Status: " + rs.getString("status"));
                }

                if (!found) {
                    System.out.println("No matching orders found!");
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // C. INCREASE ORDER QUANTITY

    static void increaseQuantity(Connection con) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter additional quantity: ");
        int quantity = sc.nextInt();

        if (quantity <= 0) {
            System.out.println("Additional quantity must be positive!");
            return;
        }

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Order quantity updated successfully!");
            } else {
                System.out.println("Order does not exist!");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // D. UPDATE ORDER STATUS

    static void updateStatus(Connection con) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter new status (Pending/Shipped/Delivered): ");
        String status = sc.next();

        if (!(status.equalsIgnoreCase("Pending") ||
                status.equalsIgnoreCase("Shipped") ||
                status.equalsIgnoreCase("Delivered"))) {

            System.out.println("Invalid Status!");
            return;
        }

        if (status.equalsIgnoreCase("Pending"))
            status = "Pending";
        else if (status.equalsIgnoreCase("Shipped"))
            status = "Shipped";
        else
            status = "Delivered";

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Order Status Updated Successfully!");

                String selectSql = "SELECT * FROM Orders WHERE order_id = ?";

                try (PreparedStatement select = con.prepareStatement(selectSql)) {

                    select.setInt(1, id);

                    try (ResultSet rs = select.executeQuery()) {

                        if (rs.next()) {
                            System.out.println("\nUpdated Order Details:");
                            System.out.println("Order ID: " + rs.getInt("order_id"));
                            System.out.println("Customer: " + rs.getString("customer_name"));
                            System.out.println("Product: " + rs.getString("product_name"));
                            System.out.println("Quantity: " + rs.getInt("quantity"));
                            System.out.println("Unit Price: " + rs.getDouble("unit_price"));
                            System.out.println("Status: " + rs.getString("status"));
                        }
                    }
                }

            } else {
                System.out.println("Order does not exist!");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // E. DISPLAY TOTAL ORDER VALUE

    static void totalOrderValue(Connection con) {

        String sql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("Total Order Value: " + rs.getDouble("total"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // E. DELETE ORDER ONLY IF STATUS IS CANCELLED

    static void deleteOrder(Connection con) {

        System.out.print("Enter Order ID to delete: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Cancelled Order Deleted Successfully!");
            } else {
                System.out.println("Order cannot be deleted! It does not exist or is not Cancelled.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}