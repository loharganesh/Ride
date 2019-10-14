package app.ride;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class Home extends AppCompatActivity {

    private RadioGroup selectRoleGrp;
    private LinearLayout continueBtn;
    private RadioButton radioButton;

    private LinearLayout fromInp, toInp;

    private String from_location_str = "";
    private String to_location_str = "";
    private String from_loc_name = "";
    private String to_loc_name = "";


    private String location = "";

    private TextView fromLocationTxt, toLocationTxt;
    private ProgressBar progressBar;


    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String location_name;

    private LinearLayout mobileVerified;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MobileVerified();

        selectRoleGrp = findViewById(R.id.selectRoleRadioBtn);
        continueBtn = findViewById(R.id.continueBtn);
        progressBar = findViewById(R.id.progressBarhome);

        fromInp = findViewById(R.id.fromLocationInp);
        toInp = findViewById(R.id.toLocationInp);

        mobileVerified = findViewById(R.id.mobileVerifiedLayout);

        fromLocationTxt = findViewById(R.id.fromLocationTxt);
        toLocationTxt = findViewById(R.id.toLocationTxt);


        fromInp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), SelectLocation.class).putExtra("from", "fromLocation"), 1);
            }
        });

        toLocationTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), SelectLocation.class).putExtra("from", "toLocation"), 1);
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get selected radio button from radioGroup
                int selectedId = selectRoleGrp.getCheckedRadioButtonId();

                try {
                    // find the radiobutton by returned id
                    radioButton = findViewById(selectedId);

                    if (radioButton.getText().toString().equals("RIDE") && !TextUtils.isEmpty(from_location_str) && !TextUtils.isEmpty(to_location_str)) {

                        progressBar.setVisibility(View.VISIBLE);

                        double  from_loc_lat = Double.parseDouble(from_location_str.substring(0,from_location_str.indexOf("+")));
                        double  from_loc_long = Double.parseDouble(from_location_str.substring(from_location_str.indexOf("+")));

                        GeoPoint from_location = new GeoPoint(from_loc_lat,from_loc_long);

                        double  to_loc_lat = Double.parseDouble(to_location_str.substring(0,to_location_str.indexOf("+")));
                        double  to_loc_long = Double.parseDouble(to_location_str.substring(to_location_str.indexOf("+")));

                        GeoPoint to_location = new GeoPoint(to_loc_lat,to_loc_long);

                        Map<String,Object> ride = new HashMap<>();
                        ride.put("rider_key",FirebaseAuth.getInstance().getCurrentUser().getUid());
                        ride.put("ride_from",from_location);
                        ride.put("ride_to",to_location);
                        ride.put("ride_status","not_riding");
                        ride.put("ride_with","");
                        ride.put("origin",from_loc_name);
                        ride.put("destination",to_loc_name);
                        ride.put("ride_route_link",from_loc_lat+""+from_loc_long+""+to_loc_lat+""+to_loc_long);

                        SharedPreferences geoPoints = getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = geoPoints.edit();
                        editor.putString("from_loc_lat",""+from_loc_lat);
                        editor.putString("from_loc_long",""+from_loc_long);
                        editor.putString("to_loc_lat",""+to_loc_lat);
                        editor.putString("to_loc_long",""+to_loc_long);
                        editor.putString("origin",from_loc_name);
                        editor.putString("destination",to_loc_name);
                        editor.putString("role","rider");
                        editor.putBoolean("busy",true);
                        editor.commit();

                        db.collection("riders").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(ride)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        FirebaseFirestore.getInstance().collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .update("busy",true,"role","driver")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Map<String,Object> rating = new HashMap<>();
                                                        rating.put("rated",true);
                                                        rating.put("rating",0);
                                                        rating.put("rate_for","");
                                                        rating.put("rate_for_name","");

                                                        db.collection("rating").document(auth.getCurrentUser().getUid()).set(rating);

                                                        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                        startActivity(intent);
                                                        finishAffinity();
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                });



                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });


                    } else if (radioButton.getText().toString().equals("DRIVE") && !TextUtils.isEmpty(from_location_str) && !TextUtils.isEmpty(to_location_str)) {
                        checkVehicles();
                    } else {
                        Toast.makeText(Home.this, "Select your role and location properly", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(Home.this, "Select your role and location properly", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }


    public void showProfile(View view){
        startActivity(new Intent(this,Profile.class));
    }
    public void addMobile(View view){
        startActivity(new Intent(this,VerifyMobile.class));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                location = data.getStringExtra("location");
                location_name = data.getStringExtra("location_name");

                String for_str = data.getStringExtra("for");

                if (for_str.equals("fromLocation")) {
                    from_location_str = location;
                    from_loc_name = location_name;
                    fromLocationTxt.setText(location_name);
                } else {
                    to_location_str = location;
                    to_loc_name = location_name;
                    toLocationTxt.setText(location_name);
                }


            }
        }
    }


    public void checkVehicles() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("vehicles").document(auth.getCurrentUser().getUid()).collection("vehicles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().isEmpty()){
                            startActivity(new Intent(getApplicationContext(),AddVehicle.class));
                            progressBar.setVisibility(View.INVISIBLE);
                        }else{
                            startActivity(new Intent(getApplicationContext(),SelectVehicle.class)
                                    .putExtra("fromlocation",from_location_str)
                                    .putExtra("tolocation",to_location_str)
                                    .putExtra("origin_name",from_loc_name)
                                    .putExtra("destination_name",to_loc_name));
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    public void MobileVerified(){
        db.collection("users").document(auth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.getString("phone").equals("")){
                            mobileVerified.setVisibility(View.VISIBLE);
                        }else{
                            mobileVerified.setVisibility(View.GONE);
                        }
                    }
                });
    }


}