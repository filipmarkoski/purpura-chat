package com.purpura.googlemaps2018.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.text.emoji.widget.EmojiAppCompatEditText;
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
import android.widget.Switch;
import android.widget.ToggleButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatroomActivity extends AppCompatActivity implements
        AddUserToChatFragment.OnUserSelectedListener,
        FilteredChatMessagesFragment.OnChatMessageSelectedListener {

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
    private StorageReference mImagesFolderRef;

    // Image (the same variables are used for Gallery-related and Camera-related tasks)
    /*private Bitmap galleryImage = null;
    private String galleryImageDownloadURL = null;*/

    //widgets
    private Chatroom mChatroom;
    private EditText editTextMessage;

    //vars
    private ListenerRegistration mChatMessageEventListener, mUserListEventListener;
    private RecyclerView mChatMessageRecyclerView;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();
    Switch locationSwitch;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

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
        editTextMessage = (EmojiAppCompatEditText) findViewById(R.id.input_message);
        mChatMessageRecyclerView = findViewById(R.id.chatmessage_recycler_view);

        View btnCheckmark = findViewById(R.id.checkmark);
        btnCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "(btnCheckmark) onClick: ");
                Toast.makeText(chatroomActivity, "Sending message...", Toast.LENGTH_SHORT).show();
                insertNewMessage();
            }
        });

        View btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "(btnGallery) onClick: ");
                Toast.makeText(chatroomActivity, "Opening gallery...", Toast.LENGTH_SHORT).show();
                if (checkWriteExternalStoragePermissionGranted()) {
                    getGalleryImage();
                }
            }
        });

        View btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "(btnCamera) onClick: ");
                Toast.makeText(chatroomActivity, "Opening camera...", Toast.LENGTH_SHORT).show();
                if (true || checkCameraPermissionGranted()) {
                    getCameraImage();
                }
            }
        });

        // Initialize Firebase-related
        mDb = FirebaseFirestore.getInstance();
        locationSwitch = (Switch) findViewById(R.id.location_switch);
        /*locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableShareLocationInChatRoom();
                } else {
                    disableShareLocationInChatRoom();
                }
            }
        });*/
        locationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationSwitch.isChecked()) {
                    enableShareLocationInChatRoom();
                } else {
                    disableShareLocationInChatRoom();
                }
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mImagesFolderRef = mStorageRef.child("images");
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        getIncomingIntent();
        initChatroomRecyclerView();
    }


    private void storeImageDownloadUrlToCloudFirestore(String imageDownloadURL) {
        Log.d(TAG, "storeImageDownloadUrlToCloudFirestore: ");

        /* Make sure you have the image */
        if (imageDownloadURL != null) {
            // Store the imageUrl to Cloud Firestore
            CollectionReference chatroomChatMessagesRef = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages));

            DocumentReference chatMessageRef = chatroomChatMessagesRef.document();

            String chatMessageId = chatMessageRef.getId();
            User user = getCurrentUser();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage_id(chatMessageId);
            chatMessage.setUser(user);
            chatMessage.setImageUrl(imageDownloadURL);

            final Task<Void> storeChatMessageTask = chatMessageRef.set(chatMessage);

            storeChatMessageTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    String message = "storeImageDownloadUrlToCloudFirestore: onSuccess ";
                    Log.d(TAG, message);
                    Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String message = "storeImageDownloadUrlToCloudFirestore: onFailure ";
                    Log.d(TAG, message);
                    Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    // SELECT_GALLERY_IMAGE_ACTION

    private void getGalleryImage() {
        Log.d(TAG, "getGalleryImage: ");
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "getGalleryImage: intent resolved");
            Intent galleryChooser = Intent.createChooser(galleryIntent, "Select an Image");
            startActivityForResult(galleryChooser, Constants.SELECT_GALLERY_IMAGE_REQUEST);
        }
    }

    private void getChatroomImageFromGallery() {
        Log.d(TAG, "getGalleryImage: ");
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "getChatroomImageFromGallery: ");
            Intent galleryChooser = Intent.createChooser(galleryIntent, "Select an Image");
            startActivityForResult(galleryChooser, Constants.SELECT_CHATROOM_IMAGE_REQUEST);
        }
    }

    private void getCameraImage() {
        Log.d(TAG, "getCameraImage: ");

        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "getCameraImage: intent resolved");


            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (imageFile != null) {
                Log.d(TAG, "getCameraImage: file created successfully");
                Toast.makeText(chatroomActivity, imageFile.toString(), Toast.LENGTH_SHORT).show();

                /*
                 * https://developer.android.com/reference/android/support/v4/content/FileProvider
                 * */

                if (!imageFile.exists()) {
                    try {
                        imageFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Uri imageContentUriFromFile = FileProvider.getUriForFile(chatroomActivity, BuildConfig.APPLICATION_ID + ".fileprovider", imageFile);
                Toast.makeText(chatroomActivity, imageContentUriFromFile.getPath(), Toast.LENGTH_SHORT).show();

                // get read and write permissions, the | is a bitwise-OR, it's the standard way to combine the two flags
                // cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageContentUriFromFile);
                cameraIntent.putExtra("return-data", true);


                // an approach to send the content uri via an intent exists using ClipData objects

                Intent cameraChooser = Intent.createChooser(cameraIntent, "Select a Camera App");
                chatroomActivity.startActivityForResult(cameraChooser, Constants.CAPTURE_CAMERA_IMAGE_REQUEST);
            }
        }
    }

    // android:authorities="com.purpura.googlemaps2018.fileprovider"

    String mCurrentPhotoPath = null;

    public File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );


        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");

        // Gallery Intent
        if (requestCode == Constants.SELECT_GALLERY_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {

            Log.d(TAG, "onActivityResult: SELECT_GALLERY_IMAGE_REQUEST");

            Uri imageUri = data.getData();
            try {
                storeImageToFirebaseStorage(imageUri, requestCode);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        // Chatroom Image Intent
        else if (requestCode == Constants.SELECT_CHATROOM_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {

            Log.d(TAG, "onActivityResult: SELECT_CHATROOM_IMAGE_REQUEST");

            Uri imageUri = data.getData();
            try {
                storeImageToFirebaseStorage(imageUri, requestCode);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        // Camera Intent
        else if (requestCode == Constants.CAPTURE_CAMERA_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK
            /*&& data != null && data.getExtras() != null*/) {

            Log.d(TAG, "onActivityResult: CAPTURE_CAMERA_IMAGE_REQUEST");

            Uri imageUri = Uri.fromFile(new File(mCurrentPhotoPath));

            try {
                storeImageToFirebaseStorage(imageUri, requestCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeImageToFirebaseStorage(Uri uri, Integer requestCode) throws IOException {
        Log.d(TAG, "storeImageToFirebaseStorage: ");
        if (uri == null) {
            Log.e(TAG, "storeImageToFirebaseStorage: uri is null", new NullPointerException());
            return;
        }

        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        String message = String.format("ImageReceived=%s, %s", imageBitmap != null, uri);
        Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();

        String fileName = getFileName(uri);
        String userId = getCurrentUser().getUser_id();
        // String timestamp = Calendar.getInstance().getTime().toString().replaceAll("[\\s+:]", "_");
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = String.format("userId_%s_timestamp_%s_fileName_%s", userId, timeStamp, fileName);
        final StorageReference imageRef = mImagesFolderRef.child(imageName);
        final UploadTask uploadTask = imageRef.putFile(uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(chatroomActivity, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(chatroomActivity, "Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "storeImageToFirebaseStorage: onComplete: success");
                            String imageDownloadUrl = task.getResult().toString();
                            Toast.makeText(chatroomActivity, imageDownloadUrl, Toast.LENGTH_SHORT).show();
                            if (requestCode == Constants.SELECT_GALLERY_IMAGE_REQUEST) {
                                chatroomActivity.storeImageDownloadUrlToCloudFirestore(imageDownloadUrl);
                            } else if (requestCode == Constants.SELECT_CHATROOM_IMAGE_REQUEST) {
                                mChatroom.setImageUrl(imageDownloadUrl);
                                DocumentReference chatroomDocument = mDb
                                        .collection(getString(R.string.collection_chatrooms))
                                        .document(mChatroom.getChatroom_id());
                                chatroomDocument.set(mChatroom);
                            }
                        } else {
                            // Handle failures
                            String message = "Firebase Storage upload task failed";
                            Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        /*imageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.d(TAG, "storeImageToFirebaseStorage: onComplete: success");
                    String imageDownloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                    Toast.makeText(chatroomActivity, imageDownloadUrl, Toast.LENGTH_SHORT).show();
                    chatroomActivity.storeImageDownloadUrlToCloudFirestore(imageDownloadUrl);
                } else {
                    // Handle failures
                    String message = "Firebase Storage upload task failed";
                    Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }


    private void getUserLocation(int pos) {
        Log.d(TAG, "getUserLocation: ");
        User user = mChatroom.getUsers().get(pos).getUser();
        DocumentReference locationsRef = mDb
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().toObject(UserLocation.class) != null) {
                        UserLocation userLocation = task.getResult().toObject(UserLocation.class);

                        mChatroom.getUsers().get(pos).setUserLocation(userLocation);
                    }
                }
            }
        });

    }

    // the first time the function is called is odd, thus the first time results in true
    private boolean theNumberTheFunctionIsCalledIsOdd = true;
    private int numberOfMessagesEntered = 0;

    private void getChatMessages() {
        Log.d(TAG, "getChatMessages: ");

        CollectionReference messagesRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chat_messages));

        // get the last 100 messages
        mChatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100L)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage chatMessage = doc.toObject(ChatMessage.class);

                                if (!mMessageIds.contains(chatMessage.getMessage_id())) {
                                    mMessageIds.add(chatMessage.getMessage_id());
                                    mMessages.add(chatMessage);
                                    mChatMessageRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                                }
                            }

                            // reverse the last one thousand messages so they come in ascending order
                            // as they were naturally written
                            if (numberOfMessagesEntered == 0) {
                                Collections.reverse(mMessages);
                            }
                            numberOfMessagesEntered += 1;
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }


    private void insertNewMessage() {
        Log.d(TAG, "insertNewMessage: ");
        String lineSeparator = System.getProperty("line.separator");
        String message = editTextMessage.getText().toString().trim();

        if (message.length() > 0 && lineSeparator != null) {
            message = message.replaceAll(lineSeparator, "");

            DocumentReference chatMessageDoc = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages))
                    .document();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage(message);
            chatMessage.setMessage_id(chatMessageDoc.getId());
            chatMessage.setTimestamp(null);

            User user = ((UserClient) (getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            chatMessage.setUser(user);

            chatMessageDoc.set(chatMessage);/*.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {



                        // getChatMessages();

                        // TODO: use Firebase Messaging To Send A Notification Message
                    } else {
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });*/

            chatroomActivity.editTextMessage.setText("");
            mMessageIds.add(chatMessage.getMessage_id());
            mMessages.add(chatMessage);
            mChatMessageRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void clearMessage() {
        Log.d(TAG, "clearMessage: ");
        editTextMessage.setText("");
    }


    private void getChatroomUsers() {
        Log.d(TAG, "getChatroomUsers: ");
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
        Log.d(TAG, "initChatroomRecyclerView: ");
        mChatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(chatroomActivity, mMessages);
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
        Log.d(TAG, "hideSoftKeyboard: ");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent: ");
        if (getIntent().hasExtra(getString(R.string.intent_chatroom))) {
            Log.d(TAG, "getIncomingIntent: hasExtra");
            mChatroom = getIntent().getParcelableExtra(getString(R.string.intent_chatroom));
            displayChatroomName();
            joinChatroom();
        }
    }

    private DocumentReference getCurrentUserSetting() {
        Log.d(TAG, "getCurrentUserSetting: ");
        return mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(mAuth.getUid());
    }

    private void leaveChatroom() {
        Log.d(TAG, "leaveChatroom: ");
        User user = getCurrentUser();
        mChatroom.removeUser(user);
        finish();
    }

    private void disableShareLocationInChatRoom() {
        User user = getCurrentUser();
        mChatroom.disableUserLocation(user);
        DocumentReference userSettingRef = getCurrentUserSetting();

        userSettingRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    restartListFragmentIfVisible();
                }
            }
        });

    }

    private void enableShareLocationInChatRoom() {
        User user = getCurrentUser();
        mChatroom.enableUserLocation(user);
        DocumentReference userSettingRef = getCurrentUserSetting();

        userSettingRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    restartListFragmentIfVisible();
                }
            }
        });

    }

    private void toggleShareLocationInChatroom() {
        Log.d(TAG, "toggleShareLocationInChatroom: ");
        User user = getCurrentUser();
        Boolean nowEnabled = mChatroom.toggleUserLocation(user);
        DocumentReference userSettingRef = getCurrentUserSetting();

        userSettingRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(chatroomActivity, nowEnabled ? "Location is being shared" : "Location is not shared", Toast.LENGTH_SHORT).show();
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
        // return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void joinChatroom() {
        Log.d(TAG, "joinChatroom: ");

        String chatroomId = mChatroom.getChatroom_id();
        String userId = mAuth.getUid();

        if (chatroomId != null && userId != null) {
            DocumentReference joinedChatroomRef = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(chatroomId)
                    .collection(getString(R.string.collection_chatroom_user_list))
                    .document(userId);

            User user = getCurrentUser();
            mChatroom.addUser(user);
            joinedChatroomRef.set(user); // Don't care about listening for completion.
        } else {
            boolean chatroomIdFound = chatroomId != null;
            boolean userIdFound = userId != null;
            String message = String.format("User %s, Chatroom %s",
                    userIdFound ? "found" : "not found",
                    chatroomIdFound ? "found" : "not found");
            Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
        }
    }


    private void displayChatroomName() {
        Log.d(TAG, "displayChatroomName: ");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mChatroom.getTitle());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        // getIncomingIntent();
        // initChatroomRecyclerView();
        // locationSwitch.setChecked(mChatroom.isLocationEnabled(getCurrentUser()));

        getChatMessages();
        getChatroomUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

        if (mChatroom != null) {
            // find the currently joined chatroom
            DocumentReference chatroomRef = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id());
            if (mChatroom.getUsers().isEmpty() && mChatroom.getPrivate()) {
                chatroomRef.delete();
            } else {
                // You shouldn't have to store or update the chatroom.
                // It should already be stored piece-by-piece

                chatroomRef.set(mChatroom);
            }

        }

        // remove ChatMessage listener
        if (mChatMessageEventListener != null) {
            mChatMessageEventListener.remove();
        }

        // remove User listener
        if (mUserListEventListener != null) {
            mUserListEventListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    UsersInChatFragment getUsersInChatFragment() {
        return (UsersInChatFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));

    }

    Boolean isUsersInChatFragmentVisible() {
        Log.d(TAG, "isUsersInChatFragmentVisible: ");
        UsersInChatFragment fragment = getUsersInChatFragment();
        return fragment != null;// && fragment.isVisible();

    }

    public void restartListFragmentIfVisible() {
        Log.d(TAG, "restartListFragmentIfVisible: ");
        UsersInChatFragment fragment = getUsersInChatFragment();
        if (fragment != null) {

            getSupportFragmentManager()
                    .beginTransaction().
                    remove(fragment).commit();
            inflateUsersInChatFragment();

        }

    }

    private boolean isAddUserToChatFragmentVisible() {
        Log.d(TAG, "isAddUserToChatFragmentVisible: ");
        AddUserToChatFragment fragment =
                (AddUserToChatFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_add_user_to_chatroom));
        return fragment != null && fragment.isVisible();
    }


    @Override
    public void addSelectedUserToChatroom(User user) {
        Log.d(TAG, "addSelectedUserToChatroom: ");

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id());

        mChatroom.addUser(user);
        joinChatroomRef.set(mChatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ChatroomActivity.super.onBackPressed();
                    restartListFragmentIfVisible();
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


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
            case R.id.action_chatroom_delete: {
                displayDeleteChatroomDialog();
                return true;
            }
            case R.id.action_chatroom_rename: {
                displayRenameChatroomDialog();
                return true;
            }
            case R.id.action_chatroom_search: {
                displaySearchChatroomDialog();
                return true;
            }
            case R.id.action_chatroom_image: {
                if (checkWriteExternalStoragePermissionGranted()) {
                    getChatroomImageFromGallery();
                }
                return true;
            }
            case R.id.action_chatroom_add_user: {
                inflateAddUserToChatFragment();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }


    /**
     * Dialog display functions
     */

    private void displayDeleteChatroomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(chatroomActivity);
        String deleteKeyword = "Delete";
        String alertDialogTitle = String.format("Enter '%s' to delete the chatroom", deleteKeyword);
        builder.setTitle(alertDialogTitle);

        View renameChatroomDialog = View.inflate(this, R.layout.layout_chatroom_rename, null);

        EditText editDeleteKeyword = (EditText) renameChatroomDialog.findViewById(R.id.edit_chatroom_name);

        builder.setView(renameChatroomDialog).setCancelable(false);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredKeyword = editDeleteKeyword.getText().toString().trim();
                if (!enteredKeyword.isEmpty() && enteredKeyword.equals(deleteKeyword)) {
                    deleteChatroom(mChatroom);
                } else {
                    String message = String.format("You didn't enter '%s' correctly", deleteKeyword);
                    Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
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

    private void displayRenameChatroomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(chatroomActivity);
        builder.setTitle("Enter a chatroom name");

        View renameChatroomDialog = View.inflate(this, R.layout.layout_chatroom_rename, null);

        EditText editChatroomName = (EditText) renameChatroomDialog.findViewById(R.id.edit_chatroom_name);

        editChatroomName.setText(mChatroom.getTitle());
        editChatroomName.setSelection(editChatroomName.getText().length());

        builder.setView(renameChatroomDialog).setCancelable(false);

        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String chatroomName = editChatroomName.getText().toString().trim();
                if (!chatroomName.isEmpty()) {
                    renameChatroom(chatroomName);
                } else {
                    Toast.makeText(chatroomActivity, "Enter new chatroom name", Toast.LENGTH_SHORT).show();
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

    /**
     * TODO: Try making a prediction or suggestion in choosing the targetString
     * https://stackoverflow.com/a/29101069/3950168
     * UserDictionary.Words.addWord(....)
     */
    private void displaySearchChatroomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(chatroomActivity);
        builder.setTitle("Enter a sequence of letters to search by");

        View searchChatroomDialog = View.inflate(this, R.layout.layout_chatroom_find, null);

        EditText editChatMessageTargetString = (EditText) searchChatroomDialog.findViewById(R.id.edit_chat_message_target);

        editChatMessageTargetString.setText("");
        editChatMessageTargetString.setSelection(editChatMessageTargetString.getText().length());

        builder.setView(searchChatroomDialog).setCancelable(false);

        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String targetString = editChatMessageTargetString.getText().toString().trim();
                if (!targetString.isEmpty()) {
                    List<ChatMessage> filteredChatMessages = searchChatMessagesByTargetString(targetString);

                    inflateFilteredChatMessagesFragment(filteredChatMessages);

                } else {
                    Toast.makeText(chatroomActivity, "Enter a word to filter the messages by", Toast.LENGTH_SHORT).show();
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

    /**
     * Dialog-related worker functions
     */

    private void deleteChatroom(Chatroom chatroom) {
        DocumentReference joinedChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(chatroom.getChatroom_id());

        // AppCompatActivity appCompatActivity = super;

        joinedChatroomRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String message = String.format("Chatroom '%s' deleted", chatroom.getTitle());
                mChatroom = null;
                Toast.makeText(chatroomActivity, message, Toast.LENGTH_SHORT).show();
               /* if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    appCompatActivity.onBackPressed();
                }*/
                finish();
            }
        });

        /*Intent mainActivityIntent = new Intent(chatroomActivity, MainActivity.class);
        startActivity(mainActivityIntent);*/

        // remove back to delete chatroom


    }

    private void renameChatroom(String title) {
        DocumentReference joinedChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id());

        mChatroom.setTitle(title);

        joinedChatroomRef.set(mChatroom)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ", e);
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: ");
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: ");
                View parentLayout = findViewById(android.R.id.content);
                if (task.isSuccessful()) {
                    // TODO: Check if the title changes immediately after the chat has been renamed
                    if (chatroomActivity.getSupportActionBar() != null) {
                        chatroomActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                        chatroomActivity.getSupportActionBar().setTitle(mChatroom.getTitle());
                    }
                    String message = String.format("Chatroom renamed to %s", title);
                    Snackbar.make(parentLayout, message, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(parentLayout, "Failed to rename the chatroom", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @param targetString the string which were are trying to find within the messages in the chatroom
     */
    private List<ChatMessage> searchChatMessagesByTargetString(/*Chatroom chatroom, */String targetString) {
        if (mMessages != null && mMessages.size() > 0) {
            List<ChatMessage> filteredChatMessages = new ArrayList<>();

            /*
             * Firebase doesn't offer full-text search, for such operations
             * external services like Algolia or ElasticSearch are needed
             * they both offer 14 day free trials for non-commercial products
             * */

            // The messages should be pre-fetched and ordered by timestamp already
            // The messages are limited to a maximum numberOfMessagesEntered of 1000
            for (ChatMessage chatMessage : mMessages) {
                if (chatMessage != null && chatMessage.hasMessage()) {

                    String message = chatMessage.getMessage().trim().toLowerCase();
                    String target = targetString.trim().toLowerCase();
                    if (message.contains(target)) {
                        filteredChatMessages.add(chatMessage);
                    }

                }
            }

            return filteredChatMessages;
        }
        return null;
    }

    /**
     * Inflation-related functions
     */

    private void inflateUsersInChatFragment() {
        Log.d(TAG, "inflateUsersInChatFragment: ");
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
        Log.d(TAG, "inflateAddUserToChatFragment: ");
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

    private void inflateFilteredChatMessagesFragment(List<ChatMessage> filteredChatMessages) {
        Log.d(TAG, "inflateFilteredChatMessagesFragment: ");
        hideSoftKeyboard();

        FilteredChatMessagesFragment fragment = FilteredChatMessagesFragment.newInstance();
        Bundle bundle = new Bundle();
        String intentKey = getString(R.string.intent_filtered_chat_messages);
        bundle.putParcelableArrayList(intentKey, (ArrayList<ChatMessage>) filteredChatMessages);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.chatroom_fragment_container, fragment, getString(R.string.fragment_filtered_chat_messages));
        transaction.addToBackStack(getString(R.string.fragment_filtered_chat_messages));
        transaction.commit();
    }

    /**
     * Permission-related Code
     */

    private boolean checkWriteExternalStoragePermissionGranted() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int permissionRequest = Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
        return checkPermissionGranted(this, permission, permissionRequest);
    }

    private boolean checkCameraPermissionGranted() {
        String permission = android.Manifest.permission.CAMERA;
        int permissionRequest = Constants.PERMISSIONS_REQUEST_CAMERA;
        return checkPermissionGranted(this, permission, permissionRequest);
    }

    private boolean checkPermissionGranted(Activity activity, String permission, int permissionRequest) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            String message = String.format("%s granted", permission);
            Log.d(TAG, message);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            String message = String.format("%s NOT granted", permission);
            Log.d(TAG, message);

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                String[] split = permission.split(".");
                String permissionName = split[split.length - 1];

                String snackbarText = String.format("%s permission is needed", permissionName);
                Snackbar.make(findViewById(android.R.id.content), snackbarText, Snackbar.LENGTH_INDEFINITE)
                        .setAction("ENABLE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{permission},
                                        permissionRequest);
                            }
                        })
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
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");

        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Write to external storage permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Write to external storage permission is NOT granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case Constants.PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Camera permission is NOT granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * StackoverFlow Code
     */

    public String getFileName(Uri uri) {
        Log.d(TAG, "getFileName: ");
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        Log.d(TAG, "getFileName: cursor.moveToFirst");
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (fileName == null && uri.getPath() != null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                Log.d(TAG, "getFileName: fileName.substring");
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    /**
     * Unused Code
     */

    private String getFilePath(Uri uri) {
        Log.d(TAG, "getFilePath: ");
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
    public void findSelectChatMessageInChatroom(ChatMessage chatMessage) {
        Toast.makeText(chatroomActivity, chatMessage.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
