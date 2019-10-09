package app.ride.Model;

import com.google.firebase.firestore.GeoPoint;

public class Requests {

    private String rider_name,rider_key,driver_key;


    public Requests(){

    }

    public Requests(String rider_name, String rider_key, String driver_key) {
        this.rider_name = rider_name;
        this.rider_key = rider_key;
        this.driver_key = driver_key;
    }

    public String getRider_name() {
        return rider_name;
    }

    public void setRider_name(String rider_name) {
        this.rider_name = rider_name;
    }

    public String getRider_key() {
        return rider_key;
    }

    public void setRider_key(String rider_key) {
        this.rider_key = rider_key;
    }

    public String getDriver_key() {
        return driver_key;
    }

    public void setDriver_key(String driver_key) {
        this.driver_key = driver_key;
    }
}
