import javax.swing.*;
import java.awt.*;

public class SlotWidget extends JPanel {
    private final JLabel label;
    private final int slotNumber;
    private final ParkingLot lot;

    public SlotWidget(int num, ParkingLot lot) {
        this.slotNumber = num;
        this.lot = lot;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setPreferredSize(new Dimension(100, 80));

        label = new JLabel("S" + num, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
        update();
    }

    public void update() {
        ParkingSlot slot = lot.getSlot(slotNumber);
        if (slot != null && slot.occupied) {
            setBackground(UIUtils.DANGER_COLOR.brighter());
            label.setText("S" + slotNumber + "\nOCCUPIED");
            label.setForeground(Color.WHITE);
        } else {
            setBackground(UIUtils.SUCCESS_COLOR.brighter());
            label.setText("S" + slotNumber + "\nFREE");
            label.setForeground(Color.BLACK);
        }
    }
}
