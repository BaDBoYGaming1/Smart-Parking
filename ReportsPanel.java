import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class ReportsPanel extends JPanel {
    private final ParkingLot lot;
    private final JLabel label;

    public ReportsPanel(ParkingLot lot) {
        this.lot = lot;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        String revenue = "₹" + String.format("%.0f", lot.getRevenue());
        label.setText("<html><h2> Reports & Analytics</h2><p>Revenue: " + revenue + "<br>Total Transactions: " + lot.getTotalTransactions() + "</p></html>");
    }
}
