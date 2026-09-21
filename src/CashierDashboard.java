import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class CashierDashboard extends JFrame {
    private final int userId;
    private final String cashierName;

    private JTable medTable, cartTable;
    private DefaultTableModel medModel, cartModel;
    private JTextField searchField;
    private JLabel totalLabel;
    private double total = 0.0;

    public CashierDashboard(int userId, String fullName) {
        this.userId = userId;
        this.cashierName = fullName;
        ThemeUtil.install();

        setTitle("HealthFirst PIMS - Point of Sale (" + fullName + ")");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1080, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(ThemeUtil.roleChip("CASHIER"));
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
        add(ThemeUtil.headerBar("Point of Sale",
                "Search, add to cart and checkout", right), BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(ThemeUtil.BG);
        searchBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchLeft.setOpaque(false);
        searchLeft.add(ThemeUtil.label("Search medicine:"));
        searchField = ThemeUtil.textField(28);
        searchLeft.add(searchField);
        JButton searchBtn = ThemeUtil.secondaryButton("Search");
        JButton stockBtn  = ThemeUtil.accentButton("Quick stock check");
        searchLeft.add(searchBtn);
        searchLeft.add(stockBtn);
        searchBar.add(searchLeft, BorderLayout.WEST);
        add(searchBar, BorderLayout.NORTH);

        medModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Expiry"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        medTable = new JTable(medModel);
        ThemeUtil.styleTable(medTable);
        JScrollPane medScroll = ThemeUtil.scroll(medTable);
        medScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeUtil.BORDER), "Available medicines"));

        cartModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        ThemeUtil.styleTable(cartTable);
        JScrollPane cartScroll = ThemeUtil.scroll(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeUtil.BORDER), "Current cart"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, medScroll, cartScroll);
        split.setDividerLocation(720);
        split.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        split.setBackground(ThemeUtil.BG);
        add(split, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(ThemeUtil.BG);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 14, 12, 14));

        totalLabel = new JLabel("Total: R0.00");
        totalLabel.setFont(new Font(ThemeUtil.FONT_FAMILY, Font.BOLD, 20));
        totalLabel.setForeground(ThemeUtil.PRIMARY_DARK);
        footer.add(totalLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton addBtn      = ThemeUtil.primaryButton("Add to cart");
        JButton clearBtn    = ThemeUtil.secondaryButton("Clear cart");
        JButton checkoutBtn = ThemeUtil.accentButton("Checkout");
        actions.add(addBtn);
        actions.add(clearBtn);
        actions.add(checkoutBtn);
        footer.add(actions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadMedicines(searchField.getText().trim()));
        stockBtn.addActionListener(e -> stockCheck());
        addBtn.addActionListener(e -> addToCart());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());
        searchField.addActionListener(e -> loadMedicines(searchField.getText().trim()));
        medTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) addToCart();
            }
        });

        loadMedicines("");
    }

    private void loadMedicines(String keyword) {
        medModel.setRowCount(0);
        String sql = "SELECT medicine_id, name, company, medicine_type, price, quantity_in_stock, expiry_date "
                   + "FROM medicines WHERE name LIKE ? OR company LIKE ? ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException ex) {
            ThemeUtil.error(this, "Error loading medicines: " + ex.getMessage());
        }
    }

    private void stockCheck() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { ThemeUtil.warn(this, "Type a medicine name to check."); return; }
        StringBuilder sb = new StringBuilder();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT name, price, quantity_in_stock, expiry_date FROM medicines WHERE name LIKE ?")) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString("name"))
                      .append("  |  Price: R").append(rs.getBigDecimal("price"))
                      .append("  |  Stock: ").append(rs.getInt("quantity_in_stock"))
                      .append("  |  Expiry: ").append(rs.getDate("expiry_date"))
                      .append("\n");
                }
            }
            if (sb.length() == 0) sb.append("No medicine matched \"").append(keyword).append("\".");
            ThemeUtil.info(this, sb.toString());
        } catch (SQLException ex) {
            ThemeUtil.error(this, "Error: " + ex.getMessage());
        }
    }

    private void addToCart() {
        int row = medTable.getSelectedRow();
        if (row == -1) { ThemeUtil.warn(this, "Select a medicine first."); return; }
        int medId   = (int) medModel.getValueAt(row, 0);
        String name = (String) medModel.getValueAt(row, 1);
        double price = ((BigDecimal) medModel.getValueAt(row, 4)).doubleValue();
        int stock   = (int) medModel.getValueAt(row, 5);

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == medId) {
                int qty = (int) cartModel.getValueAt(i, 3) + 1;
                if (qty > stock) { ThemeUtil.warn(this, "Only " + stock + " in stock."); return; }
                cartModel.setValueAt(qty, i, 3);
                cartModel.setValueAt(price * qty, i, 4);
                updateTotal();
                return;
            }
        }
        if (stock < 1) { ThemeUtil.warn(this, "Out of stock."); return; }
        cartModel.addRow(new Object[]{ medId, name, price, 1, price });
        updateTotal();
    }

    private void updateTotal() {
        total = 0.0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += (double) cartModel.getValueAt(i, 4);
        }
        totalLabel.setText(String.format("Total: R%.2f", total));
    }

    private void clearCart() {
        if (cartModel.getRowCount() == 0) return;
        if (ThemeUtil.confirm(this, "Clear the current cart?")) {
            cartModel.setRowCount(0);
            total = 0;
            totalLabel.setText("Total: R0.00");
        }
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) { ThemeUtil.warn(this, "Cart is empty."); return; }
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            int saleId = 0;
            try (PreparedStatement psSale = con.prepareStatement(
                    "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                psSale.setDouble(1, total);
                psSale.setInt(2, userId);
                psSale.executeUpdate();
                try (ResultSet k = psSale.getGeneratedKeys()) { if (k.next()) saleId = k.getInt(1); }
            }

            try (PreparedStatement item = con.prepareStatement(
                    "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)");
                 PreparedStatement stock = con.prepareStatement(
                    "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? "
                  + "WHERE medicine_id = ? AND quantity_in_stock >= ?")) {

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int medId = (int) cartModel.getValueAt(i, 0);
                    int qty   = (int) cartModel.getValueAt(i, 3);
                    double pr = (double) cartModel.getValueAt(i, 2);

                    item.setInt(1, saleId);
                    item.setInt(2, medId);
                    item.setInt(3, qty);
                    item.setDouble(4, pr);
                    item.executeUpdate();

                    stock.setInt(1, qty);
                    stock.setInt(2, medId);
                    stock.setInt(3, qty);
                    if (stock.executeUpdate() == 0)
                        throw new SQLException("Insufficient stock for medicine ID " + medId);
                }
            }
            con.commit();
            new BillFrame(saleId, cashierName).setVisible(true);
            clearCart();
            loadMedicines("");
        } catch (SQLException ex) {
            ThemeUtil.error(this, "Checkout failed: " + ex.getMessage());
        }
    }
}
