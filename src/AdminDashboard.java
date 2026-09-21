import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard(int userId, String fullName) {
        ThemeUtil.install();
        setTitle("HealthFirst PIMS - Administrator (" + fullName + ")");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1080, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(ThemeUtil.roleChip("ADMIN"));
        JLabel name = new JLabel(fullName);
        name.setForeground(Color.WHITE);
        name.setFont(ThemeUtil.FONT_LABEL);
        right.add(name);
        JButton logout = new JButton("Logout");
        logout.setBackground(ThemeUtil.CARD);
        logout.setForeground(ThemeUtil.PRIMARY_DARK);
        logout.setFont(ThemeUtil.FONT_BTN);
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            if (ThemeUtil.confirm(this, "Sign out of HealthFirst PIMS?")) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        right.add(logout);
        add(ThemeUtil.headerBar("Administrator Console",
                "Manage inventory, suppliers, users and reports", right), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeUtil.FONT_LABEL);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tabs.addTab("  Medicines  ", new ManageMedicinesPanel());
        tabs.addTab("  Suppliers  ", new ManageSuppliersPanel());
        tabs.addTab("  Users  ",     new ManageUsersPanel());
        tabs.addTab("  Reports  ",   new ReportsPanel());
        add(tabs, BorderLayout.CENTER);

        JLabel status = ThemeUtil.muted("Signed in as " + fullName + " (Administrator)");
        status.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(ThemeUtil.BG);
        south.add(status, BorderLayout.WEST);
        add(south, BorderLayout.SOUTH);
    }
}
