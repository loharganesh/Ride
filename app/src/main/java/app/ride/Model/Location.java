package app.ride.Model;

import com.google.firebase.firestore.GeoPoint;

public class Location {

    private String location_name;
    private GeoPoint location;

    public Location(){

    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public Location(String name, GeoPoint location) {
        this.location_name = name;
        this.location = location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }



    public GeoPoint getLocation() {
        return location;
    }
}
