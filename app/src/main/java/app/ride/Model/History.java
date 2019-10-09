package app.ride.Model;

public class History {

    private String from,to,driver,rider,fare,distance,vehicle;

    public History(){

    }


    public History(String from, String to, String driver, String rider, String fare, String distance, String vehicle) {
        this.from = from;
        this.to = to;
        this.driver = driver;
        this.rider = rider;
        this.fare = fare;
        this.distance = distance;
        this.vehicle = vehicle;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getRider() {
        return rider;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}
