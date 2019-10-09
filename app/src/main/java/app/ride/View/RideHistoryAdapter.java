package app.ride.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ride.Model.History;
import app.ride.Model.Vehicle;
import app.ride.R;
import app.ride.SelectVehicle;
import app.ride.SplashScreen;

public class RideHistoryAdapter extends RecyclerView.Adapter{

    private Context context;
    private List<History> historyList;

    public RideHistoryAdapter(Context context, List<History> req){
        this.historyList = req;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_history_card, parent, false);
        HistoryHolder vh = new HistoryHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        History history = historyList.get(position);

        FirebaseFirestore.getInstance().collection("users").document(history.getDriver())
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    ((HistoryHolder)holder).driverName.setText(task.getResult().getString("name"));
                }
            });


        FirebaseFirestore.getInstance().collection("vehicles").document(history.getDriver()).collection("vehicles").document(history.getVehicle())
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    ((HistoryHolder)holder).vehicleName.setText(task.getResult().getString("vehicle_number")+"  "+task.getResult().getString("vehicle_name"));
                }
            });

        ((HistoryHolder)holder).fare.setText(history.getFare()+" Rs.");
        ((HistoryHolder)holder).locationTxt.setText("from "+history.getFrom()+" to "+history.getTo());



    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }


    public class HistoryHolder extends RecyclerView.ViewHolder {

        private TextView driverName,vehicleName,fare,locationTxt;

        public HistoryHolder(View itemView) {
            super(itemView);

            driverName = itemView.findViewById(R.id.driverName);
            vehicleName = itemView.findViewById(R.id.ridevehicleNumber);
            fare = itemView.findViewById(R.id.fare);
            locationTxt = itemView.findViewById(R.id.ridefromdestn);

        }
    }
}

