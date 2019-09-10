package app.ride.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import app.ride.Home;
import app.ride.R;

import static app.ride.Auth.Register.isValidEmail;

public class Login extends AppCompatActivity {

    private Button loginBtn;
    private LinearLayout progressBar;
    private EditText emailFeild;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        //REFERENCING COMPONENTS
        emailFeild = findViewById(R.id.emailFeild);
        passwordField = findViewById(R.id.passwordFeild);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               signInWithEmail(emailFeild.getText().toString(),passwordField.getText().toString());
            }
        });

    }

    //=========================================AUTH START WITH EMAIL PASSWORD=====================================================
    private void signInWithEmail(String email, String password) {
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        setFeildsEnable(false);

        // [START create_user_with_email]
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            updateUI(null);
                            setFeildsEnable(true);
                            progressBar.setVisibility(View.GONE);

                        }
                        if (!task.isSuccessful()) {
                            Toast.makeText(Login.this, "Authentication Failed! , Please try again", Toast.LENGTH_SHORT).show();
                        }
                        //progressBar.setVisibility(View.INVISIBLE);
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    public void updateUI(FirebaseUser firebaseUser){
        if(firebaseUser != null){
            Intent i = new Intent(Login.this, Home.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            progressBar.setVisibility(View.INVISIBLE);
            finish();
        }else{

        }
    }

    //FORM VALIDATION
    private boolean validateForm() {
        boolean valid = true;

        String email = emailFeild.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailFeild.setError("Enter Email");
            valid = false;
        }else if(!isValidEmail(email)){
            emailFeild.setError("Enter Valid Email");
            valid = false;
        }else {
            emailFeild.setError(null);
        }

        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Enter Password");
            valid = false;
        }else if(password.length()<5){
            passwordField.setError("Password should atleast 6 characters");
            valid = false;
        } else {
            passwordField.setError(null);
        }

        return valid;
    }

    //UI CONTROL METHODS
    public void setFeildsEnable(boolean val){
        passwordField.setEnabled(val);
        emailFeild.setEnabled(val);
        loginBtn.setEnabled(val);
    }


}
