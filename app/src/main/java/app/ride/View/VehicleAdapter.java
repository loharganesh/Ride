package app.ride.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ride.Drive;
import app.ride.Model.Driver;
import app.ride.Model.Vehicle;
import app.ride.R;
import app.ride.SelectVehicle;
import app.ride.SplashScreen;

public class VehicleAdapter extends RecyclerView.Adapter{

    private Context context;
    private List<Vehicle> vehicleList;

    public VehicleAdapter(Context context, List<Vehicle> req){
        this.vehicleList = req;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_card_layout, parent, false);
        VehiclesHolder vh = new VehiclesHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final Vehicle vehicle = vehicleList.get(position);

        ((VehiclesHolder)holder).vehicleNameTxt.setText(vehicle.getVehicle_name());
        ((VehiclesHolder)holder).vehicleNumberTxt.setText(vehicle.getVehicle_number());


        ((VehiclesHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((SelectVehicle)context).showProgressBar(View.VISIBLE);

                 String from_location_str = ((SelectVehicle)context).getFromLocation();

                 String from_loc_name = ((SelectVehicle)context).getFromLocName();
                 String to_loc_name = ((SelectVehicle)context).getToLocName();

                 double  from_loc_lat = Double.parseDouble(from_location_str.substring(0,from_location_str.indexOf("+")));
                 double  from_loc_long = Double.parseDouble(from_location_str.substring(from_location_str.indexOf("+")));

                 GeoPoint from_location = new GeoPoint(from_loc_lat,from_loc_long);

                 String to_location_str = ((SelectVehicle)context).getToLocation();

                 double  to_loc_lat = Double.parseDouble(to_location_str.substring(0,to_location_str.indexOf("+")));
                 double  to_loc_long = Double.parseDouble(to_location_str.substring(to_location_str.indexOf("+")));

                 GeoPoint to_location = new GeoPoint(to_loc_lat,to_loc_long);

                 double km = distance(from_loc_lat,from_loc_long,to_loc_lat,to_loc_long,"K");

                 Map<String,Object> driver = new HashMap<>();
                 driver.put("driver_key", FirebaseAuth.getInstance().getCurrentUser().getUid());
                 driver.put("driver_vehicle",vehicle.getKey());
                 driver.put("drive_from",from_location);
                 driver.put("drive_to",to_location);
                 driver.put("drive_for","");
                 driver.put("origin",from_loc_name);
                 driver.put("destination",to_loc_name);
                 driver.put("drive_status","not_driving");
                 driver.put("drive_km",km);
                 driver.put("drive_fare",getPrice(km));
                 driver.put("drive_route_link",from_loc_lat+""+from_loc_long+""+to_loc_lat+""+to_loc_long);


                SharedPreferences geoPoints = context.getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = geoPoints.edit();
                editor.putString("from_loc_lat",""+from_loc_lat);
                editor.putString("from_loc_long",""+from_loc_long);
                editor.putString("to_loc_lat",""+to_loc_lat);
                editor.putString("origin",from_loc_name);
                editor.putString("destination",to_loc_name);
                editor.putString("to_loc_long",""+to_loc_long);
                editor.putString("role","driver");
                editor.putBoolean("busy",true);
                editor.commit();

                 FirebaseFirestore.getInstance().collection("drivers").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                         .set(driver)
                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 FirebaseFirestore.getInstance().collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                         .update("busy",true,"role","driver")
                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 Intent intent = new Intent(context, SplashScreen.class);
                                                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                 intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                 context.startActivity(intent);
                                                 ((SelectVehicle) context).finishAffinity();

                                             }
                                         });
                             }
                         })
                        .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ((SelectVehicle)context).showProgressBar(View.INVISIBLE);
                        }
                 });

            }
        });


        ((VehiclesHolder)holder).deleteVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseFirestore.getInstance().collection("vehicles").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("vehicles").document(vehicle.getKey()).delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                vehicleList.remove(vehicle);
                                notifyDataSetChanged();
                            }
                        });
            }
        });

    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }


    public class VehiclesHolder extends RecyclerView.ViewHolder {

        private TextView vehicleNameTxt,vehicleNumberTxt;
        private ImageView deleteVehicle;

        public VehiclesHolder(View itemView) {
            super(itemView);

            vehicleNameTxt = itemView.findViewById(R.id.vehicleNameTxt);
            vehicleNumberTxt = itemView.findViewById(R.id.vehicleNumberTxt);
            deleteVehicle = itemView.findViewById(R.id.deleteVehicle);


        }
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit == "K") {
                dist = dist * 1.609344;
                dist=Math.round(dist);
            } else if (unit == "N") {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    private int getPrice(double km){

        if(km <= 5 ){
            return 10;
        }else if(km > 5 && km <=10){
            return 20;
        }else if(km > 10 && km <= 15){
            return 30;
        }else if (km > 15 && km <= 20){
            return 40;
        }else if(km > 20 && km <= 25) {
            return 50;
        }else if(km > 25 && km <= 30) {
            return 60;
        }else if(km > 30 && km <= 35) {
            return 70;
        }else if(km > 35 && km <= 40) {
            return 80;
        }else if(km > 40 && km <= 45) {
            return 90;
        }else if(km > 45 && km <= 50) {
            return 100;
        }else{
            return 100;
        }
    }

}

