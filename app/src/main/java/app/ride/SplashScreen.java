package app.ride;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth auth;
    private static int TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUI(currentUser);
            }
        },TIME_OUT);*/


    }


    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            startActivity(new Intent(this, Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();

        } else {
            startActivity(new Intent(this, WelcomeScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }

    }
}
