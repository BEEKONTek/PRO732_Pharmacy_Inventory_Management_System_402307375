import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public final class ThemeUtil {
    public static final Color PRIMARY       = new Color(0x0F, 0x76, 0x6E);
    public static final Color PRIMARY_DARK  = new Color(0x0A, 0x5A, 0x54);
    public static final Color PRIMARY_LIGHT = new Color(0xD1, 0xEA, 0xE6);
    public static final Color ACCENT        = new Color(0xF5, 0x9E, 0x0B);
    public static final Color DANGER        = new Color(0xC0, 0x39, 0x2B);
    public static final Color SUCCESS       = new Color(0x2E, 0x7D, 0x32);
    public static final Color BG            = new Color(0xF5, 0xF7, 0xFA);
    public static final Color CARD          = Color.WHITE;
    public static final Color TEXT          = new Color(0x21, 0x21, 0x21);
    public static final Color TEXT_MUTED    = new Color(0x75, 0x75, 0x75);
    public static final Color BORDER        = new Color(0xE0, 0xE0, 0xE0);

    public static final String FONT_FAMILY = pickFont();

    public static final Font FONT_BASE  = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_H1    = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font FONT_H2    = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BTN   = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_MONO  = new Font("JetBrains Mono", Font.PLAIN, 13);

    private ThemeUtil() {}

    private static String pickFont() {
        String[] prefs = { "Inter", "Segoe UI", "Ubuntu", "Cantarell", "DejaVu Sans", "SansSerif" };
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String p : prefs) {
            for (String a : available) if (a.equalsIgnoreCase(p)) return p;
        }
        return "SansSerif";
    }

    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }
        UIManager.put("control", BG);
        UIManager.put("info", CARD);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusBlueGrey", BG);
        UIManager.put("nimbusLightBackground", CARD);
        UIManager.put("nimbusSelectionBackground", PRIMARY);
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("text", TEXT);
        UIManager.put("Table.alternateRowColor", new Color(0xF0, 0xF4, 0xF8));
        UIManager.put("Table.showGrid", Boolean.FALSE);
        UIManager.put("Table.rowHeight", 26);
        UIManager.put("TableHeader.height", 30);
        UIManager.put("TabbedPane.selected", CARD);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("OptionPane.messageFont", FONT_BASE);
        UIManager.put("OptionPane.buttonFont", FONT_BTN);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(PRIMARY_DARK); }
            public void mouseExited (java.awt.event.MouseEvent e) { b.setBackground(PRIMARY); }
        });
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, CARD, PRIMARY_DARK);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_DARK, 1, true),
                new EmptyBorder(8, 16, 8, 16)));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT, Color.WHITE);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BTN);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
    }

    public static JTextField textField(int cols) {
        JTextField f = new JTextField(cols);
        styleField(f);
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        styleField(f);
        return f;
    }

    private static void styleField(JTextField f) {
        f.setFont(FONT_BASE);
        f.setForeground(TEXT);
        f.setCaretColor(PRIMARY_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(9, 11, 9, 11)));
    }

    public static JLabel h1(String t) { JLabel l = new JLabel(t); l.setFont(FONT_H1);    l.setForeground(PRIMARY_DARK); return l; }
    public static JLabel h2(String t) { JLabel l = new JLabel(t); l.setFont(FONT_H2);    l.setForeground(PRIMARY_DARK); return l; }
    public static JLabel label(String t){ JLabel l = new JLabel(t); l.setFont(FONT_LABEL); l.setForeground(TEXT);        return l; }
    public static JLabel muted(String t){ JLabel l = new JLabel(t); l.setFont(FONT_SMALL); l.setForeground(TEXT_MUTED);  return l; }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        return p;
    }

    public static JPanel headerBar(String title, String subtitle, JComponent right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(FONT_H2);
        t.setForeground(Color.WHITE);
        left.add(t);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel s = new JLabel(subtitle);
            s.setFont(FONT_SMALL);
            s.setForeground(new Color(0xDD, 0xEE, 0xEE));
            left.add(s);
        }
        bar.add(left, BorderLayout.WEST);
        if (right != null) bar.add(right, BorderLayout.EAST);
        return bar;
    }

    public static JLabel roleChip(String text) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setOpaque(true);
        l.setBackground(new Color(255, 255, 255, 40));
        l.setForeground(Color.WHITE);
        l.setFont(FONT_LABEL);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BASE);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);

        JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL);
        h.setBackground(PRIMARY_LIGHT);
        h.setForeground(PRIMARY_DARK);
        h.setReorderingAllowed(false);
        h.setBorder(BorderFactory.createEmptyBorder());
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(CARD);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        return sp;
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
