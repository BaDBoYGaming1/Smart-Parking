public class ParkingSlot {
    int number;
    String type;
    boolean occupied;
    String occupantTicket;

    public ParkingSlot(int number, String type) {
        this.number = number;
        this.type = type;
        this.occupied = false;
        this.occupantTicket = null;
    }

    public boolean canPark(String vehicleType) {
        return !occupied && (type.equals("ALL") || type.equalsIgnoreCase(vehicleType));
    }
}
