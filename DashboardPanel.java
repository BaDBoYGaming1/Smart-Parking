import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final ParkingLot lot;
    private StatCard[] statCards = new StatCard[4];
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private javax.swing.Timer updateTimer;
    private SlotMapPanel slotMapPanel;
    private ReportsPanel reportsPanel;

    public DashboardPanel(ParkingLot lot) {
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
        statCards[0] = new StatCard("Total Slots", "12", Color.CYAN);
        statCards[1] = new StatCard("Available", String.valueOf(lot.availableSlots()), UIUtils.SUCCESS_COLOR);
        statCards[2] = new StatCard("Occupied", String.valueOf(lot.totalSlots() - lot.availableSlots()), UIUtils.DANGER_COLOR);
        statCards[3] = new StatCard("Revenue", "₹0", UIUtils.WARNING_COLOR);
        for (StatCard card : statCards) header.add(card);
        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Entry", new EntryPanel(lot));
        tabs.addTab("Exit", new ExitPanel(lot));
        tabs.addTab("Active Vehicles", createVehicleTable());
        slotMapPanel = new SlotMapPanel(lot);
        tabs.addTab("Slot Map", slotMapPanel);
        reportsPanel = new ReportsPanel(lot);
        tabs.addTab("Reports", reportsPanel);
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
        JButton refresh = new JButton("Refresh");
        UIUtils.styleButton(refresh, UIUtils.PRIMARY_COLOR);
        refresh.addActionListener(e -> updateStats());
        footer.add(refresh);
        return footer;
    }

    public void startAutoUpdate() {
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
        List<Vehicle> active = lot.getActiveVehicles();
        for (Vehicle v : active) {
            tableModel.addRow(new Object[]{
                v.ticketId, v.vehicleNumber, v.vehicleType,
                "S" + v.slotNumber,
                v.entryTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                v.getDurationMinutes() + " min"
            });
        }

        if (slotMapPanel != null) {
            slotMapPanel.refreshSlots();
        }
        if (reportsPanel != null) {
            reportsPanel.refresh();
        }
    }
}
