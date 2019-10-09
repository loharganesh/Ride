package app.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ride.Model.Vehicle;
import app.ride.View.VehicleAdapter;

public class SelectVehicle extends AppCompatActivity {

    //FIREBASE OBJECTS
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    //COMPONENTS
    private RecyclerView vehiclesRecyclerView;
    private List<Vehicle> vehicleList = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;

    private String fromLocation = "";
    private String toLocation = "";

    private String from_loc_name = "";
    private String to_loc_name = "";


    private LinearLayout settingRideProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vehicle);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Select Vehicle");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fromLocation = getIntent().getExtras().getString("fromlocation");
        toLocation = getIntent().getExtras().getString("tolocation");
        from_loc_name = getIntent().getExtras().getString("origin_name");
        to_loc_name = getIntent().getExtras().getString("destination_name");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        settingRideProgressBar = findViewById(R.id.settingRide);

        vehiclesRecyclerView = findViewById(R.id.vehicleList);
        vehicleAdapter = new VehicleAdapter(this,vehicleList);

        vehiclesRecyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        vehiclesRecyclerView.setAdapter(vehicleAdapter);

    }

    public void addVehicle(View view){
        startActivity(new Intent(getApplicationContext(),AddVehicle.class));
    }

    public void loadVehicles(){
        db.collection("vehicles").document(auth.getCurrentUser().getUid()).collection("vehicles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        vehicleList.clear();
                        if(task.isSuccessful()){
                            for(DocumentSnapshot snapshot:task.getResult()){
                                Vehicle vehicle = snapshot.toObject(Vehicle.class);
                                vehicleList.add(vehicle);
                            }
                            vehicleAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    //^[A-Z]{2}[ -][0-9]{1,2}(?: [A-Z])?(?: [A-Z]*)? [0-9]{4}$

    @Override
    protected void onStart() {
        super.onStart();
        loadVehicles();
    }

    public String getFromLocation(){
        return fromLocation;
    }

    public String getToLocation(){
        return toLocation;
    }

    public String getFromLocName(){
        return this.from_loc_name;
    }
    public String getToLocName(){
        return this.to_loc_name;
    }


    public void showProgressBar(int visibility){
        settingRideProgressBar.setVisibility(visibility);
    }

}
