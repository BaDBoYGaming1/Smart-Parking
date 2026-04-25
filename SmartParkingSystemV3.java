import javax.swing.*;
import java.awt.Dimension;

public class SmartParkingSystemV3 {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartParkingSystemV3::runApp);
    }

    public static void runApp() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        JFrame frame = new JFrame("Smart Parking Pro v3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1200, 800));

        ParkingLot lot = new ParkingLot();
        LoginPanel login = new LoginPanel(lot, frame);
        frame.setContentPane(login);
        frame.pack();
        frame.setVisible(true);
    }
}
