package app.ride.Model;

public class Vehicle {

    private String vehicle_name,vehicle_number,vehicle_license,vehicle_seats,key,mobile_number;

    public Vehicle(){

    }

    public Vehicle(String vehicle_name, String vehicle_number, String vehicle_license, String vehicle_seats, String key, String mobile_number) {
        this.vehicle_name = vehicle_name;
        this.vehicle_number = vehicle_number;
        this.vehicle_license = vehicle_license;
        this.vehicle_seats = vehicle_seats;
        this.key = key;
        this.mobile_number = mobile_number;
    }

    public String getVehicle_name() {
        return vehicle_name;
    }

    public void setVehicle_name(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }

    public String getVehicle_number() {
        return vehicle_number;
    }

    public void setVehicle_number(String vehicle_number) {
        this.vehicle_number = vehicle_number;
    }

    public String getVehicle_license() {
        return vehicle_license;
    }

    public void setVehicle_license(String vehicle_license) {
        this.vehicle_license = vehicle_license;
    }

    public String getVehicle_seats() {
        return vehicle_seats;
    }

    public void setVehicle_seats(String vehicle_seats) {
        this.vehicle_seats = vehicle_seats;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }
}
