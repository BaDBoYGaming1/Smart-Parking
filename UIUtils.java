import javax.swing.*;
import java.awt.*;

public class UIUtils {
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String[] VEHICLE_TYPES = {"CAR", "BIKE", "TRUCK"};
    public static final Color PRIMARY_COLOR = new Color(0, 123, 255);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color DARK_BG = new Color(30, 30, 46);

    public static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    @SuppressWarnings("unchecked")
    public static void styleCombo(JComboBox combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        combo.setPreferredSize(new Dimension(200, 40));
    }

    public static void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void flashError(JComponent comp) {
        Color original = comp.getBackground();
        comp.setBackground(Color.PINK);
        new javax.swing.Timer(200, e -> {
            comp.setBackground(original);
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }
}
