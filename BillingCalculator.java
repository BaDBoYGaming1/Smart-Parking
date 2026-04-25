import java.util.Map;

public class BillingCalculator {
    private static final Map<String, Double> HOURLY_RATES = Map.of(
        "CAR", 25.0, "BIKE", 12.0, "TRUCK", 60.0
    );

    public static double calculateFee(Vehicle v) {
        long minutes = v.getDurationMinutes();
        long hours = minutes / 60;
        long extraMinutes = minutes % 60;
        double base = Math.max(1, hours) * HOURLY_RATES.getOrDefault(v.vehicleType, 25.0);
        if (extraMinutes > 0) base += HOURLY_RATES.getOrDefault(v.vehicleType, 25.0) / 2;
        return Math.ceil(base);
    }
}
