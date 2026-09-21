import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.prefs.Preferences;

public class LoginFrame extends JFrame {
    private final JTextField        userField;
    private final JPasswordField    passField;
    private final JCheckBox         showPass;
    private final JCheckBox         rememberMe;
    private final JLabel            statusLabel;
    private final JButton           loginBtn;
    private final Preferences       prefs = Preferences.userNodeForPackage(LoginFrame.class);

    public LoginFrame() {
        ThemeUtil.install();

        setTitle("HealthFirst PIMS - Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2, 0, 0));

        add(new BrandPanel());

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(ThemeUtil.BG);
        add(right);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(360, 420));

        JLabel welcome = ThemeUtil.h1("Welcome back");
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(welcome);
        form.add(Box.createVerticalStrut(4));

        JLabel sub = ThemeUtil.muted("Sign in to HealthFirst Pharmacy Inventory");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);

        form.add(Box.createVerticalStrut(24));

        JLabel uLbl = ThemeUtil.label("Username");
        uLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(uLbl);
        form.add(Box.createVerticalStrut(6));

        userField = ThemeUtil.textField(24);
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(userField);
        form.add(Box.createVerticalStrut(14));

        JLabel pLbl = ThemeUtil.label("Password");
        pLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(pLbl);
        form.add(Box.createVerticalStrut(6));

        passField = ThemeUtil.passwordField(24);
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(passField);
        form.add(Box.createVerticalStrut(8));

        JPanel opts = new JPanel(new BorderLayout());
        opts.setOpaque(false);
        opts.setAlignmentX(Component.LEFT_ALIGNMENT);
        opts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        showPass   = new JCheckBox("Show password");
        rememberMe = new JCheckBox("Remember me");
        for (JCheckBox c : new JCheckBox[]{showPass, rememberMe}) {
            c.setOpaque(false);
            c.setFont(ThemeUtil.FONT_SMALL);
            c.setForeground(ThemeUtil.TEXT_MUTED);
            c.setFocusPainted(false);
        }
        opts.add(showPass, BorderLayout.WEST);
        opts.add(rememberMe, BorderLayout.EAST);
        form.add(opts);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeUtil.FONT_SMALL);
        statusLabel.setForeground(ThemeUtil.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(6));
        form.add(statusLabel);

        form.add(Box.createVerticalStrut(8));
        loginBtn = ThemeUtil.primaryButton("Sign in");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(loginBtn);

        form.add(Box.createVerticalStrut(20));
        form.add(buildDemoPanel());

        right.add(form);

        showPass.addActionListener(e ->
                passField.setEchoChar(showPass.isSelected() ? (char) 0 : '\u2022'));
        loginBtn.addActionListener(e -> doLogin());
        userField.addActionListener(e -> passField.requestFocusInWindow());
        passField.addActionListener(e -> doLogin());

        String saved = prefs.get("lastUser", "");
        if (!saved.isEmpty()) {
            userField.setText(saved);
            rememberMe.setSelected(true);
            passField.requestFocusInWindow();
        } else {
            userField.requestFocusInWindow();
        }

        getRootPane().setDefaultButton(loginBtn);
    }

    private JPanel buildDemoPanel() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = ThemeUtil.muted("Demo accounts (click to fill)");
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(t);
        wrap.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton asAdmin   = ThemeUtil.secondaryButton("Admin");
        JButton asCashier = ThemeUtil.secondaryButton("Cashier");
        asAdmin.addActionListener(e -> { userField.setText("admin");   passField.setText("admin123");  passField.requestFocusInWindow(); });
        asCashier.addActionListener(e -> { userField.setText("cashier"); passField.setText("cash123"); passField.requestFocusInWindow(); });
        row.add(asAdmin);
        row.add(asCashier);
        wrap.add(row);

        wrap.add(Box.createVerticalStrut(10));
        JLabel hint = ThemeUtil.muted("Admin \u2022 Manage inventory, suppliers, users, reports"
                          + "      Cashier \u2022 Point of sale + billing");
        hint.setFont(ThemeUtil.FONT_SMALL);
        wrap.add(hint);

        return wrap;
    }

    private void doLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        statusLabel.setText(" ");

        if (u.isEmpty() || p.isEmpty()) {
            statusLabel.setForeground(ThemeUtil.DANGER);
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");

        new SwingWorker<Object[], Void>() {
            protected Object[] doInBackground() throws Exception {
                try (Connection c = DBConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "SELECT user_id, role, full_name FROM users WHERE username=? AND password=?")) {
                    ps.setString(1, u);
                    ps.setString(2, p);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return new Object[]{ rs.getInt("user_id"), rs.getString("role"), rs.getString("full_name") };
                        }
                    }
                }
                return null;
            }
            protected void done() {
                try {
                    Object[] row = get();
                    if (row == null) {
                        statusLabel.setForeground(ThemeUtil.DANGER);
                        statusLabel.setText("Invalid username or password.");
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Sign in");
                        passField.selectAll();
                        passField.requestFocusInWindow();
                        return;
                    }
                    if (rememberMe.isSelected()) prefs.put("lastUser", u);
                    else                         prefs.remove("lastUser");

                    int uid = (int) row[0];
                    String role = (String) row[1];
                    String full = (String) row[2];

                    statusLabel.setForeground(ThemeUtil.SUCCESS);
                    statusLabel.setText("Signed in. Redirecting...");

                    dispose();
                    if ("Admin".equalsIgnoreCase(role)) {
                        new AdminDashboard(uid, full).setVisible(true);
                    } else {
                        new CashierDashboard(uid, full).setVisible(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ThemeUtil.DANGER);
                    statusLabel.setText("Login error: " + ex.getMessage());
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Sign in");
                }
            }
        }.execute();
    }

    private static class BrandPanel extends JPanel {
        BrandPanel() {
            setLayout(new GridBagLayout());
            setPreferredSize(new Dimension(450, 560));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JLabel name = new JLabel("HealthFirst");
            name.setFont(new Font(ThemeUtil.FONT_FAMILY, Font.BOLD, 34));
            name.setForeground(Color.WHITE);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tag = new JLabel("Pharmacy Inventory Management");
            tag.setFont(new Font(ThemeUtil.FONT_FAMILY, Font.PLAIN, 15));
            tag.setForeground(new Color(0xDD, 0xEE, 0xEE));
            tag.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(name);
            content.add(Box.createVerticalStrut(6));
            content.add(tag);
            content.add(Box.createVerticalStrut(28));
            content.add(bullet("Stock control with expiry tracking"));
            content.add(Box.createVerticalStrut(6));
            content.add(bullet("Fast point of sale + instant billing"));
            content.add(Box.createVerticalStrut(6));
            content.add(bullet("Sales, low-stock and expiry reports"));
            content.add(Box.createVerticalStrut(6));
            content.add(bullet("Role-based access for Admins and Cashiers"));

            add(content);
        }

        private JLabel bullet(String text) {
            JLabel l = new JLabel("\u2713  " + text);
            l.setFont(new Font(ThemeUtil.FONT_FAMILY, Font.PLAIN, 14));
            l.setForeground(new Color(0xEC, 0xF7, 0xF6));
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setPaint(new GradientPaint(0, 0, ThemeUtil.PRIMARY_DARK, 0, getHeight(), ThemeUtil.PRIMARY));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(-90, getHeight() - 190, 300, 300);
            g2.fillOval(getWidth() - 150, -70, 220, 220);

            int cx = 70, cy = 70, r = 30;
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(ThemeUtil.PRIMARY);
            int thick = 8;
            g2.fillRoundRect(cx - thick / 2, cy - r + 12, thick, r * 2 - 24, thick, thick);
            g2.fillRoundRect(cx - r + 12, cy - thick / 2, r * 2 - 24, thick, thick, thick);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
