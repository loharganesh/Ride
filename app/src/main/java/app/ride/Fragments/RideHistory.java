package app.ride.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import app.ride.Model.History;
import app.ride.R;
import app.ride.View.RideHistoryAdapter;

public class RideHistory extends Fragment {

    private RecyclerView recyclerView;
    private List<History> historyList;
    private RideHistoryAdapter rideHistoryAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ride_history, container, false);

        recyclerView = view.findViewById(R.id.rideHistoryRecyclerview);
        historyList = new ArrayList<>();
        rideHistoryAdapter = new RideHistoryAdapter(RideHistory.this.getContext(),historyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(RideHistory.this.getContext(),RecyclerView.VERTICAL,false));
        recyclerView.setAdapter(rideHistoryAdapter);

        getRideHistory();

        return view;
    }

    public void getRideHistory(){
        FirebaseFirestore.getInstance().collection("completedrides").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("completedrides")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot snapshot:task.getResult()){
                                History history = snapshot.toObject(History.class);
                                historyList.add(history);
                            }
                            rideHistoryAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

}
