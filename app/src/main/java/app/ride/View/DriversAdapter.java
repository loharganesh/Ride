package app.ride.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ride.Model.Driver;
import app.ride.R;
import app.ride.Ride;

import static android.content.Context.MODE_PRIVATE;

public class DriversAdapter extends RecyclerView.Adapter{

    private Context context;
    private List<Driver> driversList;

    public DriversAdapter(Context context, List<Driver> req){
        this.driversList = req;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_layout, parent, false);
        DriversHolder vh = new DriversHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final Driver driver = driversList.get(position);

        final String mobile_number ;


        ((DriversHolder)holder).fareTxt.setText(""+driver.getDrive_fare()+" Rs.");

        FirebaseFirestore.getInstance().collection("users").document(driver.getDriver_key())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ((DriversHolder)holder).driverNameTxt.setText(task.getResult().getString("name"));
                        ((DriversHolder)holder).driverRating.setRating(task.getResult().getLong("drive_rating"));
                    }
                });


        if(!driver.isBusy()){
            ((DriversHolder)holder).requestBtn.setEnabled(true);
        }else{
            ((DriversHolder)holder).requestBtn.setEnabled(false);
        }

        FirebaseFirestore.getInstance().collection("vehicles").document(driver.getDriver_key()).collection("vehicles").document(driver.getDriver_vehicle())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ((DriversHolder)holder).driverVehicleName.setText(task.getResult().getString("vehicle_name"));
                        ((DriversHolder)holder).vehicleNumber.setText(task.getResult().getString("vehicle_number"));
                        ((DriversHolder)holder).totalSeats.setText(task.getResult().getString("vehicle_seats")+" Seats  | ");

                    }
                });

        ((DriversHolder)holder).requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((DriversHolder)holder).progressBar.setVisibility(View.VISIBLE);

                FirebaseFirestore.getInstance().collection("drivers").document(driver.getDriver_key())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.getResult().exists()){

                                if(!task.getResult().getBoolean("busy")){
                                    final Map<String,Object> request = new HashMap<>();
                                    request.put("rider_name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                    request.put("rider_key",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    request.put("driver_key",driver.getDriver_key());

                                    FirebaseFirestore.getInstance().collection("riderequests").document(driver.getDriver_key())
                                        .collection("riderequests").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(request)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                FirebaseFirestore.getInstance().collection("drivers").document(driver.getDriver_key())
                                                    .update("drive_for",FirebaseAuth.getInstance().getCurrentUser().getUid(),"drive_status","not_driving")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            FirebaseFirestore.getInstance().collection("riders").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .update("ride_with",driver.getDriver_key(),"ride_status","requested")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        ((DriversHolder) holder).progressBar.setVisibility(View.GONE);

                                                                        SharedPreferences geoPoints = context.getSharedPreferences("requeststatus", MODE_PRIVATE);
                                                                        SharedPreferences.Editor editor = geoPoints.edit();
                                                                        editor.putString("req_accepted","false");
                                                                        editor.commit();

                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {

                                                                                SharedPreferences prefs = context.getSharedPreferences("requeststatus", MODE_PRIVATE);
                                                                                String req = prefs.getString("req_accepted","false");//"No name defined" is the default value.

                                                                                if(req.equals("false")){

                                                                                    Snackbar snackbar = Snackbar
                                                                                            .make(((Ride)context).getList(), "Request not Accepted", Snackbar.LENGTH_LONG);
                                                                                    snackbar.show();
                                                                                    //DELETING REQUEST
                                                                                    FirebaseFirestore.getInstance().collection("riderequests").document(driver.getDriver_key())
                                                                                            .collection("riderequests").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();


                                                                                    //SETTING RIDER STATUS TO DEFAULT
                                                                                    FirebaseFirestore.getInstance().collection("riders").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                            .update("ride_with","","ride_status","not_riding");


                                                                                }else{

                                                                                }
                                                                            }
                                                                        }, 10000);

                                                                    }
                                                                });
                                                        }
                                                    });
                                            }
                                        });
                                }else{
                                    Toast.makeText(context, "Driver is busy", Toast.LENGTH_SHORT).show();
                                    ((DriversHolder)holder).progressBar.setVisibility(View.GONE);
                                }

                            }
                        }
                    });


            }
        });

    }

    @Override
    public int getItemCount() {
        return driversList.size();
    }


    public class DriversHolder extends RecyclerView.ViewHolder {

        private TextView driverNameTxt,driverVehicleName,fareTxt,vehicleNumber;
        private ImageView userImage;
        private Button requestBtn;
        private ProgressBar progressBar;
        private RatingBar driverRating;
        private TextView totalSeats;

        public DriversHolder(View itemView) {
            super(itemView);
            driverNameTxt = itemView.findViewById(R.id.username);
            driverVehicleName = itemView.findViewById(R.id.vehicleName);
            fareTxt = itemView.findViewById(R.id.fareAmount);
            vehicleNumber = itemView.findViewById(R.id.vehicleNumber);
            userImage = itemView.findViewById(R.id.imageView6);
            requestBtn = itemView.findViewById(R.id.requestRideBtn);
            progressBar = itemView.findViewById(R.id.progressBar5);
            driverRating = itemView.findViewById(R.id.driverRating);
            totalSeats = itemView.findViewById(R.id.totalSeats);
        }
    }

}
