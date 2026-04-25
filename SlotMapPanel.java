import javax.swing.*;
import java.awt.*;

public class SlotMapPanel extends JPanel {
    private final ParkingLot lot;

    public SlotMapPanel(ParkingLot lot) {
        this.lot = lot;
        setBackground(Color.WHITE);
        setLayout(new GridLayout(4, 3, 10, 10));
        createSlots();
    }

    private void createSlots() {
        for (int i = 1; i <= 12; i++) {
            add(new SlotWidget(i, lot));
        }
    }

    public void refreshSlots() {
        Component[] components = getComponents();
        for (Component c : components) {
            if (c instanceof SlotWidget) {
                ((SlotWidget) c).update();
            }
        }
        repaint();
    }
}
