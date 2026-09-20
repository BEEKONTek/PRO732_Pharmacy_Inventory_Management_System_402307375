import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageMedicinesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField nameField, companyField, typeField, priceField, qtyField, reorderField, expiryField;
    private JComboBox<String> supplierCombo;
    private JButton addBtn, updateBtn, deleteBtn, clearBtn, refreshBtn;

    public ManageMedicinesPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new String[]{"ID","Name","Company","Type","Price","Qty","Reorder","Expiry","Supplier"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(9, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Medicine Details"));
        form.add(new JLabel("Name:")); nameField = new JTextField(); form.add(nameField);
        form.add(new JLabel("Company:")); companyField = new JTextField(); form.add(companyField);
        form.add(new JLabel("Type:")); typeField = new JTextField(); form.add(typeField);
        form.add(new JLabel("Price:")); priceField = new JTextField(); form.add(priceField);
        form.add(new JLabel("Quantity:")); qtyField = new JTextField(); form.add(qtyField);
        form.add(new JLabel("Reorder Level:")); reorderField = new JTextField(); form.add(reorderField);
        form.add(new JLabel("Expiry (YYYY-MM-DD):")); expiryField = new JTextField(); form.add(expiryField);
        form.add(new JLabel("Supplier:")); supplierCombo = new JComboBox<>(); form.add(supplierCombo);

        JPanel btns = new JPanel(new FlowLayout());
        addBtn = new JButton("Add");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");
        clearBtn = new JButton("Clear");
        refreshBtn = new JButton("Refresh");
        btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn); btns.add(clearBtn); btns.add(refreshBtn);
        form.add(btns);
        add(form, BorderLayout.EAST);

        addBtn.addActionListener(e -> addMedicine());
        updateBtn.addActionListener(e -> updateMedicine());
        deleteBtn.addActionListener(e -> deleteMedicine());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> { loadSuppliers(); loadMedicines(); });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                nameField.setText(model.getValueAt(r, 1).toString());
                companyField.setText(model.getValueAt(r, 2).toString());
                typeField.setText(model.getValueAt(r, 3).toString());
                priceField.setText(model.getValueAt(r, 4).toString());
                qtyField.setText(model.getValueAt(r, 5).toString());
                reorderField.setText(model.getValueAt(r, 6).toString());
                expiryField.setText(model.getValueAt(r, 7).toString());
                supplierCombo.setSelectedItem(model.getValueAt(r, 8).toString());
            }
        });

        loadSuppliers();
        loadMedicines();
    }

    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM suppliers ORDER BY name")) {
            while (rs.next()) supplierCombo.addItem(rs.getString("name"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + ex.getMessage());
        }
    }

    private void loadMedicines() {
        model.setRowCount(0);
        String sql = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, m.quantity_in_stock, " +
                     "m.reorder_level, m.expiry_date, s.name AS supplier " +
                     "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id=s.supplier_id ORDER BY m.medicine_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("medicine_id"),
                    rs.getString("name"),
                    rs.getString("company"),
                    rs.getString("medicine_type"),
                    rs.getBigDecimal("price"),
                    rs.getInt("quantity_in_stock"),
                    rs.getInt("reorder_level"),
                    rs.getDate("expiry_date"),
                    rs.getString("supplier")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + ex.getMessage());
        }
    }

    private int getSupplierId(String name) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT supplier_id FROM suppliers WHERE name=?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private void addMedicine() {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, companyField.getText());
            ps.setString(3, typeField.getText());
            ps.setBigDecimal(4, new java.math.BigDecimal(priceField.getText()));
            ps.setInt(5, Integer.parseInt(qtyField.getText()));
            ps.setInt(6, Integer.parseInt(reorderField.getText()));
            ps.setDate(7, Date.valueOf(expiryField.getText()));
            ps.setInt(8, getSupplierId((String) supplierCombo.getSelectedItem()));
            ps.executeUpdate();
            loadMedicines();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding: " + ex.getMessage());
        }
    }

    private void updateMedicine() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a medicine."); return; }
        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, companyField.getText());
            ps.setString(3, typeField.getText());
            ps.setBigDecimal(4, new java.math.BigDecimal(priceField.getText()));
            ps.setInt(5, Integer.parseInt(qtyField.getText()));
            ps.setInt(6, Integer.parseInt(reorderField.getText()));
            ps.setDate(7, Date.valueOf(expiryField.getText()));
            ps.setInt(8, getSupplierId((String) supplierCombo.getSelectedItem()));
            ps.setInt(9, id);
            ps.executeUpdate();
            loadMedicines();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating: " + ex.getMessage());
        }
    }

    private void deleteMedicine() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a medicine."); return; }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete medicine ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM medicines WHERE medicine_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadMedicines();
            clearForm();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText(""); companyField.setText(""); typeField.setText(""); priceField.setText("");
        qtyField.setText(""); reorderField.setText(""); expiryField.setText("");
        table.clearSelection();
    }
}