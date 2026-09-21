import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageSuppliersPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField nameField, contactField, phoneField, emailField, addressField;

    public ManageSuppliersPanel() {
        setBackground(ThemeUtil.BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[]{"ID","Name","Contact Person","Phone","Email","Address"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeUtil.styleTable(table);
        add(ThemeUtil.scroll(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBackground(ThemeUtil.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeUtil.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        form.setPreferredSize(new Dimension(340, 0));

        form.add(ThemeUtil.label("Name:"));
        nameField = ThemeUtil.textField(14); form.add(nameField);
        form.add(ThemeUtil.label("Contact person:"));
        contactField = ThemeUtil.textField(14); form.add(contactField);
        form.add(ThemeUtil.label("Phone:"));
        phoneField = ThemeUtil.textField(14); form.add(phoneField);
        form.add(ThemeUtil.label("Email:"));
        emailField = ThemeUtil.textField(14); form.add(emailField);
        form.add(ThemeUtil.label("Address:"));
        addressField = ThemeUtil.textField(14); form.add(addressField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        JButton addBtn     = ThemeUtil.primaryButton("Add");
        JButton updateBtn  = ThemeUtil.secondaryButton("Update");
        JButton deleteBtn  = ThemeUtil.dangerButton("Delete");
        JButton clearBtn   = ThemeUtil.secondaryButton("Clear");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn); btns.add(clearBtn); btns.add(refreshBtn);
        form.add(new JLabel());
        form.add(btns);

        add(form, BorderLayout.EAST);

        addBtn.addActionListener(e -> addSupplier());
        updateBtn.addActionListener(e -> updateSupplier());
        deleteBtn.addActionListener(e -> deleteSupplier());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> loadSuppliers());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                nameField.setText(str(model.getValueAt(r, 1)));
                contactField.setText(str(model.getValueAt(r, 2)));
                phoneField.setText(str(model.getValueAt(r, 3)));
                emailField.setText(str(model.getValueAt(r, 4)));
                addressField.setText(str(model.getValueAt(r, 5)));
            }
        });

        loadSuppliers();
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }

    private void loadSuppliers() {
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM suppliers ORDER BY supplier_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact_person"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address")
                });
            }
        } catch (SQLException ex) { ThemeUtil.error(this, "Error: " + ex.getMessage()); }
    }

    private void addSupplier() {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?,?,?,?,?)")) {
            ps.setString(1, nameField.getText());
            ps.setString(2, contactField.getText());
            ps.setString(3, phoneField.getText());
            ps.setString(4, emailField.getText());
            ps.setString(5, addressField.getText());
            ps.executeUpdate();
            loadSuppliers(); clearForm();
        } catch (SQLException ex) { ThemeUtil.error(this, "Add failed: " + ex.getMessage()); }
    }

    private void updateSupplier() {
        int r = table.getSelectedRow();
        if (r == -1) { ThemeUtil.warn(this, "Select a supplier to update."); return; }
        int id = (int) model.getValueAt(r, 0);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?")) {
            ps.setString(1, nameField.getText());
            ps.setString(2, contactField.getText());
            ps.setString(3, phoneField.getText());
            ps.setString(4, emailField.getText());
            ps.setString(5, addressField.getText());
            ps.setInt(6, id);
            ps.executeUpdate();
            loadSuppliers(); clearForm();
        } catch (SQLException ex) { ThemeUtil.error(this, "Update failed: " + ex.getMessage()); }
    }

    private void deleteSupplier() {
        int r = table.getSelectedRow();
        if (r == -1) { ThemeUtil.warn(this, "Select a supplier to delete."); return; }
        int id = (int) model.getValueAt(r, 0);
        if (!ThemeUtil.confirm(this, "Delete supplier ID " + id + "?")) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM suppliers WHERE supplier_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadSuppliers(); clearForm();
        } catch (SQLException ex) { ThemeUtil.error(this, "Delete failed: " + ex.getMessage()); }
    }

    private void clearForm() {
        nameField.setText(""); contactField.setText(""); phoneField.setText("");
        emailField.setText(""); addressField.setText("");
        table.clearSelection();
    }
}
