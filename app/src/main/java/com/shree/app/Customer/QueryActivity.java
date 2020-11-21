package com.shree.app.Customer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shree.app.Objects.CustomerObject;
import com.shree.app.Objects.QueryObject;
import com.shree.app.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.shree.app.Utils.Utils.isNameValid;
import static com.shree.app.Utils.Utils.isValidEmail;
import static com.shree.app.Utils.Utils.isValidPhoneNo;

public class QueryActivity extends AppCompatActivity {
    private EditText mNameField, mPhoneField, mEmail, mQuery;
    private DatabaseReference mCustomerDatabase;

    private String userID;

    QueryObject queryObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        mNameField = findViewById(R.id.name);
        mPhoneField = findViewById(R.id.phone);
        mQuery = findViewById(R.id.query);
        mEmail = findViewById(R.id.email);


        Button mConfirm = findViewById(R.id.confirm);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance()
                .getReference().child("Users").child("query_info").child(userID);

        queryObject = new QueryObject(userID);

        mConfirm.setOnClickListener(v -> saveUserInformation());

        setupToolbar();
    }

    /**
     * Sets up toolbar with custom text and a listener
     * to go back to the previous activity
     */
    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.query));
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> finish());
    }


    /**
     * Saves current user 's info to the database.
     * If the resultUri is not null that means the profile image has been changed
     * and we need to upload it to the storage system and update the database with the new url
     */
    private void saveUserInformation() {
        boolean flag = false;
        String mName = mNameField.getText().toString();
        String mPhone = mPhoneField.getText().toString();
        String memail = mEmail.getText().toString();
        String mquery = mQuery.getText().toString();
        if (!mNameField.getText().equals("")) {
            if (isNameValid(mName)) {
                flag = true;
            } else {
                flag = false;
                mNameField.setError("Please enter valid name");
            }
        } else {
            flag = false;
            mNameField.setError("Please enter name");
        }
        if (!mPhone.equals("")) {
            if (isValidPhoneNo(mPhone)) {
                flag = true;
            } else {
                flag = false;
                mPhoneField.setError("Please enter valid mobile number");
            }
        } else {
            flag = false;
            mPhoneField.setError("Please enter mobile number");
        }
        if (!memail.equals("")) {
                flag = true;
        } else {
            flag = false;
            mEmail.setError("Please enter registered email");
        }

        if (!mquery.equals("")) {
            flag = true;
        } else {
            flag = false;
            mQuery.setError("Please enter your query");
        }


        if (flag) {
            Map<String, Object> queryinfo = new HashMap<>();
            queryinfo.put("name", mName);
            queryinfo.put("phone", mPhone);
            queryinfo.put("email", memail);
            queryinfo.put("query", mquery);
            mCustomerDatabase.updateChildren(queryinfo);
            Toast.makeText(QueryActivity.this,"Your query has been submitted!!",Toast.LENGTH_LONG).show();
            finish();
        }else{
            Toast.makeText(QueryActivity.this,"Unable to submit query",Toast.LENGTH_LONG).show();
        }


    }

}
