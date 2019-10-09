package app.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static app.ride.Constants.getRoomId;

public class ManageDrive extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout cancelDrive;
    private ProgressBar progressBar;
    private TextView cancelDriveTxt;

    private String drive_for_id;
    private String drive_from,drive_to,drive_fare,drive_distance,drive_vehicle;
    private TextView pickupStatus,completeDrive,fareCollect,distanceTxt,fareTxt;

    private Button confirmPickupBtn,reachedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_drive);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Manage your drive");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drive_for_id = getIntent().getExtras().getString("driveFor");
        drive_from = getIntent().getExtras().getString("drive_from");
        drive_to = getIntent().getExtras().getString("drive_to");
        drive_vehicle = getIntent().getExtras().getString("drive_vehicle");


        Toast.makeText(this, drive_vehicle, Toast.LENGTH_SHORT).show();


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cancelDrive = findViewById(R.id.cancelDrive);
        progressBar = findViewById(R.id.progressBar);
        cancelDriveTxt = findViewById(R.id.cancelDriveTxt);

        pickupStatus  = findViewById(R.id.pickupStatusTxt);
        completeDrive = findViewById(R.id.completeDriveTxt);
        fareCollect   = findViewById(R.id.fareCollectTxt);
        distanceTxt   = findViewById(R.id.distanceTxtDrive);
        fareTxt   = findViewById(R.id.fareAmtTxtDrive);
        confirmPickupBtn= findViewById(R.id.confirmBtnDrive);
        reachedBtn = findViewById(R.id.completeDriveBtn);
        cancelDrive = findViewById(R.id.cancelDrive);


        getDriveStatus();
        setDistanceandFare();

        reachedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("ongoingrides").document(getRoomId(auth.getCurrentUser().getUid(),drive_for_id))
                        .update("reached_destn",true)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                reachedBtn.setVisibility(View.INVISIBLE);
                                completeDrive.setText("Reached to destination");
                                completeDrive.setTextColor(Color.BLACK);
                            }
                        });
            }
        });

        confirmPickupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("ongoingrides").document(getRoomId(auth.getCurrentUser().getUid(),drive_for_id))
                        .update("pickup_confirmed",true)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                confirmPickupBtn.setVisibility(View.INVISIBLE);
                                pickupStatus.setText("Pickup Confirmed");
                                pickupStatus.setTextColor(Color.BLACK);
                            }
                        });
            }
        });


    }

    public void getDriveStatus(){
        db.collection("ongoingrides").document(getRoomId(drive_for_id,auth.getCurrentUser().getUid()))
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){

                        if(!documentSnapshot.getBoolean("pickup_confirmed") && !documentSnapshot.getBoolean("reached_destn") ){
                            confirmPickupBtn.setVisibility(View.VISIBLE);
                            pickupStatus.setTextColor(Color.BLACK);
                            reachedBtn.setVisibility(View.INVISIBLE);
                            cancelDrive.setVisibility(View.INVISIBLE);
                        }

                        if(!documentSnapshot.getBoolean("reached_destn") && documentSnapshot.getBoolean("pickup_confirmed")){
                            confirmPickupBtn.setVisibility(View.INVISIBLE);
                            reachedBtn.setVisibility(View.VISIBLE);
                            completeDrive.setTextColor(Color.BLACK);
                            pickupStatus.setTextColor(Color.BLACK);
                            cancelDrive.setVisibility(View.INVISIBLE);
                        }

                        if(documentSnapshot.getBoolean("reached_destn") && documentSnapshot.getBoolean("pickup_confirmed")){
                            confirmPickupBtn.setVisibility(View.INVISIBLE);
                            reachedBtn.setVisibility(View.INVISIBLE);
                            completeDrive.setTextColor(Color.BLACK);
                            pickupStatus.setTextColor(Color.BLACK);
                            cancelDrive.setVisibility(View.VISIBLE);
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


    public void setDistanceandFare(){
        db.collection("drivers").document(auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){
                            drive_fare = task.getResult().get("drive_fare").toString();
                            drive_distance = task.getResult().get("drive_km").toString();
                            distanceTxt.setText(drive_distance);
                            fareTxt.setText(drive_fare);
                        }

                    }
                });
    }


    public void onDriveComplete(View view){
        progressBar.setVisibility(View.VISIBLE);
        cancelDrive.setEnabled(false);
        cancelDriveTxt.setText("Please wait");
        db.collection("drivers").document(auth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                db.collection("status").document(auth.getCurrentUser().getUid())
                        .update("role","user","busy",false)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SharedPreferences geoPoints = getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = geoPoints.edit();
                                editor.putString("from_loc_lat",""+"");
                                editor.putString("from_loc_long",""+"");
                                editor.putString("to_loc_lat",""+"");
                                editor.putString("to_loc_long",""+"");
                                editor.putString("role","user");
                                editor.putBoolean("busy",false);
                                editor.commit();

                                db.collection("drivers").document(auth.getCurrentUser().getUid())
                                        .update("drive_status","not_driving","drive_for","");

                                db.collection("riderequests").document(auth.getCurrentUser().getUid()).delete();

                                try{
                                    db.collection("riders").document(drive_for_id).update("ride_status","not_riding","ride_with","");
                                    db.collection("ongoingrides").document(getRoomId(auth.getCurrentUser().getUid(),drive_for_id)).delete();
                                }catch (Exception e){

                                }

                                addHistory();

                                Intent intent = new Intent(getApplicationContext(),SplashScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                finishAffinity();

                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void addHistory(){
        Map<String,Object> history = new HashMap<>();
        history.put("from",drive_from);
        history.put("to",drive_to);
        history.put("driver",auth.getCurrentUser().getUid());
        history.put("rider",drive_for_id);
        history.put("fare",drive_fare);
        history.put("rating",0);
        history.put("vehicle",drive_vehicle);
        history.put("distance",drive_distance);




        db.collection("completedrive").document(auth.getCurrentUser().getUid()).collection("completedrive").add(history)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        db.collection("completedrive").document(auth.getCurrentUser().getUid()).collection("completedrive")
                                .document(documentReference.getId()).update("key",documentReference.getId());

                        Map<String,Object> rating = new HashMap<>();
                        rating.put("rated",false);
                        rating.put("rating",0);
                        rating.put("rate_for",auth.getCurrentUser().getUid());
                        rating.put("rate_key",documentReference.getId());
                        rating.put("rate_for_name",auth.getCurrentUser().getDisplayName());

                        db.collection("rating").document(drive_for_id).set(rating);

                    }
                });

        db.collection("completedrides").document(drive_for_id).collection("completedrides").add(history)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        db.collection("completedrides").document(drive_for_id).collection("completedrides")
                                .document(documentReference.getId()).update("key",documentReference.getId());

                        Map<String,Object> rating = new HashMap<>();
                        rating.put("rated",false);
                        rating.put("rating",0);
                        rating.put("rate_for",auth.getCurrentUser().getUid());
                        rating.put("rate_key",documentReference.getId());
                        rating.put("rate_for_name",auth.getCurrentUser().getDisplayName());

                        db.collection("rating").document(auth.getCurrentUser().getUid()).set(rating);

                    }
                });

    }




}
