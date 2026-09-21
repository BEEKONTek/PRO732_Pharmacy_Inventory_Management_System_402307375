import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageUsersPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField usernameField, fullNameField;
    private final JPasswordField passwordField;

    public ManageUsersPanel() {
        setBackground(ThemeUtil.BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[]{"ID","Username","Full Name","Role"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeUtil.styleTable(table);
        add(ThemeUtil.scroll(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBackground(ThemeUtil.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeUtil.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        form.setPreferredSize(new Dimension(340, 0));

        form.add(ThemeUtil.label("Username:"));
        usernameField = ThemeUtil.textField(14); form.add(usernameField);
        form.add(ThemeUtil.label("Password:"));
        passwordField = ThemeUtil.passwordField(14); form.add(passwordField);
        form.add(ThemeUtil.label("Full name:"));
        fullNameField = ThemeUtil.textField(14); form.add(fullNameField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        JButton addBtn     = ThemeUtil.primaryButton("Add cashier");
        JButton deleteBtn  = ThemeUtil.dangerButton("Delete");
        JButton clearBtn   = ThemeUtil.secondaryButton("Clear");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        btns.add(addBtn); btns.add(deleteBtn); btns.add(clearBtn); btns.add(refreshBtn);
        form.add(new JLabel());
        form.add(btns);

        add(form, BorderLayout.EAST);

        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> loadUsers());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                usernameField.setText(str(model.getValueAt(r, 1)));
                fullNameField.setText(str(model.getValueAt(r, 2)));
            }
        });

        loadUsers();
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }

    private void loadUsers() {
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT user_id, username, full_name, role FROM users ORDER BY user_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("full_name"), rs.getString("role")
                });
            }
        } catch (SQLException ex) { ThemeUtil.error(this, "Error: " + ex.getMessage()); }
    }

    private void addUser() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        String n = fullNameField.getText().trim();
        if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
            ThemeUtil.warn(this, "Fill in username, password and full name.");
            return;
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users (username, password, role, full_name) VALUES (?,?, 'Cashier', ?)")) {
            ps.setString(1, u);
            ps.setString(2, p);
            ps.setString(3, n);
            ps.executeUpdate();
            loadUsers(); clearForm();
        } catch (SQLException ex) { ThemeUtil.error(this, "Add failed: " + ex.getMessage()); }
    }

    private void deleteUser() {
        int r = table.getSelectedRow();
        if (r == -1) { ThemeUtil.warn(this, "Select a user to delete."); return; }
        int id = (int) model.getValueAt(r, 0);
        String role = str(model.getValueAt(r, 3));
        if ("Admin".equalsIgnoreCase(role)) {
            ThemeUtil.warn(this, "Administrator accounts cannot be deleted here.");
            return;
        }
        if (!ThemeUtil.confirm(this, "Delete user ID " + id + "?")) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadUsers(); clearForm();
        } catch (SQLException ex) { ThemeUtil.error(this, "Delete failed: " + ex.getMessage()); }
    }

    private void clearForm() {
        usernameField.setText(""); passwordField.setText(""); fullNameField.setText("");
        table.clearSelection();
    }
}
