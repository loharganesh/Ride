package app.ride;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class Profile extends AppCompatActivity {

    private TextView usernameTxt,emailTxt,mobileNum;
    private RatingBar ratingBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Profile");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        usernameTxt  = findViewById(R.id.userName);
        emailTxt     = findViewById(R.id.userEmail);
        mobileNum    = findViewById(R.id.phoneNumberTxt);

        usernameTxt.setText(auth.getCurrentUser().getDisplayName());
        emailTxt.setText(auth.getCurrentUser().getEmail());

        ratingBar = findViewById(R.id.driverRatingProfile);

        setRating();

    }

    public void setRating(){
        db.collection("users").document(auth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){
                            ratingBar.setRating(documentSnapshot.getLong("drive_rating"));
                            mobileNum.setText(documentSnapshot.getString("phone"));
                        }
                    }
                });
    }

    public void feedback(View view){
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "lohar2159@gmail.com" });
        Email.putExtra(Intent.EXTRA_SUBJECT, "Regarding to feedback of Ride App");
        Email.putExtra(Intent.EXTRA_TEXT, "" + "");
        startActivity(Intent.createChooser(Email, "Send Feedback"));

    }

    public void showHistory(View view){
        startActivity(new Intent(this, RecentActivities.class));
    }

    public void updatePhone(View view){
        startActivity(new Intent(this, VerifyMobile.class));
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

}
