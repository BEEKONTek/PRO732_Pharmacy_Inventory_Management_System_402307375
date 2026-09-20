import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageSuppliersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField nameField, contactField, phoneField, emailField, addressField;

    public ManageSuppliersPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new String[]{"ID","Name","Contact Person","Phone","Email","Address"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        form.add(new JLabel("Name:")); nameField = new JTextField(); form.add(nameField);
        form.add(new JLabel("Contact Person:")); contactField = new JTextField(); form.add(contactField);
        form.add(new JLabel("Phone:")); phoneField = new JTextField(); form.add(phoneField);
        form.add(new JLabel("Email:")); emailField = new JTextField(); form.add(emailField);
        form.add(new JLabel("Address:")); addressField = new JTextField(); form.add(addressField);

        JPanel btns = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");
        btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn); btns.add(clearBtn); btns.add(refreshBtn);
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
                nameField.setText(model.getValueAt(r, 1).toString());
                contactField.setText(model.getValueAt(r, 2).toString());
                phoneField.setText(model.getValueAt(r, 3).toString());
                emailField.setText(model.getValueAt(r, 4).toString());
                addressField.setText(model.getValueAt(r, 5).toString());
            }
        });

        loadSuppliers();
    }

    private void loadSuppliers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM suppliers ORDER BY supplier_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact_person"),
                    rs.getString("phone"), rs.getString("email"), rs.getString("address")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void addSupplier() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?,?,?,?,?)")) {
            ps.setString(1, nameField.getText());
            ps.setString(2, contactField.getText());
            ps.setString(3, phoneField.getText());
            ps.setString(4, emailField.getText());
            ps.setString(5, addressField.getText());
            ps.executeUpdate();
            loadSuppliers();
            clearForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateSupplier() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a supplier."); return; }
        int id = (int) model.getValueAt(r, 0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?")) {
            ps.setString(1, nameField.getText());
            ps.setString(2, contactField.getText());
            ps.setString(3, phoneField.getText());
            ps.setString(4, emailField.getText());
            ps.setString(5, addressField.getText());
            ps.setInt(6, id);
            ps.executeUpdate();
            loadSuppliers();
            clearForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteSupplier() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a supplier."); return; }
        int id = (int) model.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete supplier ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM suppliers WHERE supplier_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadSuppliers();
            clearForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText(""); contactField.setText(""); phoneField.setText("");
        emailField.setText(""); addressField.setText("");
        table.clearSelection();
    }
}