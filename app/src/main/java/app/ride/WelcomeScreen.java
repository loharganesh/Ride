package app.ride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import app.ride.Auth.Login;
import app.ride.Auth.Register;

public class WelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
    }

    public void login(View view){
        startActivity(new Intent(WelcomeScreen.this, Login.class));
    }

    public void createAc(View view){
        startActivity(new Intent(WelcomeScreen.this, Register.class));
    }

}
