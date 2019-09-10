package app.ride.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class Register extends AppCompatActivity {

    private EditText email,name,password;
    private Button loginBtn;
    private FirebaseAuth mAuth;
    private LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Create Account");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = findViewById(R.id.emailFeild);
        name = findViewById(R.id.nameFeild);
        password = findViewById(R.id.passwordFeild);

        loginBtn = findViewById(R.id.loginBtn);

        progressBar = findViewById(R.id.progressBar);
        
        //FIREBASE
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(email.getText().toString(),password.getText().toString());
            }
        });


    }


    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        setFeildsEnable(false);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //INSERT NAME EMAIL AND PASSWORD IN MYSQL

                              updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            updateUI(null);
                            setFeildsEnable(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        if (!task.isSuccessful()) {
                            Toast.makeText(Register.this, "Authentication Failed! , Please try again", Toast.LENGTH_SHORT).show();
                        }

                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }


    //FORM VALIDATION
    private boolean validateForm() {
        boolean valid = true;

        String emailstr = email.getText().toString();
        if (TextUtils.isEmpty(emailstr)) {
            email.setError("Enter Email");
            valid = false;
        }else if (!isValidEmail(emailstr)) {
            email.setError("Enter Valid Email");
            valid = false;
        }else {
            email.setError(null);
        }

        String namestr = name.getText().toString();
        if (TextUtils.isEmpty(namestr)) {
            name.setError("Name Required");
            valid = false;
        } else {
            name.setError(null);
        }

        String passwordstr = password.getText().toString();
        if (TextUtils.isEmpty(passwordstr)) {
            password.setError("Set Password");
            valid = false;
        }else if(password.length()<5){
            password.setError("Password should at least 6 characters");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    //UI CONTROL METHODS
    public void setFeildsEnable(boolean val){
        password.setEnabled(val);
        email.setEnabled(val);
        name.setEnabled(val);
        loginBtn.setEnabled(val);
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            Intent i1 = new Intent(Register.this, Home.class);
            i1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i1.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i1);
            finish();
        }else{

        }
    }


}
