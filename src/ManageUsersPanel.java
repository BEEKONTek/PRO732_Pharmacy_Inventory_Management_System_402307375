import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageUsersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField usernameField, fullNameField;
    private JPasswordField passwordField;

    public ManageUsersPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new String[]{"ID","Username","Full Name","Role"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Cashier Account"));
        form.add(new JLabel("Username:")); usernameField = new JTextField(); form.add(usernameField);
        form.add(new JLabel("Password:")); passwordField = new JPasswordField(); form.add(passwordField);
        form.add(new JLabel("Full Name:")); fullNameField = new JTextField(); form.add(fullNameField);

        JPanel btns = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Cashier");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");
        btns.add(addBtn); btns.add(deleteBtn); btns.add(clearBtn); btns.add(refreshBtn);
        form.add(btns);
        add(form, BorderLayout.EAST);

        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> loadUsers());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                usernameField.setText(model.getValueAt(r, 1).toString());
                fullNameField.setText(model.getValueAt(r, 2).toString());
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id, username, full_name, role FROM users ORDER BY user_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"), rs.getString("username"),
                    rs.getString("full_name"), rs.getString("role")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void addUser() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO users (username, password, role, full_name) VALUES (?,?, 'Cashier', ?)")) {
            ps.setString(1, usernameField.getText());
            ps.setString(2, new String(passwordField.getPassword()));
            ps.setString(3, fullNameField.getText());
            ps.executeUpdate();
            loadUsers();
            clearForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a user."); return; }
        int id = (int) model.getValueAt(r, 0);
        String role = model.getValueAt(r, 3).toString();
        if ("Admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "Cannot delete Admin.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete user ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadUsers();
            clearForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        usernameField.setText(""); passwordField.setText(""); fullNameField.setText("");
        table.clearSelection();
    }
}