package app.ride;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class VerifyMobile extends AppCompatActivity {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private Button verifyBtn;
    private ProgressBar progressBar;

    private EditText mobileNumInp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);


        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Verify your mobile number");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        verifyBtn = findViewById(R.id.verifyBtn);
        progressBar =  findViewById(R.id.verifyingNumber);
        mobileNumInp = findViewById(R.id.mobileNumberInp);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(mobileNumInp.getText().toString())){
                    progressBar.setVisibility(View.VISIBLE);
                    verifyBtn.setText("Verifying");
                    startPhoneNumberVerification("+91"+mobileNumInp.getText().toString());
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Toast.makeText(VerifyMobile.this, "Number Verified", Toast.LENGTH_SHORT).show();
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("phone",mobileNumInp.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SharedPreferences user = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = user.edit();
                                editor.putString("phone",mobileNumInp.getText().toString());
                                editor.commit();
                                progressBar.setVisibility(View.GONE);
                                verifyBtn.setText("Number Verified");
                                onBackPressed();
                            }
                        });


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    progressBar.setVisibility(View.GONE);
                    verifyBtn.setText("Retry");
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    progressBar.setVisibility(View.GONE);
                    verifyBtn.setText("SMS Quota Exceeded");
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                progressBar.setVisibility(View.GONE);
                verifyBtn.setText("Retry");
            }
        };
        
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
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
