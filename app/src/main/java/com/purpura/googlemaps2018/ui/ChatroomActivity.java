package com.purpura.googlemaps2018.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.FontRequest;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.io.ByteStreams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.purpura.googlemaps2018.BuildConfig;
import com.purpura.googlemaps2018.Constants;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.UserClient;
import com.purpura.googlemaps2018.adapters.ChatMessageRecyclerAdapter;
import com.purpura.googlemaps2018.models.ChatMessage;
import com.purpura.googlemaps2018.models.Chatroom;
import com.purpura.googlemaps2018.models.User;
import com.purpura.googlemaps2018.models.UserLocation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_CAMERA;
import static com.purpura.googlemaps2018.Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class ChatroomActivity extends AppCompatActivity implements
        AddUserToChatFragment.OnUserSelectedListener {

    private static final String TAG = "ChatroomActivity";
    private final ChatroomActivity chatroomActivity = this;

    /**
     * Resources:
     * https://github.com/rolandwu23/android_camera_app_intent
     * https://developer.android.com/training/camera/photobasics
     * https://developer.android.com/training/data-storage/files#PublicFiles
     */

    // Firebase-related
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private StorageReference mImagesRef;

    // Image (the same variables are used for Gallery-related and Camera-related tasks)
    private Bitmap galleryImage = null;
    private String galleryImageDownloadURL = null;

    //widgets
    private Chatroom mChatroom;
    private EditText mMessage;

    //vars
    private ListenerRegistration mChatMessageEventListener, mUserListEventListener;
    private RecyclerView mChatMessageRecyclerView;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest)
                .setReplaceAll(true)
                .setEmojiSpanIndicatorEnabled(true)
                .setEmojiSpanIndicatorColor(Color.WHITE)
                .registerInitCallback(new EmojiCompat.InitCallback() {
                });
        EmojiCompat.init(config);

        setContentView(R.layout.activity_chatroom);
        mMessage = findViewById(R.id.input_message);
        mChatMessageRecyclerView = findViewById(R.id.chatmessage_recycler_view);

        View btnCheckmark = findViewById(R.id.checkmark);
        btnCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(chatroomActivity, "Sending message...", Toast.LENGTH_SHORT).show();
                insertNewMessage();
            }
        });

        View btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(chatroomActivity, "Opening gallery...", Toast.LENGTH_SHORT).show();
                if (mWriteExternalStoragePermissionGranted) {
                    getGalleryImage();
                    sendGalleryImage();
                } else {
                    getWriteExternalStoragePermission();
                }
            }
        });

        View btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(chatroomActivity, "Opening camera...", Toast.LENGTH_SHORT).show();
                if (mWriteExternalStoragePermissionGranted && mCameraPermissionGranted) {
                    getCameraImage();
                    sendGalleryImage();
                } else {
                    getWriteExternalStoragePermission();
                    getCameraPermission();
                }
            }
        });

        // Initialize Firebase-related
        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mImagesRef = mStorageRef.child("images");

        getIncomingIntent();
        initChatroomRecyclerView();

    }

    private void sendGalleryImage() {
        /* Make sure you have the image */
        if (galleryImageDownloadURL != null && galleryImage != null) {
            // Store the imageUrl to Cloud Firestore
            CollectionReference chatroomChatMessagesRef = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages));

            DocumentReference chatMessageRef = chatroomChatMessagesRef.document();

            String chatMessageId = chatMessageRef.getId();
            User user = ((UserClient) (getApplicationContext())).getUser();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage_id(chatMessageId);
            chatMessage.setUser(user);
            chatMessage.setImageUrl(galleryImageDownloadURL);

            chatMessageRef.set(chatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) { // && task.getResult() != null
                        clearGalleryImage();
                    } else {
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    private void clearGalleryImage() {
        this.galleryImage = null;
        this.galleryImageDownloadURL = null;
    }

    // SELECT_GALLERY_IMAGE_ACTION

    private void getGalleryImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            Intent galleryChooser = Intent.createChooser(galleryIntent, "Select an Image");
            startActivityForResult(galleryChooser, Constants.SELECT_GALLERY_IMAGE_REQUEST);
        }
    }


    private static final int REQUEST_TAKE_PHOTO = 100;


    private void getCameraImage() {
        Toast.makeText(this, "getCameraImage", Toast.LENGTH_SHORT).show();

        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            Intent cameraChooser = Intent.createChooser(cameraIntent, "Select a Camera App");

            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(chatroomActivity, BuildConfig.APPLICATION_ID + ".fileprovider", imageFile);
                // get read and write permissions, the | is a bitwise-OR, it's the standard way to combine the two flags
                cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                chatroomActivity.startActivityForResult(cameraChooser, Constants.CAPTURE_CAMERA_IMAGE_REQUEST);
            }

        }
    }

    private File createImageFile() {
        // android:authorities="com.purpura.googlemaps2018.fileprovider"

        String state = Environment.getExternalStorageState();
        File filesDir;
        // Make sure it's available
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            filesDir = new File(Environment.getExternalStorageDirectory() + "/PurpuraChat/Media", "Images");
        } else {
            // Load another directory, probably local memory
            // filesDir = new File(getFilesDir(),"Images");
            filesDir = new File(getApplicationContext().getExternalFilesDir(null), "Images");
        }

        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        return new File(filesDir, Constants.DEFAULT_PICTURE_NAME);
    }

    private String getFilePath(Uri uri) {
        String state = Environment.getExternalStorageState();
        File filesDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            filesDir = new File(Environment.getExternalStorageDirectory() + "/PurpuraChat/Media", "Images");
        } else {
            filesDir = new File(getApplicationContext().getExternalFilesDir(null), "Images");
        }
        return filesDir.toString() + uri.toString().substring(uri.toString().lastIndexOf('/'));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Gallery Intent
        if (requestCode == Constants.SELECT_GALLERY_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {

            Uri imageUri = data.getData();
            try {
                storeImageToFirebaseStorage(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        // Camera Intent
        else if (requestCode == Constants.CAPTURE_CAMERA_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK &&
                data != null && data.getExtras() != null) {

            // cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            Bundle extras = data.getExtras();
            Uri imageUri = (Uri) extras.get(MediaStore.EXTRA_OUTPUT);
            try {
                storeImageToFirebaseStorage(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeImageToFirebaseStorage(Uri uri) throws IOException {
        galleryImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        String message = String.format("ImageReceived=%s, %s", galleryImage != null, uri);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        String fileName = getFileName(uri);
        StorageReference imageRef = mImagesRef.child(fileName);

        UploadTask uploadTask = imageRef.putFile(uri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful() && task.getException() != null) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Uri downloadUri = task.getResult();
                    String downloadURL = downloadUri.toString();
                    Toast.makeText(chatroomActivity, downloadURL, Toast.LENGTH_LONG).show();

                    chatroomActivity.galleryImageDownloadURL = downloadURL;
                    try {
                        chatroomActivity.galleryImage = getBitmapFromUrl(downloadURL);
                    } catch (IOException e) {
                        Toast.makeText(chatroomActivity, "getBitmapFromUrl Failure", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                } else {
                    // Handle failures
                    String message = "Firebase Storage upload task failed";
                    Toast.makeText(chatroomActivity, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void getUserLocation(int pos) {
        User user = mChatroom.getUsers().get(pos).getUser();
        DocumentReference locationsRef = mDb
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().toObject(UserLocation.class) != null) {

                        mChatroom.getUsers().get(pos).setUserLocation(task.getResult().toObject(UserLocation.class));
                    }
                }
            }
        });

    }


    private void getChatMessages() {
        CollectionReference messagesRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chat_messages));

        mChatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage message = doc.toObject(ChatMessage.class);
                                if (!mMessageIds.contains(message.getMessage_id())) {
                                    mMessageIds.add(message.getMessage_id());
                                    mMessages.add(message);
                                    mChatMessageRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                                }

                            }
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }


    private void insertNewMessage() {
        String message = mMessage.getText().toString();

        if (message.length() > 0) {
            message = message.replaceAll(System.getProperty("line.separator"), "");

            DocumentReference newMessageDoc = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages))
                    .document();

            ChatMessage newChatMessage = new ChatMessage();
            newChatMessage.setMessage(message);
            newChatMessage.setMessage_id(newMessageDoc.getId());

            User user = ((UserClient) (getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            newChatMessage.setUser(user);

            newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        clearMessage();
                    } else {
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void clearMessage() {
        mMessage.setText("");
    }


    private void getChatroomUsers() {
        /*
        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            mChatroom.resetUsers();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                mChatroom.addUser(user);
                                getUserLocation(user);
                            }

                            Log.d(TAG, "onEvent: user list size: " + mChatroom.getUsers().size());
                        }
                    }
                });
        */
        for (int i = 0; i < mChatroom.getUsers().size(); i++) {
            getUserLocation(i);
        }
    }

    private void initChatroomRecyclerView() {
        mChatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(mMessages, new ArrayList<User>(), this);
        mChatMessageRecyclerView.setAdapter(mChatMessageRecyclerAdapter);
        mChatMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mChatMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mChatMessageRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mMessages.size() > 0 && mChatMessageRecyclerView.getAdapter() != null) {
                                mChatMessageRecyclerView.smoothScrollToPosition(
                                        mChatMessageRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });

    }


    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra(getString(R.string.intent_chatroom))) {
            mChatroom = getIntent().getParcelableExtra(getString(R.string.intent_chatroom));
            setChatroomName();
            joinChatroom();
        }
    }

    private DocumentReference getCurrentUserSetting() {
        return mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(mAuth.getUid());
    }

    private void leaveChatroom() {
        User user = getCurrentUser();
        mChatroom.removeUser(user);
        finish();
    }

    private void toggleShareLocationInChatroom() {
        User user = getCurrentUser();
        Boolean nowEnabled = mChatroom.toggleUserLocation(user);
        DocumentReference userSettingRef = getCurrentUserSetting();

        userSettingRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    restartListFragmentIfVisible();
                }
            }
        });

       /*
       TO DO: Set appropriate message in the menu
       MenuItem toggleLocation = findViewById(R.id.action_toggle_share_location);
        if(nowEnabled){
            toggleLocation.setTitle(getString(R.string.disable_location));
        } else {
            toggleLocation.setTitle(getString(R.string.enable_location));
        }*/

    }

    private User getCurrentUser() {
        return ((UserClient) (getApplicationContext())).getUser();
    }

    private void joinChatroom() {

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(mAuth.getUid());

        User user = getCurrentUser();
        mChatroom.addUser(user);
        joinChatroomRef.set(user); // Don't care about listening for completion.
    }


    private void setChatroomName() {
        getSupportActionBar().setTitle(mChatroom.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatMessages();
        getChatroomUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id());
        if (mChatroom.getUsers().isEmpty() && mChatroom.getPrivate()) {
            joinChatroomRef.delete();
        } else {
            joinChatroomRef.set(mChatroom);
        }
        if (mChatMessageEventListener != null) {
            mChatMessageEventListener.remove();
        }
        if (mUserListEventListener != null) {
            mUserListEventListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    UsersInChatFragment getUsersInChatFragment() {
        return (UsersInChatFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));

    }

    Boolean isUsersInChatFragmentVisible() {
        UsersInChatFragment fragment = getUsersInChatFragment();
        return fragment != null;// && fragment.isVisible();

    }

    public void restartListFragmentIfVisible() {
        UsersInChatFragment fragment = getUsersInChatFragment();
        if (fragment != null) {

            getSupportFragmentManager()
                    .beginTransaction().
                    remove(fragment).commit();
            inflateUsersInChatFragment();

        }

    }

    private boolean isAddUserToChatFragmentVisible() {
        AddUserToChatFragment fragment =
                (AddUserToChatFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_add_user_to_chatroom));
        return fragment != null && fragment.isVisible();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
                if (backStackCount > 0) {
                    super.onBackPressed();
                    return true;
                }
                finish();
                return true;
            }
            case R.id.action_chatroom_user_list: {
                inflateUsersInChatFragment();
                return true;
            }
            case R.id.action_chatroom_leave: {
                leaveChatroom();
                return true;
            }
            case R.id.action_toggle_share_location: {
                toggleShareLocationInChatroom();
                return true;
            }
            case R.id.action_add_user_to_chatroom: {
                inflateAddUserToChatFragment();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    private void inflateUsersInChatFragment() {
        hideSoftKeyboard();

        UsersInChatFragment fragment = UsersInChatFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mChatroom.getUsers());

        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.chatroom_fragment_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }

    private void inflateAddUserToChatFragment() {
        hideSoftKeyboard();

        AddUserToChatFragment fragment = AddUserToChatFragment.newInstance();
        Bundle bundle = new Bundle();
        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_chatroom_user_list));
        ArrayList<User> usersNotInChat = new ArrayList<>();
        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                if (mChatroom.getSettingForEmail(user.getEmail()) == null && !usersNotInChat.contains(user))
                                    usersNotInChat.add(user);
                            }
                            Log.d(TAG, "onEvent: user list size: " + mChatroom.getUsers().size());
                            bundle.putParcelableArrayList(getString(R.string.intent_user_list), usersNotInChat);

                            fragment.setArguments(bundle);

                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
                            transaction.replace(R.id.chatroom_fragment_container, fragment, getString(R.string.fragment_add_user_to_chatroom));
                            transaction.addToBackStack(getString(R.string.fragment_add_user_to_chatroom));
                            transaction.commit();

                        }
                    }
                });

    }
/*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkmark: {
                insertNewMessage();
            }
        }
    }*/

    @Override
    public void addSelectedUserToChatroom(User user) {
        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id());

        mChatroom.addUser(user);
        joinChatroomRef.set(mChatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    getSupportFragmentManager().popBackStack();
                    restartListFragmentIfVisible();
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }

    /**
     * Permission-related Code
     */


    private boolean mWriteExternalStoragePermissionGranted = false;

    private void getWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mWriteExternalStoragePermissionGranted = true;
            //
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private boolean mCameraPermissionGranted = false;

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraPermissionGranted = true;
            //
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mWriteExternalStoragePermissionGranted = false;
        mCameraPermissionGranted = false;

        switch (requestCode) {

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mWriteExternalStoragePermissionGranted = true;
                } else {
                    Toast.makeText(this, "Write to external storage permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraPermissionGranted = true;
                } else {
                    Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    /**
     * StackoverFlow Code
     */

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (result == null && uri.getPath() != null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static Bitmap getBitmapFromUrl(String url) throws IOException {
        return BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
    }


}
