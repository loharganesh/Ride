package app.ride.Auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import app.ride.R;
import app.ride.SplashScreen;


public class SignIn extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private LinearLayout signInBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        signInBtn = findViewById(R.id.signinBtn);

        db = FirebaseFirestore.getInstance();

        //----------------------------------GOOGLE SIGN IN CONFIGURATION----------------------------------------------------

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //--------------------------------------------------------------------------------------------------------------------

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInBtn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                signIn();
            }
        });

    }


    //=========================================AUTH START WITH GOOGLE=====================================================

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        // [START_EXCLUDE silent]

        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            isUserExists(user.getUid());
                            db.collection("users")
                                    .document(auth.getCurrentUser().getUid()).update("image",acct.getPhotoUrl().toString());
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void updateUI(FirebaseUser firebaseUser){
        if(firebaseUser != null){
            Intent i = new Intent(this, SplashScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            progressBar.setVisibility(View.INVISIBLE);
            signInBtn.setEnabled(true);
            finish();
        }else{
            progressBar.setVisibility(View.INVISIBLE);
            signInBtn.setEnabled(true);
        }
    }


    //UPLOADING USER INFO
    public void uploadUserInfo(FirebaseUser curruntUser){
        if(curruntUser != null){
            final FirebaseUser user = auth.getCurrentUser();

            Map<String,Object> usr = new HashMap<>();
            usr.put("name",user.getDisplayName());
            usr.put("email",user.getEmail());
            usr.put("phone","");
            usr.put("drive_rating",0);
            usr.put("image",user.getPhotoUrl().toString());

            final Map<String,Object> status = new HashMap<>();
            status.put("busy",false);
            status.put("role","user");

            SharedPreferences geoPoints = getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = geoPoints.edit();
            editor.putString("from_loc_lat","");
            editor.putString("from_loc_long","");
            editor.putString("to_loc_lat","");
            editor.putString("to_loc_long","");
            editor.putString("role","user");
            editor.putBoolean("busy",false);
            editor.commit();

            db.collection("users").document(curruntUser.getUid())
                .set(usr)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    db.collection("status").document(auth.getCurrentUser().getUid()).set(status)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    updateUI(user);
                                }else{

                                }
                            }
                        });
                    }
                });
        }
    }

    public void isUserExists(String uid){
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        updateUI(auth.getCurrentUser());
                    } else {
                        uploadUserInfo(auth.getCurrentUser());
                    }
                } else {

                }
            }
        });
    }

}