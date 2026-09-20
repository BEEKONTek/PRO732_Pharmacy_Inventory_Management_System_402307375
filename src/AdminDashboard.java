import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard(int userId, String fullName) {
        setTitle("HealthFirst PIMS - Admin Dashboard (" + fullName + ")");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Medicines", new ManageMedicinesPanel());
        tabs.addTab("Manage Suppliers", new ManageSuppliersPanel());
        tabs.addTab("Manage Users", new ManageUsersPanel());
        tabs.addTab("Reports", new ReportsPanel());

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(logout);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}