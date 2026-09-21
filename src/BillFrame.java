import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class BillFrame extends JFrame {
    public BillFrame(int saleId, String cashierName) {
        ThemeUtil.install();
        setTitle("Receipt - Sale #" + saleId);
        setSize(480, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeUtil.BG);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(ThemeUtil.FONT_MONO);
        area.setBackground(Color.WHITE);
        area.setForeground(ThemeUtil.TEXT);
        area.setMargin(new Insets(16, 18, 16, 18));

        JScrollPane sp = ThemeUtil.scroll(area);
        sp.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(ThemeUtil.BG);
        JButton saveBtn  = ThemeUtil.primaryButton("Save receipt");
        JButton closeBtn = ThemeUtil.secondaryButton("Close");
        saveBtn.addActionListener(e -> save(area.getText(), saleId));
        closeBtn.addActionListener(e -> dispose());
        actions.add(closeBtn);
        actions.add(saveBtn);
        add(actions, BorderLayout.SOUTH);

        area.setText(buildBill(saleId, cashierName));
        area.setCaretPosition(0);
    }

    private String buildBill(int saleId, String cashierName) {
        StringBuilder sb = new StringBuilder();
        sb.append("         HealthFirst Pharmacy\n");
        sb.append("       ----------------------\n\n");
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name "
                  + "FROM sales s JOIN users u ON s.user_id=u.user_id WHERE s.sale_id=?")) {
                ps.setInt(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sb.append(String.format("Sale #     : %d%n", rs.getInt("sale_id")));
                        sb.append(String.format("Date       : %s%n", rs.getTimestamp("sale_date")));
                        sb.append(String.format("Cashier    : %s%n", rs.getString("full_name")));
                    } else {
                        sb.append(String.format("Cashier    : %s%n", cashierName));
                    }
                }
            }
            sb.append("------------------------------------------\n");
            sb.append(String.format("%-18s %4s %9s %10s%n", "Item", "Qty", "Price", "Subtotal"));
            sb.append("------------------------------------------\n");
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT m.name, si.quantity_sold, si.price_at_sale, "
                  + "(si.quantity_sold*si.price_at_sale) AS subtotal "
                  + "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id "
                  + "WHERE si.sale_id=?")) {
                ps2.setInt(1, saleId);
                try (ResultSet rs = ps2.executeQuery()) {
                    while (rs.next()) {
                        sb.append(String.format("%-18s %4d %9.2f %10.2f%n",
                                trim(rs.getString("name"), 18),
                                rs.getInt("quantity_sold"),
                                rs.getBigDecimal("price_at_sale").doubleValue(),
                                rs.getBigDecimal("subtotal").doubleValue()));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT total_amount FROM sales WHERE sale_id=?")) {
                ps.setInt(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sb.append("------------------------------------------\n");
                        sb.append(String.format("TOTAL: R%.2f%n", rs.getBigDecimal("total_amount").doubleValue()));
                    }
                }
            }
            sb.append("\n     Thank you. Get well soon!\n");
        } catch (SQLException ex) {
            sb.append("Error loading bill: ").append(ex.getMessage()).append('\n');
        }
        return sb.toString();
    }

    private static String trim(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "\u2026";
    }

    private void save(String text, int saleId) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("receipt_" + saleId + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(fc.getSelectedFile())) {
                pw.print(text);
                ThemeUtil.info(this, "Receipt saved to " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                ThemeUtil.error(this, "Could not save: " + ex.getMessage());
            }
        }
    }
}
