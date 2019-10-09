package app.ride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import app.ride.Model.Requests;
import app.ride.View.RequestAdapter;

public class RideRequests extends AppCompatActivity {

    private RecyclerView requestsList;
    private RequestAdapter requestAdapter;
    private List<Requests> reqList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_requests);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Ride Requets");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        reqList = new ArrayList<>();
        requestsList = findViewById(R.id.requestsList);
        requestAdapter = new RequestAdapter(RideRequests.this,reqList);
        requestsList.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        requestsList.setAdapter(requestAdapter);

        getRequests();


    }

    public void getRequests(){
        db.collection("riderequests").document(auth.getCurrentUser().getUid()).collection("riderequests").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    reqList.clear();
                    for(DocumentSnapshot snap:queryDocumentSnapshots.getDocuments()){
                        Requests req = snap.toObject(Requests.class);
                        reqList.add(req);
                    }
                }else{
                    Toast.makeText(RideRequests.this, "No ride requests yet", Toast.LENGTH_LONG).show();
                }
                requestAdapter.notifyDataSetChanged();
            }
        });
    }
}
