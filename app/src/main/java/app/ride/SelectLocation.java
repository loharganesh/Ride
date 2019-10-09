package app.ride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import app.ride.Model.Location;
import app.ride.View.LocationsAdapter;

public class SelectLocation extends AppCompatActivity {

    private ListView locationsList;
    private LocationsAdapter adapter;
    private EditText locationQuery;

    public static String FROM;

    public static String RESPONSE;

    public static ArrayList<Location> locList = new ArrayList<Location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        FROM = getIntent().getExtras().getString("from");

        initList();

        locationsList = findViewById(R.id.locationsList);
        locationQuery = findViewById(R.id.locationQuery);

        adapter = new LocationsAdapter(this);
        locationsList.setAdapter(adapter);

        locationQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i < i1) {
                    // We're deleting char so we need to reset the adapter data
                    
                    //adapter.notifyDataSetChanged();
                }

                adapter.filter(charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        locationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {

                Location location = locList.get(position);

                Intent intent = new Intent();
                intent.putExtra("location",""+location.getLocation().getLatitude()+"+"+location.getLocation().getLongitude());
                intent.putExtra("for",FROM);
                intent.putExtra("location_name",location.getLocation_name());
                setResult(RESULT_OK, intent);
                finish();



            }
        });

    }

    public void onBack (View view){
        onBackPressed();
    }

    private void initList() {
        // We populate the planets
        FirebaseFirestore.getInstance().collection("Locations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            locList.clear();
                            for (DocumentSnapshot snap:task.getResult()){
                                Location loc = snap.toObject(Location.class);
                                locList.add(loc);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });


    }

    public static void setResponse(String str){
        RESPONSE = str;
    }

    public static String getResponse(){
        return RESPONSE;
    }


}
