import javax.swing.*;
import java.awt.*;

public class ExitPanel extends JPanel {
    private final ParkingLot lot;
    private JTextField ticketField;
    private JButton exitBtn;

    public ExitPanel(ParkingLot lot) {
        this.lot = lot;
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        createUI();
    }

    private void createUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header
        JLabel header = new JLabel("Vehicle Exit");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(UIUtils.DANGER_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(header, gbc);

        // Ticket input
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Ticket ID:"), gbc);
        gbc.gridx = 1;
        ticketField = new JTextField(15);
        UIUtils.styleField(ticketField);
        ticketField.addActionListener(e -> attemptCheckout());
        add(ticketField, gbc);

        // Buttons
        gbc.gridy = 2; gbc.gridwidth = 2; gbc.gridx = 0;
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton findBtn = new JButton("Search");
        UIUtils.styleButton(findBtn, UIUtils.PRIMARY_COLOR);
        findBtn.addActionListener(e -> findTicket());
        btnPanel.add(findBtn);

        exitBtn = new JButton("Checkout");
        UIUtils.styleButton(exitBtn, UIUtils.WARNING_COLOR);
        exitBtn.setEnabled(false);
        exitBtn.addActionListener(e -> attemptCheckout());
        btnPanel.add(exitBtn);
        add(btnPanel, gbc);
    }

    private void findTicket() {
        String ticket = ticketField.getText().trim().toUpperCase();
        boolean found = lot.getActiveVehicles().stream()
            .anyMatch(v -> v.ticketId.contains(ticket) || v.vehicleNumber.contains(ticket));
        if (found) {
            ticketField.setBackground(UIUtils.SUCCESS_COLOR.brighter());
            exitBtn.setEnabled(true);
        } else {
            UIUtils.flashError(ticketField);
        }
    }

    private void attemptCheckout() {
        Vehicle vehicle = lot.checkout(ticketField.getText().trim());
        if (vehicle != null) {
            String msg = String.format(
                "<html> Checkout Complete!<br>" +
                "Vehicle: %s<br>Duration: %d min<br>Fee: ₹%.0f</html>",
                vehicle.vehicleNumber, vehicle.getDurationMinutes(), vehicle.fee
            );
            JOptionPane.showMessageDialog(this, msg, "Receipt", JOptionPane.INFORMATION_MESSAGE);
            ticketField.setText("");
            exitBtn.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this, "Vehicle not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
