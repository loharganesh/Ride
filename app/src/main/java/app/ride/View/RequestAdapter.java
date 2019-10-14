package app.ride.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ride.Model.Requests;
import app.ride.R;

import static app.ride.Constants.getRoomId;

public class RequestAdapter extends RecyclerView.Adapter{

    private Context context;
    private List<Requests> requestsList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public RequestAdapter(Context context,List<Requests> req){
        this.requestsList = req;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_layout, parent, false);
        RequestsHolder vh = new RequestsHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final Requests requests = requestsList.get(position);



        ((RequestsHolder)holder).location.setText("from "+requests.getOrigin()+" to "+requests.getDestination());

        db.collection("users").document(requests.getRider_key())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            ((RequestsHolder)holder).riderName.setText(task.getResult().getString("name"));
                        }
                    }
                });

        ((RequestsHolder)holder).acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("drivers").document(auth.getCurrentUser().getUid())
                .update("drive_status","driving","drive_for",requests.getRider_key(),"busy",true);

                db.collection("riders").document(requests.getRider_key())
                        .update("ride_status","riding","ride_with",requests.getDriver_key());

                db.collection("riderequests").document(auth.getCurrentUser().getUid()).collection("riderequests")
                        .document(requests.getRider_key()).delete();


                Map<String,Object> thread = new HashMap<>();
                thread.put("pickup_confirmed",false);
                thread.put("reached_destn",false);
                thread.put("fare_paid",false);

                db.collection("ongoingrides").document(getRoomId(requests.getDriver_key(),requests.getRider_key()))
                        .set(thread);


            }
        });


    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }
    public class RequestsHolder extends RecyclerView.ViewHolder {
        private TextView riderName,fareAmount,location;
        private Button acceptBtn;
        public RequestsHolder(View itemView) {
            super(itemView);
            riderName = itemView.findViewById(R.id.username);
            fareAmount = itemView.findViewById(R.id.fareAmount);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            location = itemView.findViewById(R.id.locationTxt);
        }
    }

}
