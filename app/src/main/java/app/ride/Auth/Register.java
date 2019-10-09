package app.ride.Auth;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

import app.ride.Home;
import app.ride.R;

public class Register extends AppCompatActivity {

    private EditText emailInp,name,passwordInp,licenseInp;
    private Button loginBtn;
    private FirebaseAuth mAuth;
    private LinearLayout progressBar;
    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        emailInp = findViewById(R.id.emailFeild);
        name = findViewById(R.id.nameFeild);
        passwordInp = findViewById(R.id.passwordFeild);
        //licenseInp = findViewById(R.id.license_no);


        loginBtn = findViewById(R.id.loginBtn);

        progressBar = findViewById(R.id.progressBar);
        
        //FIREBASE
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(emailInp.getText().toString(),passwordInp.getText().toString());
            }
        });


    }


    private void createAccount(final String email, String password) {
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
                            final FirebaseUser user = mAuth.getCurrentUser();

                            Map<String,Object> usr = new HashMap<>();
                            usr.put("name",name.getText().toString());
                            usr.put("email",emailInp.getText().toString());
                            usr.put("password",emailInp.getText().toString());

                            final Map<String,Object> status = new HashMap<>();
                            status.put("busy",false);
                            status.put("status","user");

                            db.collection("users").document(user.getUid())
                                    .set(usr)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            db.collection("status").document(mAuth.getCurrentUser().getUid()).set(status)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                updateUI(mAuth.getCurrentUser());
                                                            }else{

                                                            }
                                                        }
                                                    });
                                            updateUI(user);
                                        }
                                    });

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

        String emailstr = emailInp.getText().toString();
        if (TextUtils.isEmpty(emailstr)) {
            emailInp.setError("Enter Email");
            valid = false;
        }else if (!isValidEmail(emailstr)) {
            emailInp.setError("Enter Valid Email");
            valid = false;
        }else {
            emailInp.setError(null);
        }

        String namestr = name.getText().toString();
        if (TextUtils.isEmpty(namestr)) {
            name.setError("Name Required");
            valid = false;
        } else {
            name.setError(null);
        }

        String passwordstr = passwordInp.getText().toString();
        if (TextUtils.isEmpty(passwordstr)) {
            passwordInp.setError("Set Password");
            valid = false;
        }else if(passwordInp.length()<5){
            passwordInp.setError("Password should at least 6 characters");
            valid = false;
        } else {
            passwordInp.setError(null);
        }

        return valid;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    //UI CONTROL METHODS
    public void setFeildsEnable(boolean val){
        passwordInp.setEnabled(val);
        emailInp.setEnabled(val);
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

    /*public boolean IsValid(String text){

        String pattern = "^(?<intro>[A-Z]{2})(?<numeric>\\d{2})(?<year>\\d{4})(?<tail>\\d{7})$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        if(m.matches())
        {
            System.out.println("Validated");
            return true;
        }
        else
        {
            System.out.println("Not Validated");
            return false;

        }


    }*/


}
