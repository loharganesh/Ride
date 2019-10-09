package app.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

import static app.ride.Constants.getRoomId;

public class ManageRide extends AppCompatActivity {

    private TextView pickupStatus,reacStatus,fareStatus,distanceTxt,fareAmtTxt;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String driver_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ride);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Your Ride");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        driver_id = getIntent().getExtras().getString("driver_key");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        pickupStatus = findViewById(R.id.pickupStatusTxt);
        reacStatus = findViewById(R.id.reachedStatusTxt);
        fareStatus = findViewById(R.id.farePAidStatus);
        distanceTxt = findViewById(R.id.distanceTxt);
        fareAmtTxt = findViewById(R.id.fareAmtTxt);



        setDistanceandFare();
        setUI();
    }

    public void setDistanceandFare(){
        db.collection("drivers").document(driver_id)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.getResult().exists()){
                        distanceTxt.setText(""+task.getResult().get("drive_km"));
                        fareAmtTxt.setText(""+task.getResult().get("drive_fare"));
                    }

                }
            });
    }

    public void setUI(){
        db.collection("ongoingrides").document(getRoomId(driver_id,auth.getCurrentUser().getUid()))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){

                            if(documentSnapshot.getBoolean("pickup_confirmed")){
                                pickupStatus.setTextColor(Color.BLACK);
                            }else{
                                pickupStatus.setTextColor(Color.LTGRAY);
                            }

                            if(documentSnapshot.getBoolean("reached_destn")){
                                reacStatus.setTextColor(Color.BLACK);
                            }else{
                                reacStatus.setTextColor(Color.LTGRAY);
                            }

                            if(documentSnapshot.getBoolean("fare_paid")){
                                fareStatus.setTextColor(Color.BLACK);
                            }else{
                                fareStatus.setTextColor(Color.LTGRAY);
                            }


                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
