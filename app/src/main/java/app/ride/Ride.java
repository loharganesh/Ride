package app.ride;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import app.ride.Model.Driver;
import app.ride.View.DriversAdapter;

import static app.ride.Constants.MAP_BUNDLE_KEY;
import static app.ride.Constants.createCustomMarker;
import static app.ride.Constants.getRoomId;

public class Ride extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView driversRecyclerView;
    private List<Driver> driversList;
    private DriversAdapter driverAdapter;
    private GoogleMap map;
    private MapView mapView;

    private String location_link = "";
    private GeoPoint from_location, to_location;
    private SharedPreferences location;
    private ProgressBar progressBar, progressBarRide;
    private LinearLayout waitingForConfirmation, listsLayout,rideCompleteLayout;
    private LinearLayout ratingLayout;
    private RatingBar ratingBar;
    private TextView messageTxt;

    private String requested_to;
    private List<Marker> markers = new ArrayList<>();

    private TextView ridingWithName, ridingVehicleName, ridingVehicleNumber;
    private LinearLayout ridingLayout;
    private Button manageRideBtn;

    private String from_location_name;
    private String to_location_name;

    private Polyline currentPolyline;
    private String rateforid;
    private String rate_key;

    private ProgressBar callingProgressBar;

    private ImageView callBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar3);
        progressBarRide = findViewById(R.id.progressBarRide);
        waitingForConfirmation = findViewById(R.id.waitingForConfirmation);
        listsLayout = findViewById(R.id.listLayout);

        ratingLayout = findViewById(R.id.ratingLayout);
        ratingBar = findViewById(R.id.ratingBar);
        messageTxt = findViewById(R.id.messageTxt);

        callBtn = findViewById(R.id.callBtn);

        callingProgressBar = findViewById(R.id.callingProgressBar);

        getRatingStatus();

        getRideDetails();

        ridingLayout = findViewById(R.id.ridingLayout);
        ridingWithName = findViewById(R.id.ridingWithName);
        ridingVehicleName = findViewById(R.id.ridingVehicleName);
        ridingVehicleNumber = findViewById(R.id.ridingVehicleNumber);
        manageRideBtn = findViewById(R.id.rideActBtn);

        rideCompleteLayout = findViewById(R.id.rideCompleteLayout);

        mapView = findViewById(R.id.mapView2);
        initMap(savedInstanceState);


        driversList = new ArrayList<>();
        driversRecyclerView = findViewById(R.id.driversList);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(driversRecyclerView);
        driversRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        driverAdapter = new DriversAdapter(this, driversList);
        driversRecyclerView.setAdapter(driverAdapter);


        setLayouts();




    }

    public void showRideActivity(View view) {
        startActivity(new Intent(Ride.this, ManageRide.class).putExtra("driver_key", requested_to));
    }



    public void rateDriver(View view) {
        if (ratingBar.getRating() > 0) {
            db.collection("completedrive").document(rateforid).collection("completedrive").document(rate_key).update("rating", ratingBar.getRating());
            //db.collection("completedrides").document(auth.getCurrentUser().getUid()).collection("completedrides").document(rateforid).update("rating",ratingBar.getRating());

           // Toast.makeText(this, "" + rateforid, Toast.LENGTH_SHORT).show();

            db.collection("completedrive").document(rateforid).collection("completedrive")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.getResult().isEmpty()) {
                                int total_rating = 0;
                                int no_of_rides = 0;
                                for (DocumentSnapshot snapshot : task.getResult()) {
                                    no_of_rides++;
                                    long temp = snapshot.getLong("rating");
                                    total_rating = total_rating + (int) temp;
                                    //Toast.makeText(Ride.this, "Rating : "+snapshot.getLong("rating"), Toast.LENGTH_SHORT).show();;
                                }
                                int average = total_rating / no_of_rides;
                                //Toast.makeText(Ride.this, "No. of Rides : "+no_of_rides, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(Ride.this, "Average Rating : "+average, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(Ride.this, "" + rateforid, Toast.LENGTH_SHORT).show();
                                db.collection("users").document(rateforid).update("drive_rating", average)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                db.collection("rating").document(auth.getCurrentUser().getUid()).update("rated", true, "rate_for", "", "rate_for_name", "");
                                            }
                                        });
                            }
                        }
                    });

        } else {
            Toast.makeText(this, "Rate Driver", Toast.LENGTH_SHORT).show();
        }
    }

    public void getRatingStatus() {
        db.collection("rating").document(auth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        boolean rated = documentSnapshot.getBoolean("rated");
                        String username = documentSnapshot.getString("rate_for_name");
                        String rate_for_id = documentSnapshot.getString("rate_for");

                        rateforid = rate_for_id;
                        rate_key = documentSnapshot.getString("rate_key");

                        if (!rated) {
                            ratingLayout.setVisibility(View.VISIBLE);
                            rideCompleteLayout.setVisibility(View.GONE);
                            messageTxt.setText(Html.fromHtml("How was your ride with <b><u>" + username));
                        } else {
                            ratingLayout.setVisibility(View.GONE);
                        }

                    }
                });
    }

    public void getDrivers(final String route) {
        db.collection("drivers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    driversList.clear();
                    for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                        Driver req = snap.toObject(Driver.class);

                        if (req.getDrive_route_link().equals(route) && !req.isBusy()) {
                            driversList.add(req);
                        }
                    }
                }else{
                    Snackbar snackbar = Snackbar
                            .make(driversRecyclerView, "No rides available", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                driverAdapter.notifyDataSetChanged();
            }
        });
    }

    public RecyclerView getList()
    {
        return this.driversRecyclerView;
    }

    public void initMap(Bundle savedInstance) {
        Bundle mapViewBundle = null;
        if (savedInstance != null) {
            mapViewBundle = savedInstance.getBundle(MAP_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    public void loadDrivers() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        //routeUrl = getDirectionsUrl(origin,destination);
        LatLng origin = new LatLng(from_location.getLatitude(), from_location.getLongitude());
        LatLng destination = new LatLng(to_location.getLatitude(), to_location.getLongitude());
        //SETTING CUSTOM THEME TO MAP
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.ride_custom_map));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        //BitmapDescriptor origin_marker_icon = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_map_origin_marker));
        //BitmapDescriptor destination_marker_icon = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_map_destn_marker));

        MarkerOptions origin_marker = new MarkerOptions();
        origin_marker.position(origin);
        origin_marker.icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(Ride.this, "◯  " + from_location_name)));

        MarkerOptions destination_marker = new MarkerOptions();
        destination_marker.position(destination);
        destination_marker.icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(Ride.this, "☐  " + to_location_name)));

        Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> origin_address;
        List<Address> destn_address;
        try {
            origin_address = geocoder.getFromLocation(origin.latitude, origin.longitude, 1);
            destn_address = geocoder.getFromLocation(destination.latitude, destination.longitude, 1);
            String feature_name_origin = origin_address.get(0).getAddressLine(0);
            //destination_marker.title(feature_name_origin);
            String feature_name_destn = destn_address.get(0).getAddressLine(0);
            //destination_marker.title(feature_name_destn);

        } catch (IOException e) {
            e.printStackTrace();
        }


        map.addMarker(origin_marker);
        map.addMarker(destination_marker);

        Polyline line = map.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(5)
                .color(Color.LTGRAY));

        showCurvedPolyline(origin, destination, 3);


        //move map camera
        //map.moveCamera(CameraUpdateFactory.newLatLng(origin));
        //map.animateCamera(CameraUpdateFactory.zoomTo(13));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 10));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        callingProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void showRideHistory(View view) {
        startActivity(new Intent(Ride.this, RecentActivities.class));
    }


    private void showCurvedPolyline(LatLng p1, LatLng p2, double k) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1, p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        //Midpoint position
        LatLng p = SphericalUtil.computeOffset(p1, d * 0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - k * k) * d * 0.5 / (2 * k);
        double r = (1 + k * k) * d * 0.5 / (2 * k);

        LatLng c = SphericalUtil.computeOffset(p, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, p1);
        double h2 = SphericalUtil.computeHeading(c, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 100;
        double step = (h2 - h1) / numpoints;

        for (int i = 0; i < numpoints; i++) {
            LatLng pi = SphericalUtil.computeOffset(c, r, h1 + i * step);
            options.add(pi);
        }

        //Draw polyline
        map.addPolyline(options.width(5).color(Color.BLACK).geodesic(false).pattern(pattern));
    }

    public void getRideDetails() {

        location = getSharedPreferences("location",
                Context.MODE_PRIVATE);

        from_location = new GeoPoint(Double.parseDouble(location.getString("from_loc_lat", "default")), Double.parseDouble(location.getString("from_loc_long", "default")));
        to_location = new GeoPoint(Double.parseDouble(location.getString("to_loc_lat", "default")), Double.parseDouble(location.getString("to_loc_long", "default")));

        from_location_name = location.getString("origin", "");
        to_location_name = location.getString("destination", "");

        db.collection("riders").document(auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            getDrivers(task.getResult().getString("ride_route_link"));
                        }
                    }
                });
    }

    public void cancelRide(View view) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("riders").document(auth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                db.collection("status").document(auth.getCurrentUser().getUid())
                        .update("role", "user", "busy", false)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SharedPreferences geoPoints = getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = geoPoints.edit();
                                editor.putString("from_loc_lat", "" + "");
                                editor.putString("from_loc_long", "" + "");
                                editor.putString("to_loc_lat", "" + "");
                                editor.putString("to_loc_long", "" + "");
                                editor.putString("role", "user");
                                editor.putBoolean("busy", false);
                                editor.commit();

                                try {
                                    db.collection("riderequests").document(requested_to).collection("riderequests")
                                            .document(auth.getCurrentUser().getUid()).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });

                                    try {
                                        db.collection("drivers").document(requested_to).update("drive_status", "not_driving", "drive_for", "");
                                    } catch (Exception e) {

                                    }
                                } catch (Exception e) {

                                }


                                FirebaseFirestore.getInstance().collection("riders").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .update("ride_status", "not_riding", "ride_with", "");

                                Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
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


    public void onCancelRide(View view) {

        db.collection("riderequests").document(requested_to).collection("riderequests")
                .document(auth.getCurrentUser().getUid()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseFirestore.getInstance().collection("riders").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .update("ride_status", "not_riding", "ride_with", "");

                    }
                });


    }

    public void setLayouts() {

        db.collection("riders").document(auth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            String ride_status = documentSnapshot.getString("ride_status");
                            final String req_driver_id = documentSnapshot.getString("ride_with");

                            requested_to = req_driver_id;

                            try{
                                RideCompleted(req_driver_id);
                            }catch (Exception e1){

                            }

                            if (ride_status.equals("requested")) {
                                waitingForConfirmation.setVisibility(View.VISIBLE);
                                listsLayout.setVisibility(View.GONE);
                                ridingLayout.setVisibility(View.GONE);
                            } else if (ride_status.equals("not_riding")) {
                                waitingForConfirmation.setVisibility(View.GONE);
                                listsLayout.setVisibility(View.VISIBLE);
                                ridingLayout.setVisibility(View.GONE);
                            } else if (ride_status.equals("riding")) {

                                waitingForConfirmation.setVisibility(View.GONE);
                                listsLayout.setVisibility(View.GONE);

                                SharedPreferences req = getApplicationContext().getSharedPreferences("requeststatus", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = req.edit();
                                editor.putString("req_accepted","true");
                                editor.commit();

                                db.collection("users").document(req_driver_id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                ridingWithName.setText(task.getResult().getString("name"));

                                                db.collection("drivers").document(req_driver_id).get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                db.collection("vehicles").document(req_driver_id).collection("vehicles").document(task.getResult().getString("driver_vehicle"))
                                                                        .get()
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                                                ridingVehicleName.setText(task.getResult().getString("vehicle_name"));
                                                                                ridingVehicleNumber.setText(task.getResult().getString("vehicle_number"));
                                                                                ridingLayout.setVisibility(View.VISIBLE);
                                                                            }
                                                                        });
                                                            }
                                                        });


                                            }
                                        });
                                ridingLayout.setVisibility(View.GONE);
                            } else {

                            }
                        }
                    }
                });
    }

    public void onPhoneClicked(View view) {
        callingProgressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(requested_to).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+91"+task.getResult().getString("phone")));
                        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Ride.this, new String[]{Manifest.permission.CALL_PHONE},1);
                            return;
                        }else{
                            startActivity(intent);
                        }

               }
           });
    }

    public void RideCompleted(String id){
        db.collection("ongoingrides").document(getRoomId(id,auth.getCurrentUser().getUid()))
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getBoolean("reached_destn") && documentSnapshot.getBoolean("pickup_confirmed")){

                        rideCompleteLayout.setVisibility(View.VISIBLE);

                    }
                }
                }
            });
    }

}
