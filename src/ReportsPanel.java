import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;

    public ReportsPanel() {
        setBackground(ThemeUtil.BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        JButton salesBtn  = ThemeUtil.primaryButton("Sales report");
        JButton itemBtn   = ThemeUtil.secondaryButton("Item-wise report");
        JButton lowBtn    = ThemeUtil.secondaryButton("Low stock report");
        JButton expiryBtn = ThemeUtil.secondaryButton("Expiry report");
        btns.add(salesBtn); btns.add(itemBtn); btns.add(lowBtn); btns.add(expiryBtn);
        add(btns, BorderLayout.NORTH);

        model = new DefaultTableModel();
        table = new JTable(model);
        ThemeUtil.styleTable(table);
        add(ThemeUtil.scroll(table), BorderLayout.CENTER);

        salesBtn.addActionListener(e -> salesReport());
        itemBtn.addActionListener(e -> itemWiseReport());
        lowBtn.addActionListener(e -> lowStockReport());
        expiryBtn.addActionListener(e -> expiryReport());
    }

    private void setModel(String[] cols, List<Object[]> rows) {
        model.setDataVector(rows.toArray(new Object[0][]), cols);
    }

    private void salesReport() {
        String sql = "SELECT s.sale_id, s.sale_date, u.full_name, s.total_amount "
                   + "FROM sales s JOIN users u ON s.user_id=u.user_id ORDER BY s.sale_date DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) rows.add(new Object[]{
                    rs.getInt("sale_id"), rs.getTimestamp("sale_date"),
                    rs.getString("full_name"), rs.getBigDecimal("total_amount")
            });
            setModel(new String[]{"Sale ID","Date","Cashier","Total"}, rows);
        } catch (SQLException ex) { ThemeUtil.error(this, "Error: " + ex.getMessage()); }
    }

    private void itemWiseReport() {
        String sql = "SELECT m.name, SUM(si.quantity_sold) AS total_qty, "
                   + "SUM(si.quantity_sold*si.price_at_sale) AS total_sales "
                   + "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id "
                   + "GROUP BY m.medicine_id, m.name ORDER BY total_sales DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) rows.add(new Object[]{
                    rs.getString("name"), rs.getInt("total_qty"), rs.getBigDecimal("total_sales")
            });
            setModel(new String[]{"Medicine","Total Qty Sold","Total Sales"}, rows);
        } catch (SQLException ex) { ThemeUtil.error(this, "Error: " + ex.getMessage()); }
    }

    private void lowStockReport() {
        String sql = "SELECT medicine_id, name, quantity_in_stock, reorder_level "
                   + "FROM medicines WHERE quantity_in_stock <= reorder_level ORDER BY quantity_in_stock";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) rows.add(new Object[]{
                    rs.getInt("medicine_id"), rs.getString("name"),
                    rs.getInt("quantity_in_stock"), rs.getInt("reorder_level")
            });
            setModel(new String[]{"ID","Medicine","Stock","Reorder Level"}, rows);
        } catch (SQLException ex) { ThemeUtil.error(this, "Error: " + ex.getMessage()); }
    }

    private void expiryReport() {
        String sql = "SELECT medicine_id, name, expiry_date, quantity_in_stock "
                   + "FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH) "
                   + "ORDER BY expiry_date";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) rows.add(new Object[]{
                    rs.getInt("medicine_id"), rs.getString("name"),
                    rs.getDate("expiry_date"), rs.getInt("quantity_in_stock")
            });
            setModel(new String[]{"ID","Medicine","Expiry Date","Stock"}, rows);
        } catch (SQLException ex) { ThemeUtil.error(this, "Error: " + ex.getMessage()); }
    }
}
