import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;

public class LoginPanel extends JPanel {
    private final ParkingLot lot;
    private final JFrame parent;
    private JPasswordField pwdField;

    public LoginPanel(ParkingLot lot, JFrame parent) {
        this.lot = lot;
        this.parent = parent;
        setLayout(new BorderLayout());
        createLoginUI();
    }

    private void createLoginUI() {
        setBackground(UIUtils.DARK_BG);
        JPanel glassPanel = createGlassPanel();
        add(glassPanel, BorderLayout.CENTER);
    }

    private JPanel createGlassPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override 
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(50,50,70,220), 400, 400, new Color(20,20,40,240));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(450, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("PARKING PRO", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Smart Management System v3", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        subtitle.setForeground(new Color(200,200,220));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        // Password
        gbc.gridwidth = 1; gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel label = new JLabel("Admin Password:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);

        gbc.gridx = 1;
        pwdField = new JPasswordField(15);
        UIUtils.styleField(pwdField);
        panel.add(pwdField, gbc);

        // Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton btn = new JButton("ENTER DASHBOARD");
        UIUtils.styleButton(btn, UIUtils.PRIMARY_COLOR);
        btn.addActionListener(e -> login());
        panel.add(btn, gbc);

        return panel;
    }

    private void login() {
        if (UIUtils.ADMIN_PASSWORD.equals(new String(pwdField.getPassword()))) {
            parent.setContentPane(new DashboardPanel(lot));
            parent.revalidate();
            parent.repaint();
            ((DashboardPanel) parent.getContentPane()).startAutoUpdate();
        } else {
            UIUtils.flashError(pwdField);
            JOptionPane.showMessageDialog(this, "Incorrect Password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            pwdField.setText("");
        }
    }
}
