import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.OptionalInt;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ParkingLot {
    private final Map<Integer, ParkingSlot> slots = new ConcurrentHashMap<>();
    private final List<Vehicle> activeVehicles = Collections.synchronizedList(new ArrayList<>());
    private final List<Vehicle> history = Collections.synchronizedList(new ArrayList<>());
    private int nextTicket = 1001;

    public ParkingLot() {
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

    public boolean park(Vehicle v) {
        String slotId = getNearestSlot(v.vehicleType);
        synchronized (activeVehicles) {
            if (activeVehicles.stream().anyMatch(existing -> existing.vehicleNumber.equals(v.vehicleNumber))) {
                return false;
            }
        }
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

    public Vehicle checkout(String ticketId) {
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

    public ParkingSlot getSlot(int number) {
        return slots.get(number);
    }

    public int totalSlots() { return slots.size(); }
    public int availableSlots() { 
        return (int) slots.values().stream().filter(s -> !s.occupied).count(); 
    }
    public double getRevenue() { 
        synchronized (history) {
            return history.stream().mapToDouble(v -> v.fee).sum(); 
        }
    }
    public int getTotalTransactions() { 
        synchronized (history) { 
            return history.size(); 
        } 
    }
    public List<Vehicle> getHistory() { 
        synchronized (history) { 
            return new ArrayList<>(history); 
        }
    }
    public List<Vehicle> getActiveVehicles() { return new ArrayList<>(activeVehicles); }
}
