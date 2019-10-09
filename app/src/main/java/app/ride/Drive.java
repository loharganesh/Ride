package app.ride;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import app.ride.Model.Driver;
import app.ride.Model.Requests;
import app.ride.Model.Vehicle;
import app.ride.View.RequestAdapter;

import static app.ride.Constants.MAP_BUNDLE_KEY;
import static app.ride.Constants.createCustomMarker;
import static app.ride.Constants.getMarkerIconFromDrawable;
import static app.ride.Constants.getRoomId;

public class Drive extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LinearLayout reqLayout;
    private Button rideBtn;

    private MapView mapView;
    private GoogleMap map;

    private String routeUrl;

    private String vehicle_key;
    private GeoPoint from_location,to_location;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Vehicle vehicle;
    private Driver driver;
    private SharedPreferences location;

    private RecyclerView requestsList;
    private RequestAdapter requestAdapter;
    private List<Requests> reqList;

    private LinearLayout listsLayout;
    private LinearLayout drivingLayout;

    private TextView driveForNameTxt,driveLocationTxt;
    private Button manageDriveBtn;
    private String drive_for_id;
    private ProgressBar progressBar;
    private String from_location_name;
    private String to_location_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        mapView = findViewById(R.id.mapView);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        driveForNameTxt = findViewById(R.id.driveForUsername);
        driveLocationTxt = findViewById(R.id.driveLocation);
        manageDriveBtn = findViewById(R.id.manageDriveBtn);
        drivingLayout = findViewById(R.id.driveLayout);
        listsLayout = findViewById(R.id.listLayout);
        progressBar = findViewById(R.id.progressBar4);

        reqList = new ArrayList<>();
        requestsList = findViewById(R.id.requestsList);
        requestAdapter = new RequestAdapter(Drive.this,reqList);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(requestsList);
        requestsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        requestsList.setAdapter(requestAdapter);

//      requestAdapter.notifyDataSetChanged();

        getDriverDetails();

        initMap(savedInstanceState);


        getRequests();

        setupDriveUI();


    }

    public void initMap(Bundle savedInstance){
        Bundle mapViewBundle = null;
        if (savedInstance != null) {
            mapViewBundle = savedInstance.getBundle(MAP_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    public void fillRequestsList(){

    }


    //INIRTIATINF ROUTE BETWEEN LOCATIONS
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        LatLng origin = new LatLng(from_location.getLatitude(),from_location.getLongitude());
        LatLng destination = new LatLng(to_location.getLatitude(),to_location.getLongitude());
        //routeUrl = getDirectionsUrl(origin,destination);

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
        origin_marker.icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(Drive.this,"◯  "+from_location_name)));

        MarkerOptions destination_marker = new MarkerOptions();
        destination_marker.position(destination);
        destination_marker.icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(Drive.this,"☐ "+to_location_name)));

        Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> origin_address;
        List<Address> destn_address;
        try {
            origin_address = geocoder.getFromLocation(from_location.getLatitude(), from_location.getLongitude(), 1);
            destn_address = geocoder.getFromLocation(to_location.getLatitude(), to_location.getLongitude(), 1);
            String feature_name_origin = origin_address.get(0).getAddressLine(0);
            //origin_marker.title(feature_name_origin).visible(true);
            String feature_name_destn = destn_address.get(0).getAddressLine(0);
            //destination_marker.title(feature_name_destn).visible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        map.addMarker(origin_marker);
        map.addMarker(destination_marker);

        Polyline line = map.addPolyline(new PolylineOptions()
                .add(origin,destination)
                .width(5)
                .color(Color.LTGRAY));

        showCurvedPolyline(origin,destination,3);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin,13));

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void showCurvedPolyline (LatLng p1, LatLng p2, double k) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1,p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        //Midpoint position
        LatLng p = SphericalUtil.computeOffset(p1, d*0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1-k*k)*d*0.5/(2*k);
        double r = (1+k*k)*d*0.5/(2*k);

        LatLng c = SphericalUtil.computeOffset(p, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, p1);
        double h2 = SphericalUtil.computeHeading(c, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 100;
        double step = (h2 -h1) / numpoints;

        for (int i=0; i < numpoints; i++) {
            LatLng pi = SphericalUtil.computeOffset(c, r, h1 + i * step);
            options.add(pi);
        }

        //Draw polyline
        map.addPolyline(options.width(5).color(Color.BLACK).geodesic(false).pattern(pattern));
    }


    public void getDriverDetails(){

        location = getSharedPreferences("location",
                Context.MODE_PRIVATE);

        from_location = new GeoPoint(Double.parseDouble(location.getString("from_loc_lat","default")),Double.parseDouble(location.getString("from_loc_long","default")));
        to_location = new GeoPoint(Double.parseDouble(location.getString("to_loc_lat","default")),Double.parseDouble(location.getString("to_loc_long","default")));

        from_location_name = location.getString("origin","");
        to_location_name = location.getString("destination","");

        /*db.collection("Drivers").document(auth.getCurrentUser().getUid())
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.getResult().exists()){
                        driver = task.getResult().toObject(Driver.class);
                        from_location = driver.getDrive_from();
                        to_location = driver.getDrive_to();
                        db.collection("Vehicles").document(driver.getDriver_key()).collection("Vehicles")
                            .document(driver.getDriver_vehicle())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.getResult().exists()){
                                        vehicle = task.getResult().toObject(Vehicle.class);
                                    }
                                }
                            });
                    }
                }
            });*/
    }

    public void onManageDrive(View view){
        startActivity(new Intent(Drive.this,ManageDrive.class)
                .putExtra("driveFor",drive_for_id)
                .putExtra("drive_from",from_location_name)
                .putExtra("drive_to",to_location_name)
                .putExtra("drive_vehicle",vehicle_key));

    }

    public void getRequests(){
        db.collection("riderequests").document(auth.getCurrentUser().getUid()).collection("riderequests").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                reqList.clear();
                if(!queryDocumentSnapshots.isEmpty()){
                    for(DocumentSnapshot snap:queryDocumentSnapshots.getDocuments()){
                        Requests req = snap.toObject(Requests.class);
                        reqList.add(req);
                    }
                }else{
                    Snackbar snackbar = Snackbar
                            .make(requestsList, "No ride requests yet", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                requestAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setupDriveUI(){
        db.collection("drivers").document(auth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(documentSnapshot.exists()){
                            String drive_status = documentSnapshot.getString("drive_status");
                            final String req_rider_id = documentSnapshot.getString("drive_for");

                            vehicle_key = documentSnapshot.getString("driver_vehicle");
                            drive_for_id = req_rider_id;

                            if(drive_status.equals("driving")){

                                listsLayout.setVisibility(View.GONE);
                                drivingLayout.setVisibility(View.VISIBLE);

                                db.collection("users").document(drive_for_id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                driveForNameTxt.setText(task.getResult().getString("name"));
                                            }
                                        });

                            }else if(drive_status.equals("not_driving")){
                                listsLayout.setVisibility(View.VISIBLE);
                                drivingLayout.setVisibility(View.GONE);
                            }else{

                            }
                        }else{

                        }



                    }
                });
    }

    public void showDriveHistory(View view){
        startActivity(new Intent(Drive.this,RecentActivities.class));
    }

    public void cancelMyDrive(View view){
        progressBar.setVisibility(View.VISIBLE);
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

                                /*db.collection("riderequests").document(auth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(Drive.this, "Request Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });*/

                                try{
                                    db.collection("riders").document(drive_for_id).update("ride_status","not_riding","ride_with","");
                                    db.collection("ongoingrides").document(getRoomId(auth.getCurrentUser().getUid(),drive_for_id)).delete();
                                }catch (Exception e){

                                }


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

}
