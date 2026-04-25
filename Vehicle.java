import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Vehicle {
    String ticketId, vehicleNumber, vehicleType;
    LocalDateTime entryTime, exitTime;
    double fee;
    int slotNumber;

    public Vehicle(String ticketId, String vehicleNumber, String vehicleType, 
                LocalDateTime entryTime, int slotNumber) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber.toUpperCase().trim();
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
        this.slotNumber = slotNumber;
    }

    public long getDurationMinutes() {
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
