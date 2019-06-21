package com.purpura.googlemaps2018.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
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
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
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
import java.util.Set;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        ChatroomRecyclerAdapter.ChatroomRecyclerClickListener {

    private static final String TAG = "MainActivity";
    private final MainActivity mainActivity = this;
    private ProgressBar mProgressBar;
    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private Set<String> mChatroomIds = new HashSet<>();
    private ChatroomRecyclerAdapter mChatroomRecyclerAdapter;
    private RecyclerView mChatroomRecyclerView;
    private ListenerRegistration mChatroomEventListener;
    private FirebaseFirestore mDb;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private SwitchCompat enableNearBySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(-10f);


        mProgressBar = findViewById(R.id.progressBar);
        mChatroomRecyclerView = findViewById(R.id.chatrooms_recycler_view);

        findViewById(R.id.fab_create_chatroom).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // initSupportActionBar();
        initChatroomRecyclerView();
        initDrawer();
    }

    private void initSupportActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Chatrooms");
        }
    }

    private void initChatroomRecyclerView() {
        mChatroomRecyclerAdapter = new ChatroomRecyclerAdapter(mChatrooms, this);
        mChatroomRecyclerView.setAdapter(mChatroomRecyclerAdapter);
        mChatroomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /*
     * Implemented using:
     * https://github.com/mikepenz/MaterialDrawer/tree/v6.0.9
     * */
    private void initDrawer() {
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ic_launcher_background)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com").withIcon(getResources().getDrawable(R.drawable.batman))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, com.mikepenz.materialdrawer.model.interfaces.IProfile profile, boolean current) {
                        return false;
                    }

                })
                .build();


        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.menu_home);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.menu_tools);

        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(false)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new DividerDrawerItem(), new DividerDrawerItem(), new DividerDrawerItem(), new DividerDrawerItem(),
                        item1,
                        new DividerDrawerItem(),
                        item2
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        return false;
                    }
                })
                .build();
        getSupportActionBar().setElevation(0f);
    }

    private Integer count = 0;
    private boolean onResumeCalled = false;

    @Override
    protected void onResume() {
        super.onResume();
        /*Configuration config = new Configuration();
        config.setToDefaults();
        Log.d("Config", config.toString());*/

        if (!onResumeCalled) {
            Log.d(TAG, "onResume: ");
            onResumeCalled = !onResumeCalled;

            if (checkMapServices() && checkAcesssFineLocationPermissionGranted()) {
                getUserDetails();
            }

        }
    }

    private User getCurrentUser() {
        return ((UserClient) (getApplicationContext())).getUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatroomEventListener != null) {
            mChatroomEventListener.remove();
        }
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(mainActivity, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (checkForegroundServicePermissionGranted()) {
                    MainActivity.this.startForegroundService(serviceIntent);
                }
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
        String uid = FirebaseAuth.getInstance().getUid();

        if (mUserLocation == null && uid != null) {
            mUserLocation = new UserLocation();

            DocumentReference currentFirebaseUserDocument = mDb.collection(getString(R.string.collection_users))
                    .document(uid);

            currentFirebaseUserDocument.get()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: couldn't get CurrentUser", e);
                            Toast.makeText(mainActivity, "Failed to get current user", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Log.d(TAG, "onComplete: successfully set the user client.");
                                User user = task.getResult().toObject(User.class);

                                ((UserClient) (getApplicationContext())).setUser(user);
                                mUserLocation.setUser(user);

                                Toast.makeText(mainActivity, user != null ? "Welcome, " + user.getUsername() : null, Toast.LENGTH_SHORT).show();

                                getLastKnownLocation();
                                getChatrooms();
                            }
                        }
                    });
        } else {
            getLastKnownLocation();
        }

    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                            // Toast.makeText(mainActivity, location.toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            // Toast.makeText(mainActivity, "Location is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {

                        Log.d(TAG, "onComplete: ");

                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            /*Toast.makeText(mainActivity, location.toString(), Toast.LENGTH_SHORT).show();*/
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            mUserLocation.setGeo_point(geoPoint);
                            saveUserLocation();
                            startLocationService();
                            // getChatrooms();
                        }
                    }
                });
    }


    private void saveUserLocation() {

        if (mUserLocation != null) {
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation);
        }
    }

    private void saveCurrentUser() {
        User user = getCurrentUser();

        if (user != null) {
            DocumentReference userRef = mDb
                    .collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.set(user)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mainActivity, "Saving current user failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(mainActivity, "Saving current user completed", Toast.LENGTH_SHORT).show();
                            if (task.isSuccessful()) {
                                Log.d(TAG, "saveCurrentUser: user saved in db");
                                Toast.makeText(mainActivity, "Fetching chatrooms for stored current user", Toast.LENGTH_SHORT).show();
                                getChatrooms();
                            }
                        }
                    });
        }
    }

    private boolean checkMapServices() {
        return isServicesOK() && isMapsEnabled();
    }

    public boolean isServicesOK() {

        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, Constants.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, Constants.ENABLE_GPS_REQUEST);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case Constants.ENABLE_GPS_REQUEST: {
                if (checkAcesssFineLocationPermissionGranted()) {
                    getUserDetails();
                }
                break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_create_chatroom: {
                displayCreateChatroomDialog();
            }
        }
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
                            String chatroomId = chatroom.getChatroom_id();
                            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            User user = getCurrentUser();
                            Integer currentUserAge = user.getAge();
                            Boolean currentUserSeeNearbyEnabled = user.getSeeNearbyEnabled();

                            if (currentUserAge == null) {
                                currentUserAge = 0;
                                user.setAge(currentUserAge);
                                saveCurrentUser();
                            }


                            Boolean isAccessibleToUser = chatroom.isAccessable(email);
                            Boolean isNearyBy = chatroom.isPublicAndBusiness() && currentUserSeeNearbyEnabled && chatroom.checkProximity(mUserLocation.getGeo_point());
                            Boolean isAlreadyInList = mChatroomIds.contains(chatroomId);
                            Boolean isAgeAppropriate = chatroom.isInAgeRangeInclusive(currentUserAge);

                            Boolean isGranted = (isAccessibleToUser || isNearyBy) && !isAlreadyInList && isAgeAppropriate;

                            if (isGranted) {
                                mChatroomIds.add(chatroomId);
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

    private void createChatroom(String chatroomName, Boolean isPrivate, Integer ageFrom, Integer ageTo) {
        User user = getCurrentUser();

        final Chatroom chatroom = new Chatroom();
        chatroom.setTitle(chatroomName);
        chatroom.addUser(user);
        chatroom.setPrivate(isPrivate);
        chatroom.setAgeFrom(ageFrom);
        chatroom.setAgeTo(ageTo);

        DocumentReference newChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document();

        chatroom.setChatroom_id(newChatroomRef.getId());

        newChatroomRef.set(chatroom)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mainActivity, "onSuccess", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private void displayCreateChatroomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name");

        View createChatroomDialog = View.inflate(this, R.layout.private_checkbox, null);

        EditText editChatroomName = createChatroomDialog.findViewById(R.id.chatRoomName);
        EditText editChatroomAgeFrom = createChatroomDialog.findViewById(R.id.edit_age_from);
        EditText editChatroomAgeTo = createChatroomDialog.findViewById(R.id.edit_age_to);

        editChatroomAgeFrom.setText(Constants.DEFAULT_CHATROOM_AGE_FROM.toString());
        editChatroomAgeTo.setText(Constants.DEFAULT_CHATROOM_AGE_TO.toString());

        final Boolean[] isPrivate = {false};

        CheckBox checkBox = createChatroomDialog.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> isPrivate[0] = isChecked);

        checkBox.setText("Private chatroom");
        builder.setView(createChatroomDialog).setCancelable(false);

        builder.setPositiveButton("CREATE", (dialog, which) -> {

            String chatroomName = editChatroomName.getText().toString();
            Integer chatroomAgeFrom = Integer.parseInt(editChatroomAgeFrom.getText().toString());
            Integer chatroomAgeTo = Integer.parseInt(editChatroomAgeTo.getText().toString());

            if (!chatroomName.isEmpty()) {
                createChatroom(chatroomName, isPrivate[0], chatroomAgeFrom, chatroomAgeTo);
            } else {
                Toast.makeText(MainActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onChatroomSelected(int position) {

        Chatroom selectedChatroom = mChatrooms.get(position);
        navChatroomActivity(selectedChatroom);

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
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem enableNearByMenuItem = menu.findItem(R.id.action_enable_nearby_switch);
        enableNearByMenuItem.setActionView(R.layout.switch_layout);

        enableNearBySwitch = enableNearByMenuItem.getActionView().findViewById(R.id.switchForActionBar);
        String switchCompatText = getString(R.string.action_enable_nearby);
        enableNearBySwitch.setText(switchCompatText);
        enableNearBySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onCheckedChanged: ");
                Toast.makeText(mainActivity, "Near By=", Toast.LENGTH_SHORT).show();
                getCurrentUser().toggleSeeNearbyEnabled();
                saveCurrentUser(); // saveCurrentUser() calls getChatrooms when it has finished saving the current user
            }
        });

        // onCreateOptionsMenu is called after onResume
        if (getCurrentUser() != null && getCurrentUser().getSeeNearbyEnabled() != null && enableNearBySwitch != null) {
            enableNearBySwitch.setChecked(getCurrentUser().getSeeNearbyEnabled());
        }

        return true;
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
                getCurrentUser().toggleSeeNearbyEnabled();
                saveCurrentUser();
                // saveCurrentUser() calls getChatrooms when it has finished saving the current user
                enableNearBySwitch.setChecked(getCurrentUser().getSeeNearbyEnabled());
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

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


    /**
     * Permission-related Code
     */

    private boolean checkForegroundServicePermissionGranted() {
        String permission = android.Manifest.permission.FOREGROUND_SERVICE;
        int permissionRequest = Constants.PERMISSIONS_REQUEST_FOREGROUND_SERVICE;
        return checkPermissionGranted(this, permission, permissionRequest);
    }

    private boolean checkAcesssFineLocationPermissionGranted() {
        String permission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        int permissionRequest = Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
        return checkPermissionGranted(this, permission, permissionRequest);
    }

    private boolean checkPermissionGranted(Activity activity, String permission, int permissionRequest) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            String message = String.format("%s granted", permission);
            Log.d(TAG, message);
            // Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            String message = String.format("%s NOT granted", permission);
            Log.d(TAG, message);

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                String[] split = permission.split(".");
                String permissionName = split[split.length - 1];

                String snackbarText = String.format("%s permission is needed", permissionName);
                Snackbar.make(findViewById(android.R.id.content), snackbarText, Snackbar.LENGTH_INDEFINITE)
                        .setAction("ENABLE", v -> ActivityCompat.requestPermissions(activity,
                                new String[]{permission},
                                permissionRequest))
                        .show();

            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        permissionRequest);
            }

        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");

        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access fine location permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Access fine location permission is NOT granted", Toast.LENGTH_SHORT).show();
                }
            }

            case Constants.PERMISSIONS_REQUEST_FOREGROUND_SERVICE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Foreground service permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Toast.makeText(this, "Foreground service permission is NOT granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
