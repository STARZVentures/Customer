package com.shree.app.Customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ncorti.slidetoact.SlideToActView;
import com.shree.app.Objects.CustomerObject;
import com.shree.app.History.HistoryActivity;
import com.shree.app.Objects.LocationObject;
import com.shree.app.Login.LauncherActivity;
import com.shree.app.Payment.PaymentActivity;
import com.shree.app.R;
import com.shree.app.Objects.RideObject;
import com.shree.app.Adapters.TypeAdapter;
import com.shree.app.Objects.TypeObject;
import com.shree.app.Utils.SendNotification;
import com.shree.app.Utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity displayed to the customer
 */
public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, DirectionCallback, View.OnClickListener {


    int TIMEOUT_MILLISECONDS = 20000,
            CANCEL_OPTION_MILLISECONDS = 10000;

    final int END_OF_NIGHT_TIME = 0 ;//UTC hours equivalent
    final int START_OF_NIGHT_TIME = 15; //UTC hours equivalent

    private GoogleMap mMap;

    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private SlideToActView mRequest;

    private LocationObject pickupLocation, currentLocation, destinationLocation;

    private Boolean requestBol = false;

    int bottomSheetStatus = 1;

    private Marker destinationMarker, pickupMarker;


    private LinearLayout mDriverInfo,
            mRadioLayout,
            mLocation,
            mLooking,
            mTimeout;

    private ImageView mDriverProfileImage;

    private TextView mDriverName, mDriverFare;
    private TextView mDriverCar;
    private TextView mDriverLicense;
    private TextView mRatingText;
    private TextView autocompleteFragmentTo;
    private TextView autocompleteFragmentFrom;

    CardView autocompleteFragmentFromContainer, mContainer;

    FloatingActionButton mCallDriver;
    Button mCancel;
    FloatingActionButton mCancelTimeout;
    FloatingActionButton mCurrentLocation;

    DrawerLayout drawer;

    RideObject mCurrentRide;


    private TypeAdapter mAdapter;

    ArrayList<TypeObject> typeArrayList = new ArrayList<>();

    private Boolean driverFound = false;

    private ValueEventListener driveHasEndedRefListener;

    Handler cancelHandler, timeoutHandler;
    private FloatingActionButton navigateDirection;

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_customer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        mDatabase = FirebaseDatabase.getInstance().getReference().child("ride_info");
        mCurrentRide = new RideObject(MapsActivity.this, null);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigateDirection = findViewById(R.id.navigateDirection);
        navigateDirection.setOnClickListener(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserData();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mDriverInfo = findViewById(R.id.driverInfo);
        mRadioLayout = findViewById(R.id.radioLayout);

        mDriverProfileImage = findViewById(R.id.driverProfileImage);

        mDriverName = findViewById(R.id.driverName);
        mDriverCar = findViewById(R.id.driverCar);
        mDriverLicense = findViewById(R.id.driverPlate);
        mDriverFare = findViewById(R.id.driverFare);

        mCallDriver = findViewById(R.id.phone);

        mRatingText = findViewById(R.id.ratingText);

        mContainer = findViewById(R.id.container_card);

        autocompleteFragmentTo = findViewById(R.id.place_to);
        autocompleteFragmentFrom = findViewById(R.id.place_from);
        autocompleteFragmentFromContainer = findViewById(R.id.place_from_container);
        mCurrentLocation = findViewById(R.id.current_location);
        mLocation = findViewById(R.id.location_layout);
        mLooking = findViewById(R.id.looking_layout);
        mTimeout = findViewById(R.id.timeout_layout);
        TextView mLogout = findViewById(R.id.logout);

        mRequest = findViewById(R.id.request);
        mCancel = findViewById(R.id.cancel);
        mCancelTimeout = findViewById(R.id.cancel_looking);

        mLogout.setOnClickListener(v -> logOut());

        mCancelTimeout.setOnClickListener(v -> {
            bottomSheetStatus = 0;
            mCurrentRide.cancelRide();
            endRide();
        });
        mRequest.setOnSlideCompleteListener(v -> startRideRequest());
//        checkForRideFare());

        mCancel.setOnClickListener(v -> {
            bottomSheetStatus = 0;
            mCurrentRide.cancelRide();
            endRide();
        });
        mCallDriver.setOnClickListener(view -> {
            if (mCurrentRide == null) {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.driver_no_phone), Snackbar.LENGTH_LONG).show();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mCurrentRide.getDriver().getPhone()));
                startActivity(intent);
            } else {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.no_phone_call_permissions), Snackbar.LENGTH_LONG).show();
            }
        });

        ImageView mDrawerButton = findViewById(R.id.drawerButton);
        mDrawerButton.setOnClickListener(v -> drawer.openDrawer(Gravity.LEFT));

        mCurrentLocation.setOnClickListener(view -> {

            autocompleteFragmentFrom.setText(getString(R.string.current_location));
            mCurrentLocation.setImageDrawable(getResources()
                    .getDrawable(R.drawable.ic_location_on_primary_24dp));
            pickupLocation = currentLocation;
            if (pickupLocation == null) {
                return;
            }
            fetchLocationName();


            mMap.clear();
            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).title("Pickup")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));
            mCurrentRide.setPickup(pickupLocation);
            autocompleteFragmentFrom.setText(pickupLocation.getName());
            if (destinationLocation != null) {
                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(destinationLocation.getCoordinates()).title("Destination")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                bringBottomSheetDown();
            }

            erasePolylines();
            getRouteToMarker();

            mRequest.setText(getString(R.string.call_uber));
        });


        bringBottomSheetUp();
        initPlacesAutocomplete();
        initRecyclerView();
        isRequestInProgress();

    }


    /**
     * Handles stating the ride request.
     * Starts up two timers. The first will show up after CANCEL_OPTION_MILLISECONDS
     * and it will display a layout with a button for the user to be able to cancel the ride..
     * The second will cancel the ride automatically if the TIMEOUT_MILLISECONDS is reached.
     */
    private void startRideRequest() {
        cancelHandler = new Handler();
        cancelHandler.postDelayed(() -> {
            if (mCurrentRide == null) {
                return;
            }
            if (mCurrentRide.getDriver() == null) {
                runOnUiThread(() -> {
                    mTimeout.setVisibility(View.VISIBLE);
                });
            }
        }, CANCEL_OPTION_MILLISECONDS);

        timeoutHandler = new Handler();
        cancelHandler.postDelayed(() -> {
            if (mCurrentRide == null) {
                return;
            }
            if (mCurrentRide.getDriver() == null) {
                runOnUiThread(() -> {
                    bottomSheetStatus = 0;
                    mCurrentRide.cancelRide();
                    endRide();
                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle(getResources().getString(R.string.no_drivers_around))
                            .setMessage(getResources().getString(R.string.no_driver_found))
                            .setPositiveButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .setIcon(R.drawable.ic_cancel_black_24dp)
                            .show();

                });
            }
        }, TIMEOUT_MILLISECONDS);

        bringBottomSheetDown();
        if (!requestBol) {
            checkForRideFare();
            mCurrentRide.setDestination(destinationLocation);
            mCurrentRide.setPickup(pickupLocation);
            mCurrentRide.setRequestService(mAdapter.getSelectedItem().getId());

            if (mCurrentRide.checkRide() == -1) {
                return;
            }

            requestBol = true;

            mRequest.setText(getResources().getString(R.string.getting_driver));

            mCurrentRide.postRideInfo();

            requestListener();
        }
    }

    private void checkForRideFare() {
        FirebaseDatabase.getInstance().getReference().child("ride_info").orderByChild("customerId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                for (DataSnapshot mData : dataSnapshot.getChildren()) {
                    mCurrentRide = new RideObject();
                    mCurrentRide.parseData(mData);
//                    Long timestamp_picked_customer = mCurrentRide.getPickedCustomerTimestamptime();
//                    Long timestamp_arrived_picked = mCurrentRide.getTimeStampArrivedPicked();
//                    double duration_waiting = ((timestamp_picked_customer - timestamp_arrived_picked) / 60000) % 60;

                    String distancestring = mCurrentRide.getCalculatedRideDistance();
                    Log.d("DistanceInKmMaps",distancestring);
//                    String str = distancestring.substring(0, 5);
                    String str = distancestring.replaceAll("[^\\d.]", "");
                    Log.d("DistanceMapsActivity",str);
                    float distance = Float.parseFloat(str);
                    double price = distance * 0.5;
                    int hours = Integer.valueOf(mCurrentRide.getHours());

                    switch (mCurrentRide.getRequestService()) {
                        case "type_1":
                            price = 14 + distance * 34;
//                            if (duration_waiting >= 5) {
//                                price += 3;
//                            }

                            if (hours > START_OF_NIGHT_TIME && hours < END_OF_NIGHT_TIME) {
                                price += price * 0.5;
                            }
                            mDriverFare.setText("₹ "+ price);
                            Log.d("FareService types 1", String.valueOf(price));
                            break;
                        case "type_2":
                            if (distance <= 2) {
                                price = 400;
                            } else {
                                price = 400 + 95 * (distance - 2);
                            }
//                            if (duration_waiting >= 20) {
//                                price += 5;
//                            }

                            if (hours > START_OF_NIGHT_TIME && hours < END_OF_NIGHT_TIME) {
                                price += price * 0.5;
                            }
                            Log.d("FareService types 2", String.valueOf(price));
                            mDriverFare.setText("₹ "+ String.format(String.valueOf(price), "%.2f"));
                            break;
                        case "type_3":
                            if (distance <= 2) {
                                price = 500;
                            } else {
                                price = 500 + 95 * (distance - 2);
                            }

//                            if (duration_waiting >= 20) {
//                                price += 5;
//                            }

                            if (hours > START_OF_NIGHT_TIME && hours < END_OF_NIGHT_TIME) {
                                price += price * 0.5;
                            }
                            Log.d("FareService types 3", String.valueOf(price));
                            mDriverFare.setText("₹ "+ String.format(String.valueOf(price), "%.2f"));

                            break;
                        case "type_4":
                            price = 25 + distance * 26;

//                            if (duration_waiting >= 5) {
//                                price += 5;
//                            }

                            if (hours > START_OF_NIGHT_TIME && hours < END_OF_NIGHT_TIME) {
                                price += price * 0.8;
                            }
                            Log.d("FareService types 4", String.valueOf(price));
                            mDriverFare.setText("₹ "+ String.format(String.valueOf(price), "%.2f"));

                            break;
                        case "type_5":
                            price = 25 + distance * 25;

//                            if (duration_waiting >= 5) {
//                                price += 5;
//                            }

                            if (hours > START_OF_NIGHT_TIME && hours < END_OF_NIGHT_TIME) {
                                price += price * 0.8;
                            }
                            Log.d("FareService types 5", String.valueOf(price));
                            mDriverFare.setText("₹ "+String.format(String.valueOf(price), " %.2f"));

                            break;
                        case "type_6":
                            price = 20 + distance * 12;

//                            if (duration_waiting >= 5) {
//                                price += 1;
//                            }

                            if (hours > START_OF_NIGHT_TIME && hours < END_OF_NIGHT_TIME) {
                                price += price * 0.8;
                            }
                            Log.d("FareService types 6", String.valueOf(price));
                            mDriverFare.setText("₹ "+ String.format(String.valueOf(price), "%.2f"));

                            break;
                    }
//                    mCurrentRide = new RideObject(MapsActivity.this, null);
//                    startRideRequest();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.d("Fare of Service types",databaseError.getMessage());
            }
        });

    }


    /**
     * Initializes the recyclerview that shows the costumer the
     * available car types
     */
    private void initRecyclerView() {
        typeArrayList = Utils.getTypeList(MapsActivity.this);
        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MapsActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TypeAdapter(typeArrayList, MapsActivity.this);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Handles showing the bottom sheet with animation.
     */
    private void bringBottomSheetUp() {
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        mContainer.startAnimation(slideUp);
        mContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Handles hiding the bottom sheet with animation.
     * Also takes care of hiding or showing the elements in it
     * depending on the current state of the request.
     */
    private void bringBottomSheetDown() {
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                switch (bottomSheetStatus) {
                    case 0:
                        bottomSheetStatus = 1;
                        destinationLocation = null;
                        pickupLocation = null;
                        mCurrentRide.setCurrent(null);
                        mCurrentRide.setDestination(null);
                        autocompleteFragmentFrom.setText(getString(R.string.from));
                        autocompleteFragmentTo.setText(getString(R.string.to));
                        mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_grey_24dp));
                        mMap.clear();
                        erasePolylines();
                        mRadioLayout.setVisibility(View.GONE);
                        mRequest.setVisibility(View.GONE);
                        mLocation.setVisibility(View.VISIBLE);
                        mLooking.setVisibility(View.GONE);
                        mDriverInfo.setVisibility(View.GONE);
                        break;
                    case 1:
                        bottomSheetStatus = 2;
                        mRequest.resetSlider();
                        mRadioLayout.setVisibility(View.VISIBLE);
                        mRequest.setVisibility(View.VISIBLE);
                        mLocation.setVisibility(View.GONE);
                        mLooking.setVisibility(View.GONE);
                        mDriverInfo.setVisibility(View.GONE);
                        mTimeout.setVisibility(View.GONE);
                        break;
                    case 2:
                        bottomSheetStatus = 3;
                        mLocation.setVisibility(View.GONE);
                        mRadioLayout.setVisibility(View.GONE);
                        mRequest.setVisibility(View.GONE);
                        mLooking.setVisibility(View.VISIBLE);
                        mDriverInfo.setVisibility(View.GONE);
                        break;
                    case 3:
                        bottomSheetStatus = 0;
                        mLocation.setVisibility(View.GONE);
                        mRadioLayout.setVisibility(View.GONE);
                        mRequest.setVisibility(View.GONE);
                        mLooking.setVisibility(View.GONE);
                        mDriverInfo.setVisibility(View.VISIBLE);
//                        drawLayoutforNavigation();
                }
                bringBottomSheetUp();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mContainer.startAnimation(slideDown);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void drawLayoutforNavigation() {
        LinearLayout picLL = new LinearLayout(this);
        picLL.layout(0, 0, 50, 50);
        picLL.setForegroundGravity(Gravity.CENTER);
        picLL.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        picLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView myImage = new ImageView(this);
        myImage.setImageResource(R.drawable.direction);
        myImage.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
        picLL.addView(myImage);
        setContentView(picLL);
        myImage.setOnClickListener(this);

    }

    /**
     * Init Places according the updated google api and
     * listen for user inputs, when a user chooses a place change the values
     * of destination and destinationLocation so that the user can call a driver
     */
    void initPlacesAutocomplete() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        autocompleteFragmentTo.setOnClickListener(v -> {
            if (requestBol) {
                return;
            }

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                    .setCountry("NP")
                    .build(getApplicationContext());
            startActivityForResult(intent, 1);
        });

        autocompleteFragmentFrom.setOnClickListener(v -> {
            if (requestBol) {
                return;
            }
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                    .build(getApplicationContext());
            startActivityForResult(intent, 2);
        });
    }


    /**
     * Fetches current user's info and populates the design elements
     */
    private void getUserData() {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance()
                .getReference().child("Users").child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View header = navigationView.getHeaderView(0);

                    CustomerObject mCustomer = new CustomerObject();
                    mCustomer.parseData(dataSnapshot);

                    TextView mUsername = header.findViewById(R.id.usernameDrawer);
                    ImageView mProfileImage = header.findViewById(R.id.imageViewDrawer);

                    mUsername.setText(mCustomer.getName());

                    if (!mCustomer.getProfileImage().equals("default"))
                        Glide.with(getApplication()).load(mCustomer.getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(getClass().getName(), databaseError.getMessage());
            }
        });
    }

    /**
     * Checks if request is in progress by looking at the last ride_info child that the
     * current customer was a part of and if that last ride is still ongoing then
     * start all of the relevant variables up, with that ride info.
     */
    private void isRequestInProgress() {
        FirebaseDatabase.getInstance().getReference().child("ride_info").orderByChild("customerId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                for (DataSnapshot mData : dataSnapshot.getChildren()) {
                    mCurrentRide = new RideObject();
                    mCurrentRide.parseData(mData);

                    if (mCurrentRide.getCancelled() || mCurrentRide.getEnded()) {
                        mCurrentRide = new RideObject();
                        return;
                    }

                    if (mCurrentRide.getDriver() == null) {
                        mTimeout.setVisibility(View.VISIBLE);
                        bottomSheetStatus = 2;
                    } else {
                        bottomSheetStatus = 3;
                    }
                    bringBottomSheetDown();
                    requestListener();
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(getClass().getName(), databaseError.getMessage());
            }
        });
    }


    /**
     * Listener for the current request.
     */
    private void requestListener() {
        if (mCurrentRide == null) {
            return;
        }

        driveHasEndedRefListener = mCurrentRide.getRideRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                RideObject mRide = new RideObject();
                mRide.parseData(dataSnapshot);

                if (mRide.getCancelled() || mRide.getEnded()) {
                    if (!mCurrentRide.getEnded() && mRide.getEnded()) {
                        mCurrentRide.showDialog(MapsActivity.this, mRide);
                    }
                    cancelHandler.removeCallbacksAndMessages(null);
                    timeoutHandler.removeCallbacksAndMessages(null);
                    bottomSheetStatus = 0;
                    endRide();

//                    if (mRide.getCancelledType() == 11) {
//                        new AlertDialog.Builder(MapsActivity.this)
//                                .setTitle(getResources().getString(R.string.no_default_payment))
//                                .setMessage(getResources().getString(R.string.no_payment_available_message))
//                                .setPositiveButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
//                                .setIcon(R.drawable.ic_cancel_black_24dp)
//                                .show();
//                    }
                    return;
                }

                if (mCurrentRide.getDriver() == null && mRide.getDriver() != null) {
                    try {
                        mCurrentRide = (RideObject) mRide.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    cancelHandler.removeCallbacksAndMessages(null);
                    timeoutHandler.removeCallbacksAndMessages(null);

                    getDriverInfo();
                    getDriverLocation();
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(getClass().getName(), databaseError.getMessage());
            }
        });
    }

    /**
     * Get's most updated driver location and it's always checking for movements.
     * Even though we used geofire to push the location of the driver we can use a normal
     * Listener to get it's location with no problem.
     * 0 -> Latitude
     * 1 -> Longitudde
     */
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

    private void getDriverLocation() {
        if (mCurrentRide.getDriver().getId() == null) {
            return;
        }
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(mCurrentRide.getDriver().getId()).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    if (map == null) {
                        return;
                    }
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LocationObject mDriverLocation = new LocationObject(new LatLng(locationLat, locationLng), "");
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.getCoordinates().latitude);
                    loc1.setLongitude(pickupLocation.getCoordinates().longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(mDriverLocation.getCoordinates().latitude);
                    loc2.setLongitude(mDriverLocation.getCoordinates().longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {
                        mRequest.setText(getResources().getString(R.string.driver_here));
                    } else {
                        mRequest.setText(getResources().getString(R.string.driver_found));
                    }

                    mCurrentRide.getDriver().setLocation(mDriverLocation);

                    if(mCurrentRide.getRequestService() != null){
                        mDriverMarker = mMap.addMarker(new MarkerOptions()
                                .position(mCurrentRide.getDriver().getLocation().getCoordinates())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
                                .title(mCurrentRide.getRequestService()));
                        mDriverMarker.setTag(mCurrentRide.getRequestService());
                    }else{
                        mDriverMarker = mMap.addMarker(new MarkerOptions()
                                .position(mCurrentRide.getDriver().getLocation().getCoordinates())
                                .title("your driver")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(getClass().getName(), databaseError.getMessage());
            }
        });

    }

    /**
     * Get all the user information that we can get from the user's database.
     */
    private void getDriverInfo() {
        if (mCurrentRide == null) {
            return;
        }

        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(mCurrentRide.getDriver().getId());
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    mCurrentRide.getDriver().parseData(dataSnapshot);

                    mDriverName.setText(mCurrentRide.getDriver().getNameDash());
                    mDriverCar.setText(mCurrentRide.getDriver().getCarDash());
                    mDriverLicense.setText(mCurrentRide.getDriver().getLicenseDash());
                    if (mCurrentRide.getDriver().getProfileImage().equals("default")) {
                        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);
                    } else {
                        Glide.with(getApplication())
                                .load(mCurrentRide.getDriver().getProfileImage())
                                .apply(RequestOptions.circleCropTransform())
                                .into(mDriverProfileImage);
                    }


                    mRatingText.setText(String.valueOf(mCurrentRide.getDriver()
                            .getDriverRatingString()));

                    bringBottomSheetDown();

                    new SendNotification("You have a customer waiting", "New Ride", mCurrentRide.getDriver().getNotificationKey());
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(getClass().getName(), databaseError.getMessage());
            }
        });
    }


    /**
     * End Ride by removing all of the active listeners,
     * returning all of the values to the default state
     * and clearing the map from markers
     */
    private void endRide() {
        if (cancelHandler != null) {
            cancelHandler.removeCallbacksAndMessages(null);
        }

        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }

        requestBol = false;
        if (driverLocationRefListener != null)
            driverLocationRef.removeEventListener(driverLocationRefListener);

        if (driveHasEndedRefListener != null && mCurrentRide.getRideRef() != null)
            mCurrentRide.getRideRef().removeEventListener(driveHasEndedRefListener);

        if (mCurrentRide != null && driverFound) {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Drivers").child(mCurrentRide.getDriver().getId()).child("customer_requests");
            driverRef.removeValue();
        }

        pickupLocation = null;
        destinationLocation = null;

        driverFound = false;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customer_requests");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, (key, error) -> {
        });

        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (mDriverMarker != null) {
            mDriverMarker.remove();
        }
        mMap.clear();
        mRequest.setText(getString(R.string.call_uber));

        mDriverName.setText("");
        mDriverCar.setText(getString(R.string.destination));
        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);

        autocompleteFragmentTo.setText(getString(R.string.to));
        autocompleteFragmentFrom.setText(getString(R.string.from));
        mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_grey_24dp));

        mCurrentRide = new RideObject(MapsActivity.this, null);
        getDriversAround();
        bringBottomSheetDown();
        zoomUpdated = false;

        mAdapter.setSelectedItem(typeArrayList.get(0));
        mAdapter.notifyDataSetChanged();

    }
    /**
     * Find and update user's location.
     * The update interval is set to 1000Ms and the accuracy is set to PRIORITY_HIGH_ACCURACY,
     * If you're having trouble with battery draining too fast then change these to lower values
     *
     * @param googleMap - Map object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        }

    }

    boolean zoomUpdated = false;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplication() != null) {
                    currentLocation = new LocationObject(new LatLng(location.getLatitude(),
                            location.getLongitude()), "");
                    mCurrentRide.setCurrent(currentLocation);

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!zoomUpdated) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                        zoomUpdated = true;
                    }
                    if (!getDriversAroundStarted)
                        getDriversAround();
                }
            }
        }
    };

    /**
     * This function returns the name of location given the coordinates
     * of said location
     */
    private void fetchLocationName() {
        if (pickupLocation == null) {
            return;
        }
        try {

            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude, 1);
            if (addresses.isEmpty()) {
                autocompleteFragmentFrom.setText(R.string.waiting_for_location);
            } else {
                addresses.size();
                if (addresses.get(0).getThoroughfare() == null) {
                    pickupLocation.setName(addresses.get(0).getLocality());
                } else if (addresses.get(0).getLocality() == null) {
                    pickupLocation.setName("Unknown Location");
                } else {
                    pickupLocation.setName(addresses.get(0).getLocality() + ", " + addresses.get(0).getThoroughfare());
                }
                autocompleteFragmentFrom.setText(pickupLocation.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get permissions for our app if they didn't previously exist.
     * requestCode -> the number assigned to the request that we've made.
     * Each request has it's own unique request code.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission to this app")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(getApplication(), "Please provide the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    boolean getDriversAroundStarted = false;
    List<Marker> markerList = new ArrayList<Marker>();

    /**
     * Displays drivers around the user's current
     * location and updates them in real time.
     */
    private void getDriversAround() {
        if (currentLocation == null) {
            return;
        }
        getDriversAroundStarted = true;
        DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child(("driversWorking"));
        GeoFire geoFire = new GeoFire(driversLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getCoordinates().latitude,
                currentLocation.getCoordinates().longitude), 10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (mCurrentRide != null) {
                    if (mCurrentRide.getDriver() != null) {
                        return;
                    }
                }
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag() == null || key == null) {
                        continue;
                    }
                    if (markerIt.getTag().equals(key))
                        return;
                }

                checkDriverLastUpdated(key);
                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

//                if(mCurrentRide.getRequestService() != null){
//                    Marker mDriverMarker = mMap.addMarker(new MarkerOptions()
//                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
//                            .title(mCurrentRide.getRequestService())
//                            .position(driverLocation));
//                    mDriverMarker.setTag(mCurrentRide.getRequestService());
//                }else{
                    Marker mDriverMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
                            .title(key)
                            .position(driverLocation));
                    mDriverMarker.setTag(key);
//                }
                markerList.add(mDriverMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag() == null || key == null) {
                        continue;
                    }
                    if (markerIt.getTag().equals(key)) {
                        markerIt.remove();
                        markerList.remove(markerIt);
                        return;
                    }

                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag() == null || key == null) {
                        continue;
                    }
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                        return;
                    }
                }
                checkDriverLastUpdated(key);
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d(getClass().getName(), error.getMessage());
            }
        });
    }

    /**
     * Checks if driver has not been updated in a while, if it has been more than x time
     * since the driver location was last updated then remove it from the database.
     *
     * @param key - id of the driver
     */
    private void checkDriverLastUpdated(String key) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }

                        if (dataSnapshot.child("last_updated").getValue() != null) {
                            long lastUpdated = Long.parseLong(dataSnapshot.child("last_updated").getValue().toString());
                            long currentTimestamp = System.currentTimeMillis();

                            if (currentTimestamp - lastUpdated > 10000) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversWorking");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.removeLocation(dataSnapshot.getKey(), (key1, error) -> {
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        Log.e(getClass().getName(), databaseError.getMessage());
                    }
                });
    }


    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MapsActivity.this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Get Route from pickup to destination, showing the route to the user
     */
    private void getRouteToMarker() {

        String serverKey = getResources().getString(R.string.google_maps_key);
        if (mCurrentRide.getDestination() != null && mCurrentRide.getPickup() != null) {
            GoogleDirection.withServerKey(serverKey)
                    .from(mCurrentRide.getDestination().getCoordinates())
                    .to(mCurrentRide.getPickup().getCoordinates())
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }

    private List<Polyline> polylines = new ArrayList<>();

    /**
     * Remove route polylines from the map
     */
    private void erasePolylines() {
        if (polylines == null) {
            return;
        }
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    /**
     * Show map within the pickup and destination marker,
     * This will make sure everything is displayed to the user
     *
     * @param route - route between pickup and destination
     */
    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    /**
     * Checks if route where fetched successfully, if yes then
     * add them to the map
     *
     * @param direction - direction object to the destination
     * @param rawBody   - data of the route
     */
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            Polyline polyline = mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLACK));
            polylines.add(polyline);
            setCameraWithCoordinationBounds(route);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
     Log.d("MapsAcitivty",t.getMessage());
    }


    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example it's place name and place ID).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            LocationObject mLocation;

            if (currentLocation == null) {
                Snackbar.make(findViewById(R.id.drawer_layout),
                        "First Activate GPS", Snackbar.LENGTH_LONG).show();
                return;
            }
            Place place = Autocomplete.getPlaceFromIntent(data);

            mLocation = new LocationObject(place.getLatLng(), place.getName());

            currentLocation = new LocationObject(new LatLng(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude), "");

            if (requestCode == 1) {
                mMap.clear();
                destinationLocation = mLocation;
                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled))
                        .position(destinationLocation.getCoordinates()).title("Destination"));
                mCurrentRide.setDestination(destinationLocation);
                autocompleteFragmentTo.setText(destinationLocation.getName());
                if (pickupLocation != null) {

                    pickupMarker = mMap.addMarker(new MarkerOptions()
                            .position(pickupLocation.getCoordinates())
                            .title("Pickup").icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.ic_radio)));
                    bringBottomSheetDown();
                }
            } else if (requestCode == 2) {
                mMap.clear();
                pickupLocation = mLocation;
                pickupMarker = mMap.addMarker(new MarkerOptions()
                        .position(pickupLocation.getCoordinates()).title("Pickup")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));
                mCurrentRide.setPickup(pickupLocation);
                autocompleteFragmentFrom.setText(pickupLocation.getName());
                if (destinationLocation != null) {
                    destinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(destinationLocation.getCoordinates()).title("Destination")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                    bringBottomSheetDown();

                }
            }
            erasePolylines();
            getRouteToMarker();
            getDriversAround();

            mRequest.setText(getString(R.string.call_uber));


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
            assert status.getStatusMessage() != null;
            Log.i("PLACE_AUTOCOMPLETE", status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            initPlacesAutocomplete();
        }
        initPlacesAutocomplete();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (bottomSheetStatus == 2) {
                bottomSheetStatus = 0;
                bringBottomSheetDown();
            } else {
                super.onBackPressed();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.history) {
            Intent intent = new Intent(MapsActivity.this, HistoryActivity.class);
            intent.putExtra("customerOrDriver", "Customers");
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(MapsActivity.this, CustomerSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.payment) {
            Intent intent = new Intent(MapsActivity.this, PaymentActivity.class);
            startActivity(intent);
        } else if (id == R.id.query) {
            Intent intent = new Intent(MapsActivity.this, QueryActivity.class);
            startActivity(intent);
        }else if(id == R.id.share){
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shree App");
                String shareMessage= "\nLet me recommend you this application\n\n";
//                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                shareMessage = shareMessage + "https://shreevahan.com/Login";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Choose One"));
            } catch(Exception e) {
                //e.toString();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if(destinationLocation.getCoordinates()!= null) {
            String format = "geo:0,0?q=" + destinationLocation.getCoordinates().latitude + ","
                    + destinationLocation.getCoordinates().longitude + "( " + pickupLocation.getName() + ")";
            Uri uri = Uri.parse(format);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            Toast.makeText(this,"No Locaion selected",Toast.LENGTH_LONG).show();
        }
    }
}
