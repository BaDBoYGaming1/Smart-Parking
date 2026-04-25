import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class EntryPanel extends JPanel {
    private final ParkingLot lot;
    private JTextField regField;
    private JComboBox<String> typeCombo;

    public EntryPanel(ParkingLot lot) {
        this.lot = lot;
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        createUI();
    }

    private void createUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Header
        JLabel header = new JLabel("New Vehicle Entry");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(UIUtils.SUCCESS_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(header, gbc);

        // Reg number
        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Vehicle No:"), gbc);
        gbc.gridx = 1;
        regField = new JTextField(15);
        UIUtils.styleField(regField);
        add(regField, gbc);

        // Type
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(UIUtils.VEHICLE_TYPES);
        UIUtils.styleCombo(typeCombo);
        add(typeCombo, gbc);

        // Park button
        gbc.gridy = 3; gbc.gridwidth = 2; gbc.gridx = 0;
        JButton parkBtn = new JButton("PARK NOW");
        UIUtils.styleButton(parkBtn, UIUtils.SUCCESS_COLOR);
        parkBtn.setPreferredSize(new Dimension(200, 50));
        parkBtn.addActionListener(e -> park());
        add(parkBtn, gbc);
    }

    private void park() {
        String reg = regField.getText().trim();
        if (reg.length() < 2) {
            UIUtils.flashError(regField);
            return;
        }
        String type = (String) typeCombo.getSelectedItem();
        Vehicle vehicle = new Vehicle("", reg, type, LocalDateTime.now(), 0);
        if (lot.park(vehicle)) {
            JOptionPane.showMessageDialog(this, 
                String.format("<html> Parked!<br>Ticket: %s<br>Slot: S%d</html>", vehicle.ticketId, vehicle.slotNumber),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            regField.setText("");
        } else {
            if (lot.availableSlots() == 0) {
                JOptionPane.showMessageDialog(this, "No available slots for " + type, "Full", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, " Vehicle " + reg + " is already parked!\\nPlease checkout first.", "Duplicate Vehicle", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
