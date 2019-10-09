package app.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddVehicle extends AppCompatActivity {

    private EditText v_name,v_number,v_license,v_seats,mobile_number;
    private Button continueBtn;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        //SETTING TOOLBAR  FOR WALLFRAGMENR
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Park new vehicle");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        v_license = findViewById(R.id.licenseNumberInp);
        v_name = findViewById(R.id.vehicleNameinp);
        v_number = findViewById(R.id.vehicleNumberInp);
        v_seats = findViewById(R.id.totalSeatsInp);
        mobile_number = findViewById(R.id.mobileNumberInp);
        continueBtn = findViewById(R.id.continueBtn);
        progressBar = findViewById(R.id.uploadingBar);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parkVehicle();
            }
        });

    }

    public void parkVehicle(){

        hideKeyboard(this);
        setFeildEnable(false);
        progressBar.setVisibility(View.VISIBLE);
        String v_name_str = v_name.getText().toString();
        String v_license_str = v_license.getText().toString();
        String v_seats_str = v_seats.getText().toString();
        String v_number_str = v_number.getText().toString();

        String mobile_number_str = mobile_number.getText().toString();

        Map<String,Object> vehicle = new HashMap<>();
        vehicle.put("vehicle_name",v_name_str);
        vehicle.put("vehicle_number",v_number_str);
        vehicle.put("vehicle_license",v_license_str);
        vehicle.put("vehicle_seats",v_seats_str);

        if(validateForm() && IsValidLicenseNumber(v_license_str) && IsValidVehicleNumber(v_number_str)){
            db.collection("vehicles").document(auth.getCurrentUser().getUid()).collection("vehicles").add(vehicle)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                onBackPressed();
                                db.collection("vehicles").document(auth.getCurrentUser().getUid()).collection("vehicles")
                                        .document(task.getResult().getId()).update("key",task.getResult().getId());
                                Toast.makeText(AddVehicle.this, "Vehicle Parked", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }else{
            setFeildEnable(true);
            progressBar.setVisibility(View.INVISIBLE);
        }





    }


    public void setFeildEnable(boolean val){
        v_license.setEnabled(val);
        v_name.setEnabled(val);
        v_number.setEnabled(val);
        v_seats.setEnabled(val);
        continueBtn.setEnabled(val);

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public boolean validateForm(){
        boolean valid = true;
        String v_name_str = v_name.getText().toString();
        String v_license_str = v_license.getText().toString();
        String v_seats_str = v_seats.getText().toString();
        String v_number_str = v_number.getText().toString();

        if(!TextUtils.isEmpty(v_name_str) && !TextUtils.isEmpty(v_license_str) && !TextUtils.isEmpty(v_seats_str) && !TextUtils.isEmpty(v_number_str) && !TextUtils.isEmpty(mobile_number.getText().toString())){
            valid = true;
        }else{
            valid = false;
            Toast.makeText(this, "All Feilds are mandatory", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    public boolean IsValidLicenseNumber(String text){


        String expression = "^(?<intro>[A-Z]{2})(?<numeric>\\d{2})(?<year>\\d{4})(?<tail>\\d{7})$";
        CharSequence inputStr = text;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if(matcher.matches())
        {
            System.out.println("Validate licence Number");
            return true;
        }
        else
        {
            System.out.println("Not Validated licence Number");
            return false;

        }


    }

    public boolean IsValidVehicleNumber(String text){


        String expression = "^[a-z]{2}[0-9]{2}[a-z]{1,2}[0-9]{4}$";
        CharSequence inputStr = text;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if(matcher.matches())
        {
            System.out.println("Validate Vehicle num");
            return true;
        }
        else
        {
            System.out.println("Not Validated vehicle num");
            return false;

        }


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
