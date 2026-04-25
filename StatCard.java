import javax.swing.*;
import java.awt.*;

public class StatCard extends JPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;

    public StatCard(String title, String value, Color color) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        add(titleLabel, BorderLayout.NORTH);

        valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color.darker());
        add(valueLabel, BorderLayout.CENTER);
    }

    public void updateValue(String value) {
        valueLabel.setText(value);
    }
}
