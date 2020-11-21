package com.shree.app.Login;

import android.content.Intent;
import android.os.Bundle;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.google.android.gms.tasks.OnCompleteListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.shree.app.R;

import java.util.HashMap;
import java.util.Map;

import static com.shree.app.Utils.Utils.isValidPhoneNo;


/**
 * Fragment Responsible for registering a new user
 */
public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Detail Activity";
    private EditText mName,phone;

    private SegmentedButtonGroup mRadioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        initializeObjects();
        Intent intent = getIntent();
        String phoneNo = intent.getStringExtra("phone");
        if(phoneNo != null || !phoneNo.equals("")){
            String str = phoneNo.substring(3);
            phone.setText(str);
        }
    }
    /**
     * Register the user, but before that check if every field is correct.
     * After that registers the user and creates an entry for it oin the database
     */
    private void register() {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean flag = false;
        final String mPhone = phone.getText().toString();

        final String name = mName.getText().toString();
        final String accountType;
//        int selectId = mRadioGroup.getPosition();
        accountType = "Customers";

        if (mName.getText().length() == 0) {
            mName.setError("please fill this field");
            return;
        }

        if (mPhone.equals("")) {
            flag = true;
            phone.setError("Please enter mobile number");
        }

        String mobNo = "+977" + mPhone;
        if (!flag) {
            Map<String, Object> newUserMap = new HashMap<>();
            newUserMap.put("name", name);
            newUserMap.put("phone", mobNo);
            newUserMap.put("profileImageUrl", "default");

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(accountType).child(user_id).updateChildren(newUserMap).
                    addOnCompleteListener((OnCompleteListener<Void>) task -> {
                Intent intent = new Intent(DetailsActivity.this, LauncherActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }


    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    private void initializeObjects() {
        mName = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        Button mRegister = findViewById(R.id.register);
        mRadioGroup = findViewById(R.id.radioRealButtonGroup);

        mRadioGroup.setPosition(0, false);
        mRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.register) {
            register();
        }
    }
}