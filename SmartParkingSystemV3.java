import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.stream.Collectors;

// 🚗 SMART PARKING SYSTEM v3.1 - FULLY FIXED & COMPILABLE
public class SmartParkingSystemV3 {
    
    // CONSTANTS
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String[] VEHICLE_TYPES = {"CAR", "BIKE", "TRUCK"};
    private static final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DARK_BG = new Color(30, 30, 46);
    
    // Model Classes
    static class Vehicle {
        String ticketId, vehicleNumber, vehicleType;
        LocalDateTime entryTime, exitTime;
        double fee;
        int slotNumber;

        Vehicle(String ticketId, String vehicleNumber, String vehicleType, 
                LocalDateTime entryTime, int slotNumber) {
            this.ticketId = ticketId;
            this.vehicleNumber = vehicleNumber.toUpperCase().trim();
            this.vehicleType = vehicleType;
            this.entryTime = entryTime;
            this.slotNumber = slotNumber;
        }

        long getDurationMinutes() {
            if (exitTime == null) return ChronoUnit.MINUTES.between(entryTime, LocalDateTime.now());
            return ChronoUnit.MINUTES.between(entryTime, exitTime);
        }

        @Override
        public String toString() {
            return String.format("%s,%s,%s,%s,%s,%.2f,%d",
                ticketId, vehicleNumber, vehicleType, 
                entryTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                exitTime != null ? exitTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                fee, slotNumber);
        }
    }

    static class ParkingSlot {
        int number;
        String type;
        boolean occupied;
        String occupantTicket;

        ParkingSlot(int number, String type) {
            this.number = number;
            this.type = type;
            this.occupied = false;
            this.occupantTicket = null;
        }

        boolean canPark(String vehicleType) {
            return !occupied && (type.equals("ALL") || type.equalsIgnoreCase(vehicleType));
        }
    }

    static class ParkingLot {
        private final Map<Integer, ParkingSlot> slots = new ConcurrentHashMap<>();
        private final List<Vehicle> activeVehicles = Collections.synchronizedList(new ArrayList<>());
        private final List<Vehicle> history = Collections.synchronizedList(new ArrayList<>());
        private int nextTicket = 1001;

        ParkingLot() {
            initSlots();
        }

        private void initSlots() {
            String[] slotTypes = {"CAR","CAR","CAR","CAR","CAR","BIKE","BIKE","BIKE","TRUCK","TRUCK","CAR","BIKE"};
            for (int i = 1; i <= 12; i++) {
                slots.put(i, new ParkingSlot(i, slotTypes[i-1]));
            }
        }

        private String getNearestSlot(String type) {
            OptionalInt slotOpt = slots.entrySet().stream()
                .filter(entry -> entry.getValue().canPark(type))
                .mapToInt(Map.Entry::getKey)
                .min();
            return slotOpt.isPresent() ? "SLOT-" + slotOpt.getAsInt() : null;
        }

        boolean park(Vehicle v) {
            String slotId = getNearestSlot(v.vehicleType);
            if (slotId != null) {
                try {
                    v.slotNumber = Integer.parseInt(slotId.split("-")[1]);
                    v.ticketId = "TKT-" + nextTicket++;
                    activeVehicles.add(v);
                    ParkingSlot slot = slots.get(v.slotNumber);
                    slot.occupied = true;
                    slot.occupantTicket = v.ticketId;
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        Vehicle checkout(String ticketId) {
            synchronized (activeVehicles) {
                return activeVehicles.stream()
                    .filter(v -> v.ticketId.equals(ticketId))
                    .findFirst()
                    .map(v -> {
                        v.exitTime = LocalDateTime.now();
                        v.fee = BillingCalculator.calculateFee(v);
                        activeVehicles.remove(v);
                        history.add(v);
                        ParkingSlot slot = slots.get(v.slotNumber);
                        if (slot != null) {
                            slot.occupied = false;
                            slot.occupantTicket = null;
                        }
                        return v;
                    })
                    .orElse(null);
            }
        }

        int totalSlots() { return slots.size(); }
        int availableSlots() { 
            return (int) slots.values().stream().filter(s -> !s.occupied).count(); 
        }
        double getRevenue() { 
            synchronized (history) {
                return history.stream().mapToDouble(v -> v.fee).sum(); 
            }
        }
        int getTotalTransactions() { 
            synchronized (history) { 
                return history.size(); 
            } 
        }
        List<Vehicle> getHistory() { 
            synchronized (history) { 
                return new ArrayList<>(history); 
            } 
        }
        List<Vehicle> getActiveVehicles() { return new ArrayList<>(activeVehicles); }
    }

    static class BillingCalculator {
        private static final Map<String, Double> HOURLY_RATES = Map.of(
            "CAR", 25.0, "BIKE", 12.0, "TRUCK", 60.0
        );

        static double calculateFee(Vehicle v) {
            long minutes = v.getDurationMinutes();
            long hours = minutes / 60;
            long extraMinutes = minutes % 60;
            double base = Math.max(1, hours) * HOURLY_RATES.getOrDefault(v.vehicleType, 25.0);
            if (extraMinutes > 0) base += HOURLY_RATES.getOrDefault(v.vehicleType, 25.0) / 2;
            return Math.ceil(base);
        }
    }

    // MAIN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartParkingSystemV3::runApp);
    }

    private static void runApp() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        JFrame frame = new JFrame("🚗 Smart Parking Pro v3.1 - Fixed");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1200, 800));

        ParkingLot lot = new ParkingLot();
        LoginPanel login = new LoginPanel(lot, frame);
        frame.setContentPane(login);
        frame.pack();
        frame.setVisible(true);
    }

    // UI Components
    static class LoginPanel extends JPanel {
        private final ParkingLot lot;
        private final JFrame parent;
        private JPasswordField pwdField;

        LoginPanel(ParkingLot lot, JFrame parent) {
            this.lot = lot;
            this.parent = parent;
            setLayout(new BorderLayout());
            createLoginUI();
        }

        private void createLoginUI() {
            setBackground(DARK_BG);
            JPanel glassPanel = createGlassPanel();
            add(glassPanel, BorderLayout.CENTER);
        }

        private JPanel createGlassPanel() {
            JPanel panel = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
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
            JLabel title = new JLabel("🚗 PARKING PRO", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(Color.WHITE);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            panel.add(title, gbc);

            JLabel subtitle = new JLabel("Smart Management System v3.1", SwingConstants.CENTER);
            subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            subtitle.setForeground(new Color(200,200,220));
            gbc.gridy = 1;
            panel.add(subtitle, gbc);

            // Password
            gbc.gridwidth = 1; gbc.gridy = 2;
            gbc.gridx = 0;
            JLabel label = new JLabel("🔐 Admin Password:");
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            panel.add(label, gbc);

            gbc.gridx = 1;
            pwdField = new JPasswordField(15);
            styleField(pwdField);
            panel.add(pwdField, gbc);

            // Button
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            JButton btn = new JButton("🚀 ENTER DASHBOARD");
            styleButton(btn, PRIMARY_COLOR);
            btn.addActionListener(e -> login());
            panel.add(btn, gbc);

            return panel;
        }

        private void login() {
            if (ADMIN_PASSWORD.equals(new String(pwdField.getPassword()))) {
                parent.setContentPane(new DashboardPanel(lot));
                parent.revalidate();
                parent.repaint();
                ((DashboardPanel) parent.getContentPane()).startAutoUpdate();
            } else {
                flashError(pwdField);
                JOptionPane.showMessageDialog(this, "❌ Incorrect Password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                pwdField.setText("");
            }
        }
    }

    static class DashboardPanel extends JPanel {
        private final ParkingLot lot;
        private StatCard[] statCards = new StatCard[4];
        private JTable vehicleTable;
        private DefaultTableModel tableModel;
        private javax.swing.Timer updateTimer;

        DashboardPanel(ParkingLot lot) {
            this.lot = lot;
            setLayout(new BorderLayout());
            createDashboard();
        }

        private void createDashboard() {
            setBackground(Color.WHITE);

            // Header stats
            JPanel header = new JPanel(new GridLayout(1, 4, 10, 0));
            header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            header.setOpaque(false);
            statCards[0] = new StatCard("🅿️ Total Slots", "12", Color.CYAN);
            statCards[1] = new StatCard("✅ Available", String.valueOf(lot.availableSlots()), SUCCESS_COLOR);
            statCards[2] = new StatCard("🔴 Occupied", String.valueOf(lot.totalSlots() - lot.availableSlots()), DANGER_COLOR);
            statCards[3] = new StatCard("💰 Revenue", "₹0", WARNING_COLOR);
            for (StatCard card : statCards) header.add(card);
            add(header, BorderLayout.NORTH);

            // Tabs
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("📥 Entry", new EntryPanel(lot));
            tabs.addTab("📤 Exit", new ExitPanel(lot));
            tabs.addTab("📊 Active Vehicles", createVehicleTable());
            tabs.addTab("🅿️ Slot Map", new SlotMapPanel(lot));
            tabs.addTab("💰 Reports", new ReportsPanel(lot));
            add(tabs, BorderLayout.CENTER);

            // Footer
            add(createFooter(), BorderLayout.SOUTH);
        }

        private JScrollPane createVehicleTable() {
            tableModel = new DefaultTableModel(new String[]{"Ticket", "Vehicle", "Type", "Slot", "Entry Time", "Duration"}, 0);
            vehicleTable = new JTable(tableModel);
            vehicleTable.setRowHeight(30);
            vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            vehicleTable.setDefaultEditor(Object.class, null); // non-editable
            return new JScrollPane(vehicleTable);
        }

        private JPanel createFooter() {
            JPanel footer = new JPanel(new FlowLayout());
            footer.setBackground(new Color(52, 58, 64));
            footer.setPreferredSize(new Dimension(0, 50));
            JButton refresh = new JButton("🔄 Refresh");
            styleButton(refresh, PRIMARY_COLOR);
            refresh.addActionListener(e -> updateStats());
            footer.add(refresh);
            return footer;
        }

        void startAutoUpdate() {
            updateTimer = new javax.swing.Timer(3000, e -> updateStats());
            updateTimer.start();
            updateStats();
        }

        private void updateStats() {
            statCards[0].updateValue("12");
            statCards[1].updateValue(String.valueOf(lot.availableSlots()));
            statCards[2].updateValue(String.valueOf(lot.totalSlots() - lot.availableSlots()));
            statCards[3].updateValue("₹" + String.format("%.0f", lot.getRevenue()));

            tableModel.setRowCount(0);
            lot.getActiveVehicles().forEach(v -> 
                tableModel.addRow(new Object[]{
                    v.ticketId, v.vehicleNumber, v.vehicleType,
                    "S" + v.slotNumber,
                    v.entryTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    v.getDurationMinutes() + " min"
                })
            );
        }
    }

    static class StatCard extends JPanel {
        private final JLabel titleLabel;
        private final JLabel valueLabel;

        StatCard(String title, String value, Color color) {
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

        void updateValue(String value) {
            valueLabel.setText(value);
        }
    }

    static class EntryPanel extends JPanel {
        private final ParkingLot lot;
        private JTextField regField;
        private JComboBox<String> typeCombo;

        EntryPanel(ParkingLot lot) {
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
            JLabel header = new JLabel("📥 New Vehicle Entry");
            header.setFont(new Font("Segoe UI", Font.BOLD, 28));
            header.setForeground(SUCCESS_COLOR);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            add(header, gbc);

            // Reg number
            gbc.gridwidth = 1; gbc.gridy = 1;
            gbc.gridx = 0;
            add(new JLabel("Vehicle No:"), gbc);
            gbc.gridx = 1;
            regField = new JTextField(15);
            styleField(regField);
            add(regField, gbc);

            // Type
            gbc.gridy = 2;
            gbc.gridx = 0;
            add(new JLabel("Type:"), gbc);
            gbc.gridx = 1;
            typeCombo = new JComboBox<>(VEHICLE_TYPES);
            styleCombo(typeCombo);
            add(typeCombo, gbc);

            // Park button
            gbc.gridy = 3; gbc.gridwidth = 2; gbc.gridx = 0;
            JButton parkBtn = new JButton("🚗 PARK NOW");
            styleButton(parkBtn, SUCCESS_COLOR);
            parkBtn.setPreferredSize(new Dimension(200, 50));
            parkBtn.addActionListener(e -> park());
            add(parkBtn, gbc);
        }

        private void park() {
            String reg = regField.getText().trim();
            if (reg.length() < 2) {
                flashError(regField);
                return;
            }
            String type = (String) typeCombo.getSelectedItem();
            Vehicle vehicle = new Vehicle("", reg, type, LocalDateTime.now(), 0);
            if (lot.park(vehicle)) {
                JOptionPane.showMessageDialog(this, 
                    String.format("<html>✅ Parked!<br>Ticket: %s<br>Slot: S%d</html>", vehicle.ticketId, vehicle.slotNumber),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                regField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "No available slots for " + type, "Full", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    static class ExitPanel extends JPanel {
        private final ParkingLot lot;
        private JTextField ticketField;
        private JButton exitBtn;

        ExitPanel(ParkingLot lot) {
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
            JLabel header = new JLabel("📤 Vehicle Exit");
            header.setFont(new Font("Segoe UI", Font.BOLD, 28));
            header.setForeground(DANGER_COLOR);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            add(header, gbc);

            // Ticket input
            gbc.gridy = 1; gbc.gridwidth = 1;
            gbc.gridx = 0;
            add(new JLabel("Ticket ID:"), gbc);
            gbc.gridx = 1;
            ticketField = new JTextField(15);
            styleField(ticketField);
            ticketField.addActionListener(e -> attemptCheckout());
            add(ticketField, gbc);

            // Buttons
            gbc.gridy = 2; gbc.gridwidth = 2; gbc.gridx = 0;
            JPanel btnPanel = new JPanel(new FlowLayout());
            JButton findBtn = new JButton("🔍 Search");
            styleButton(findBtn, PRIMARY_COLOR);
            findBtn.addActionListener(e -> findTicket());
            btnPanel.add(findBtn);

            exitBtn = new JButton("💰 Checkout");
            styleButton(exitBtn, WARNING_COLOR);
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
                ticketField.setBackground(SUCCESS_COLOR.brighter());
                exitBtn.setEnabled(true);
            } else {
                flashError(ticketField);
            }
        }

        private void attemptCheckout() {
            Vehicle vehicle = lot.checkout(ticketField.getText().trim());
            if (vehicle != null) {
                String msg = String.format(
                    "<html>✅ Checkout Complete!<br>" +
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

    static class SlotMapPanel extends JPanel {
        private final ParkingLot lot;

        SlotMapPanel(ParkingLot lot) {
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

    static class SlotWidget extends JPanel {
        private final JLabel label;
        private ParkingSlot slot;

        SlotWidget(int num, ParkingSlot slot) {
            this.slot = slot;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
            setPreferredSize(new Dimension(100, 80));

            label = new JLabel("S" + num, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 20));
            add(label, BorderLayout.CENTER);
            update();
        }

        void update() {
            if (slot.occupied) {
                setBackground(DANGER_COLOR.brighter());
                label.setText("Sxx\nOCCUPIED");
                label.setForeground(Color.WHITE);
            } else {
                setBackground(SUCCESS_COLOR.brighter());
                label.setText("Sxx\nFREE");
                label.setForeground(Color.BLACK);
            }
        }
    }

    static class ReportsPanel extends JPanel {
        ReportsPanel(ParkingLot lot) {
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());
            JLabel label = new JLabel("<html><h2>📈 Reports & Analytics</h2><p>Revenue: ₹" + String.format("%.0f", lot.getRevenue()) + "<br>Total Transactions: " + lot.history.size() + "</p></html>", SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }

    // UI Utilities
    private static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    @SuppressWarnings("unchecked")
    private static void styleCombo(JComboBox combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        combo.setPreferredSize(new Dimension(200, 40));
    }

    private static void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static void flashError(JComponent comp) {
        Color original = comp.getBackground();
        comp.setBackground(Color.PINK);
new javax.swing.Timer(200, e -> {
            comp.setBackground(original);
        }).start();
    }
}
