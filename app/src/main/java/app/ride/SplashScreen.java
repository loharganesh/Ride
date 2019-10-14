package app.ride;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import app.ride.Auth.SignIn;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth auth;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateUI(FirebaseUser currentUser) {

        if(currentUser != null){
            SharedPreferences prefs = getSharedPreferences("location", MODE_PRIVATE);
            Boolean busy = prefs.getBoolean("busy",true);//"No name defined" is the default value.
            String role = prefs.getString("role", "user"); //0 is the default value.


            if(busy && role.equals("driver")){
                startActivity(new Intent(SplashScreen.this, Drive.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
                finish();
            }else if(busy && role.equals("rider")){
                startActivity(new Intent(SplashScreen.this, Ride.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
                finish();
            }else{
                startActivity(new Intent(SplashScreen.this, Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
                finish();
            }
        }else{
            startActivity(new Intent(this, SignIn.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
            finish();
        }

        /*if (currentUser != null) {

            FirebaseFirestore.getInstance().collection("status").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                if(task.getResult().getBoolean("busy") && task.getResult().getString("role").equals("driver")){
                                    startActivity(new Intent(SplashScreen.this, Drive.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
                                    finish();
                                }else if(task.getResult().getBoolean("busy") && task.getResult().getString("role").equals("rider")){
                                    startActivity(new Intent(SplashScreen.this, Ride.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
                                    finish();
                                }else{
                                    startActivity(new Intent(SplashScreen.this, Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
                                    finish();
                                }
                            }
                        }
                    });
        } else {
            startActivity(new Intent(SplashScreen.this, SignIn.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
            finish();
        }*/

    }
}
