import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CashierDashboard extends JFrame {
    private int userId;
    private JTable medTable, cartTable;
    private DefaultTableModel medModel, cartModel;
    private JTextField searchField;
    private JLabel totalLabel;
    private double total = 0.0;

    public CashierDashboard(int userId, String fullName) {
        this.userId = userId;
        setTitle("HealthFirst PIMS - Cashier POS (" + fullName + ")");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search Medicine:"));
        searchField = new JTextField(20);
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        top.add(searchBtn);
        JButton stockBtn = new JButton("Stock Check");
        top.add(stockBtn);
        JButton logout = new JButton("Logout");
        top.add(logout);
        add(top, BorderLayout.NORTH);

        medModel = new DefaultTableModel(new String[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Expiry"}, 0);
        medTable = new JTable(medModel);
        JScrollPane medScroll = new JScrollPane(medTable);
        medScroll.setBorder(BorderFactory.createTitledBorder("Medicines"));

        cartModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0);
        cartTable = new JTable(cartModel);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder("Cart"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, medScroll, cartScroll);
        split.setDividerLocation(600);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: 0.00");
        bottom.add(totalLabel);
        JButton addBtn = new JButton("Add to Cart");
        bottom.add(addBtn);
        JButton clearBtn = new JButton("Clear Cart");
        bottom.add(clearBtn);
        JButton checkoutBtn = new JButton("Checkout");
        bottom.add(checkoutBtn);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadMedicines(searchField.getText().trim()));
        stockBtn.addActionListener(e -> stockCheck());
        addBtn.addActionListener(e -> addToCart());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        loadMedicines("");
    }

    private void loadMedicines(String keyword) {
        medModel.setRowCount(0);
        String sql = "SELECT medicine_id, name, company, medicine_type, price, quantity_in_stock, expiry_date " +
                     "FROM medicines WHERE name LIKE ? OR company LIKE ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                medModel.addRow(new Object[]{
                    rs.getInt("medicine_id"),
                    rs.getString("name"),
                    rs.getString("company"),
                    rs.getString("medicine_type"),
                    rs.getBigDecimal("price"),
                    rs.getInt("quantity_in_stock"),
                    rs.getDate("expiry_date")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + ex.getMessage());
        }
    }

    private void stockCheck() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter medicine name to check.");
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT name, price, quantity_in_stock, expiry_date FROM medicines WHERE name LIKE ?")) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("name"))
                  .append(" | Price: ").append(rs.getBigDecimal("price"))
                  .append(" | Stock: ").append(rs.getInt("quantity_in_stock"))
                  .append(" | Expiry: ").append(rs.getDate("expiry_date"))
                  .append("\n");
            }
            if (sb.length() == 0) sb.append("No medicine found.");
            JOptionPane.showMessageDialog(this, sb.toString(), "Stock Check", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void addToCart() {
        int row = medTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a medicine first.");
            return;
        }
        int medId = (int) medModel.getValueAt(row, 0);
        String name = (String) medModel.getValueAt(row, 1);
        double price = ((java.math.BigDecimal) medModel.getValueAt(row, 4)).doubleValue();
        int stock = (int) medModel.getValueAt(row, 5);

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == medId) {
                int qty = (int) cartModel.getValueAt(i, 3) + 1;
                if (qty > stock) {
                    JOptionPane.showMessageDialog(this, "Not enough stock. Available: " + stock);
                    return;
                }
                cartModel.setValueAt(qty, i, 3);
                cartModel.setValueAt(price * qty, i, 4);
                updateTotal();
                return;
            }
        }
        if (stock < 1) {
            JOptionPane.showMessageDialog(this, "Out of stock.");
            return;
        }
        cartModel.addRow(new Object[]{medId, name, price, 1, price});
        updateTotal();
    }

    private void updateTotal() {
        total = 0.0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += (double) cartModel.getValueAt(i, 4);
        }
        totalLabel.setText(String.format("Total: %.2f", total));
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        total = 0.0;
        totalLabel.setText("Total: 0.00");
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            PreparedStatement psSale = con.prepareStatement(
                "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            psSale.setDouble(1, total);
            psSale.setInt(2, userId);
            psSale.executeUpdate();

            ResultSet rsKeys = psSale.getGeneratedKeys();
            int saleId = 0;
            if (rsKeys.next()) saleId = rsKeys.getInt(1);

            PreparedStatement psItem = con.prepareStatement(
                "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)");
            PreparedStatement psStock = con.prepareStatement(
                "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ? AND quantity_in_stock >= ?");

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int medId = (int) cartModel.getValueAt(i, 0);
                int qty = (int) cartModel.getValueAt(i, 3);
                double price = (double) cartModel.getValueAt(i, 2);

                psItem.setInt(1, saleId);
                psItem.setInt(2, medId);
                psItem.setInt(3, qty);
                psItem.setDouble(4, price);
                psItem.executeUpdate();

                psStock.setInt(1, qty);
                psStock.setInt(2, medId);
                psStock.setInt(3, qty);
                if (psStock.executeUpdate() == 0) {
                    throw new SQLException("Insufficient stock for medicine ID " + medId);
                }
            }
            con.commit();
            JOptionPane.showMessageDialog(this, "Sale completed. Sale ID: " + saleId);
            new BillFrame(saleId).setVisible(true);
            clearCart();
            loadMedicines("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        }
    }
}