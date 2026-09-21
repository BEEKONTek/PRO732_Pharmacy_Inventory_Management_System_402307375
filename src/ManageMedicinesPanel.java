import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class ManageMedicinesPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField nameField, companyField, typeField, priceField, qtyField, reorderField, expiryField;
    private final JComboBox<String> supplierCombo;

    public ManageMedicinesPanel() {
        setBackground(ThemeUtil.BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new String[]{"ID","Name","Company","Type","Price","Qty","Reorder","Expiry","Supplier"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeUtil.styleTable(table);
        add(ThemeUtil.scroll(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.setBackground(ThemeUtil.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeUtil.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        form.setPreferredSize(new Dimension(340, 0));

        form.add(ThemeUtil.label("Name:"));
        nameField = ThemeUtil.textField(14); form.add(nameField);
        form.add(ThemeUtil.label("Company:"));
        companyField = ThemeUtil.textField(14); form.add(companyField);
        form.add(ThemeUtil.label("Type:"));
        typeField = ThemeUtil.textField(14); form.add(typeField);
        form.add(ThemeUtil.label("Price:"));
        priceField = ThemeUtil.textField(14); form.add(priceField);
        form.add(ThemeUtil.label("Quantity:"));
        qtyField = ThemeUtil.textField(14); form.add(qtyField);
        form.add(ThemeUtil.label("Reorder level:"));
        reorderField = ThemeUtil.textField(14); form.add(reorderField);
        form.add(ThemeUtil.label("Expiry (YYYY-MM-DD):"));
        expiryField = ThemeUtil.textField(14); form.add(expiryField);
        form.add(ThemeUtil.label("Supplier:"));
        supplierCombo = new JComboBox<>(); form.add(supplierCombo);

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

        addBtn.addActionListener(e -> addMedicine());
        updateBtn.addActionListener(e -> updateMedicine());
        deleteBtn.addActionListener(e -> deleteMedicine());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> { loadSuppliers(); loadMedicines(); });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                nameField.setText(str(model.getValueAt(r, 1)));
                companyField.setText(str(model.getValueAt(r, 2)));
                typeField.setText(str(model.getValueAt(r, 3)));
                priceField.setText(str(model.getValueAt(r, 4)));
                qtyField.setText(str(model.getValueAt(r, 5)));
                reorderField.setText(str(model.getValueAt(r, 6)));
                expiryField.setText(str(model.getValueAt(r, 7)));
                supplierCombo.setSelectedItem(str(model.getValueAt(r, 8)));
            }
        });

        loadSuppliers();
        loadMedicines();
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }

    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM suppliers ORDER BY name")) {
            while (rs.next()) supplierCombo.addItem(rs.getString("name"));
        } catch (SQLException ex) { ThemeUtil.error(this, "Error loading suppliers: " + ex.getMessage()); }
    }

    private void loadMedicines() {
        model.setRowCount(0);
        String sql = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, m.quantity_in_stock, "
                   + "m.reorder_level, m.expiry_date, s.name AS supplier "
                   + "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id=s.supplier_id ORDER BY m.medicine_id";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("medicine_id"), rs.getString("name"), rs.getString("company"),
                        rs.getString("medicine_type"), rs.getBigDecimal("price"),
                        rs.getInt("quantity_in_stock"), rs.getInt("reorder_level"),
                        rs.getDate("expiry_date"), rs.getString("supplier")
                });
            }
        } catch (SQLException ex) { ThemeUtil.error(this, "Error loading medicines: " + ex.getMessage()); }
    }

    private int getSupplierId(String name) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT supplier_id FROM suppliers WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        return 0;
    }

    private void addMedicine() {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, companyField.getText());
            ps.setString(3, typeField.getText());
            ps.setBigDecimal(4, new BigDecimal(priceField.getText()));
            ps.setInt(5, Integer.parseInt(qtyField.getText()));
            ps.setInt(6, Integer.parseInt(reorderField.getText()));
            ps.setDate(7, Date.valueOf(expiryField.getText()));
            ps.setInt(8, getSupplierId((String) supplierCombo.getSelectedItem()));
            ps.executeUpdate();
            loadMedicines(); clearForm();
        } catch (Exception ex) { ThemeUtil.error(this, "Add failed: " + ex.getMessage()); }
    }

    private void updateMedicine() {
        int r = table.getSelectedRow();
        if (r == -1) { ThemeUtil.warn(this, "Select a medicine to update."); return; }
        int id = (int) model.getValueAt(r, 0);
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, companyField.getText());
            ps.setString(3, typeField.getText());
            ps.setBigDecimal(4, new BigDecimal(priceField.getText()));
            ps.setInt(5, Integer.parseInt(qtyField.getText()));
            ps.setInt(6, Integer.parseInt(reorderField.getText()));
            ps.setDate(7, Date.valueOf(expiryField.getText()));
            ps.setInt(8, getSupplierId((String) supplierCombo.getSelectedItem()));
            ps.setInt(9, id);
            ps.executeUpdate();
            loadMedicines(); clearForm();
        } catch (Exception ex) { ThemeUtil.error(this, "Update failed: " + ex.getMessage()); }
    }

    private void deleteMedicine() {
        int r = table.getSelectedRow();
        if (r == -1) { ThemeUtil.warn(this, "Select a medicine to delete."); return; }
        int id = (int) model.getValueAt(r, 0);
        if (!ThemeUtil.confirm(this, "Delete medicine ID " + id + "?")) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM medicines WHERE medicine_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadMedicines(); clearForm();
        } catch (SQLException ex) { ThemeUtil.error(this, "Delete failed: " + ex.getMessage()); }
    }

    private void clearForm() {
        nameField.setText(""); companyField.setText(""); typeField.setText("");
        priceField.setText(""); qtyField.setText(""); reorderField.setText(""); expiryField.setText("");
        table.clearSelection();
    }
}
