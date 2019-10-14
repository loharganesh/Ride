package app.ride.Model;

import com.google.firebase.firestore.GeoPoint;

public class Driver {

    private String driver_key,driver_vehicle,drive_route_link,vehicle_seats;
    private GeoPoint drive_from,drive_to;
    private long drive_fare;
    private boolean busy;


    public Driver(){

    }

    public String getDriver_key() {
        return driver_key;
    }

    public void setDriver_key(String driver_key) {
        this.driver_key = driver_key;
    }

    public String getDriver_vehicle() {
        return driver_vehicle;
    }

    public void setDriver_vehicle(String driver_vehicle) {
        this.driver_vehicle = driver_vehicle;
    }

    public String getDrive_route_link() {
        return drive_route_link;
    }

    public void setDrive_route_link(String drive_route_link) {
        this.drive_route_link = drive_route_link;
    }

    public String getVehicle_seats() {
        return vehicle_seats;
    }

    public void setVehicle_seats(String vehicle_seats) {
        this.vehicle_seats = vehicle_seats;
    }

    public GeoPoint getDrive_from() {
        return drive_from;
    }

    public void setDrive_from(GeoPoint drive_from) {
        this.drive_from = drive_from;
    }

    public GeoPoint getDrive_to() {
        return drive_to;
    }

    public void setDrive_to(GeoPoint drive_to) {
        this.drive_to = drive_to;
    }

    public long getDrive_fare() {
        return drive_fare;
    }

    public void setDrive_fare(long drive_fare) {
        this.drive_fare = drive_fare;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}
