package com.purpura.googlemaps2018.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.purpura.googlemaps2018.Constants;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.UserClient;
import com.purpura.googlemaps2018.adapters.ChatroomRecyclerAdapter;
import com.purpura.googlemaps2018.models.Chatroom;
import com.purpura.googlemaps2018.models.User;
import com.purpura.googlemaps2018.models.UserLocation;
import com.purpura.googlemaps2018.services.LocationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import static com.purpura.googlemaps2018.Constants.ERROR_DIALOG_REQUEST;
import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_CAMERA;
import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

// Image-related permissions


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        ChatroomRecyclerAdapter.ChatroomRecyclerClickListener {

    private static final String TAG = "MainActivity";
    private final MainActivity mainActivity = this;

    //widgets
    private ProgressBar mProgressBar;

    //vars
    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private Set<String> mChatroomIds = new HashSet<>();
    private ChatroomRecyclerAdapter mChatroomRecyclerAdapter;
    private RecyclerView mChatroomRecyclerView;
    private ListenerRegistration mChatroomEventListener;
    private FirebaseFirestore mDb;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progressBar);
        mChatroomRecyclerView = findViewById(R.id.chatrooms_recycler_view);

        findViewById(R.id.fab_create_chatroom).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initSupportActionBar();
        initChatroomRecyclerView();

        getFirebaseInstanceIdToken();
    }


    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(mainActivity, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                MainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.purpura.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = new UserLocation();

            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        currentUser = task.getResult().toObject(User.class);
                        mUserLocation.setUser(currentUser);
                        ((UserClient) (getApplicationContext())).setUser(currentUser);
                        getLastKnownLocation();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }

    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, "onSuccess: " + location.toString());
                            Toast.makeText(mainActivity, location.toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mainActivity, "Location is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {

                        Log.d(TAG, "onComplete: ");

                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            Toast.makeText(mainActivity, location.toString(), Toast.LENGTH_SHORT).show();

                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            mUserLocation.setGeo_point(geoPoint);
                            saveUserLocation();
                            startLocationService();
                            getChatrooms();
                        }
                    }
                });

    }


    private void saveUserLocation() {

        if (mUserLocation != null) {
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

    private void saveCurrentUser() {

        if (currentUser != null) {
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveCurrentUser: user saved in db");
                        getChatrooms();
                    }
                }
            });
        }
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            getUserDetails();
            // getChatrooms();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {

                    getUserDetails();
                } else {
                    getLocationPermission();
                }
                getChatrooms();
            }
        }

    }

    private void initSupportActionBar() {
        setTitle("Chatrooms");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.fab_create_chatroom: {
                newChatroomDialog();
            }
        }
    }

    private void initChatroomRecyclerView() {
        mChatroomRecyclerAdapter = new ChatroomRecyclerAdapter(mChatrooms, this);
        mChatroomRecyclerView.setAdapter(mChatroomRecyclerAdapter);
        mChatroomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void getChatrooms() {

        CollectionReference chatroomsCollection = mDb.collection(getString(R.string.collection_chatrooms));

        mChatroomEventListener = chatroomsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: called.");

                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                mChatrooms.clear();
                mChatroomIds.clear();

                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Chatroom chatroom = doc.toObject(Chatroom.class);

                        if (FirebaseAuth.getInstance().getCurrentUser() != null && mUserLocation != null) {
                            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();


                            Boolean isPrivateSettings = chatroom.canAccess(email);
                            Boolean isNearbySettings = chatroom.checkProximity(mUserLocation.getGeo_point()) && currentUser.getSeeNearbyEnabled();
                            Boolean isAlreadyInList = mChatroomIds.contains(chatroom.getChatroom_id());

                            if ((isPrivateSettings || isNearbySettings) && !isAlreadyInList) {
                                mChatroomIds.add(chatroom.getChatroom_id());
                                mChatrooms.add(chatroom);
                            }
                        }
                    }

                    Log.d(TAG, "onEvent: number of chatrooms: " + mChatrooms.size());
                    mChatroomRecyclerAdapter.notifyDataSetChanged();
                }

            }
        });


    }

    private void buildNewChatroom(String chatroomName, Boolean isPrivate) {

        final Chatroom chatroom = new Chatroom();
        chatroom.setTitle(chatroomName);
        chatroom.setPrivate(isPrivate);
        chatroom.addUser(currentUser);


        DocumentReference newChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document();

        chatroom.setChatroom_id(newChatroomRef.getId());

        newChatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideDialog();

                if (task.isSuccessful()) {
                    navChatroomActivity(chatroom);
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navChatroomActivity(Chatroom chatroom) {
        Intent intent = new Intent(MainActivity.this, ChatroomActivity.class);
        intent.putExtra(getString(R.string.intent_chatroom), chatroom);
        startActivity(intent);
    }

    private void newChatroomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name");

        final Boolean[] isPrivate = {false};
        View checkBoxView = View.inflate(this, R.layout.private_checkbox, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPrivate[0] = isChecked;
            }
        });
        EditText chatRoomName = (EditText) checkBoxView.findViewById(R.id.chatRoomName);
        checkBox.setText("Private chatroom");
        builder.setView(checkBoxView).setCancelable(false);

        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!chatRoomName.getText().toString().equals("")) {
                    buildNewChatroom(chatRoomName.getText().toString(), isPrivate[0]);
                } else {
                    Toast.makeText(MainActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void newPrivateChatroomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().equals("")) {
                    buildNewChatroom(input.getText().toString(), false);
                } else {
                    Toast.makeText(MainActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatroomEventListener != null) {
            mChatroomEventListener.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getUserDetails();
            } else {
                getLocationPermission();
            }
        }
        getChatrooms();
    }

    @Override
    public void onChatroomSelected(int position) {
        navChatroomActivity(mChatrooms.get(position));
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                signOut();
                return true;
            }
            case R.id.action_profile: {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            case R.id.action_enable_nearby: {
                currentUser.toggleSeeNearbyEnabled();
                saveCurrentUser();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void getFirebaseInstanceIdToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();


                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }


}
