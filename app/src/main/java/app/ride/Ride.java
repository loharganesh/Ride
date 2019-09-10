package app.ride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Ride extends AppCompatActivity {

    private LinearLayout reqLayout;
    private Button rideBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Take your ride");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reqLayout = findViewById(R.id.reqLayout);
        rideBtn = findViewById(R.id.rideBtn);



    }

    public void onRequest(View view){
        reqLayout.setVisibility(View.VISIBLE);
        reqLayout.animate().setDuration(500).alpha(1f).start();
        rideBtn.setText("Check your ride details");
    }
}
