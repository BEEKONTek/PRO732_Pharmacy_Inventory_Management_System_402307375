import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class BillFrame extends JFrame {
    public BillFrame(int saleId) {
        setTitle("Bill - Sale #" + saleId);
        setSize(500, 600);
        setLocationRelativeTo(null);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(area), BorderLayout.CENTER);

        JButton saveBtn = new JButton("Save Bill");
        add(saveBtn, BorderLayout.SOUTH);
        saveBtn.addActionListener(e -> saveBill(area.getText()));

        loadBill(saleId, area);
    }

    private void loadBill(int saleId, JTextArea area) {
        StringBuilder sb = new StringBuilder();
        sb.append("        HealthFirst Pharmacy\n");
        sb.append("        ----------------------\n");

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name " +
                "FROM sales s JOIN users u ON s.user_id=u.user_id WHERE s.sale_id=?");
            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sb.append("Sale ID: ").append(rs.getInt("sale_id")).append("\n");
                sb.append("Date: ").append(rs.getTimestamp("sale_date")).append("\n");
                sb.append("Cashier: ").append(rs.getString("full_name")).append("\n");
                sb.append("----------------------------------------\n");
            }

            PreparedStatement ps2 = con.prepareStatement(
                "SELECT m.name, si.quantity_sold, si.price_at_sale, " +
                "(si.quantity_sold*si.price_at_sale) AS subtotal " +
                "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id " +
                "WHERE si.sale_id=?");
            ps2.setInt(1, saleId);
            ResultSet rs2 = ps2.executeQuery();
            sb.append(String.format("%-20s %5s %10s %10s\n", "Item", "Qty", "Price", "Subtotal"));
            while (rs2.next()) {
                sb.append(String.format("%-20s %5d %10.2f %10.2f\n",
                    rs2.getString("name"),
                    rs2.getInt("quantity_sold"),
                    rs2.getBigDecimal("price_at_sale").doubleValue(),
                    rs2.getBigDecimal("subtotal").doubleValue()));
            }

            ps = con.prepareStatement("SELECT total_amount FROM sales WHERE sale_id=?");
            ps.setInt(1, saleId);
            rs = ps.executeQuery();
            if (rs.next()) {
                sb.append("----------------------------------------\n");
                sb.append(String.format("TOTAL: %.2f\n", rs.getBigDecimal("total_amount").doubleValue()));
            }
        } catch (SQLException ex) {
            sb.append("Error loading bill: ").append(ex.getMessage());
        }
        area.setText(sb.toString());
    }

    private void saveBill(String text) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("bill.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile())) {
                pw.print(text);
                JOptionPane.showMessageDialog(this, "Bill saved.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
            }
        }
    }
}