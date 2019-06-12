package com.purpura.googlemaps2018.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.app.FragmentTransaction;
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

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.UserClient;
import com.purpura.googlemaps2018.adapters.ChatMessageRecyclerAdapter;
import com.purpura.googlemaps2018.models.ChatMessage;
import com.purpura.googlemaps2018.models.Chatroom;
import com.purpura.googlemaps2018.models.User;
import com.purpura.googlemaps2018.models.UserLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatroomActivity extends AppCompatActivity implements
        View.OnClickListener, AddUserToChatFragment.OnUserSelectedListener {

    private static final String TAG = "ChatroomActivity";

    //widgets
    private Chatroom mChatroom;
    private EditText mMessage;

    //vars
    private ListenerRegistration mChatMessageEventListener, mUserListEventListener;
    private RecyclerView mChatMessageRecyclerView;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private FirebaseFirestore mDb;
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

        findViewById(R.id.checkmark).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        getIncomingIntent();
        initChatroomRecyclerView();

    }

    private void getUserLocation(int pos) {
        User user = mChatroom.getUsers().get(pos).getUser();
        DocumentReference locationsRef = mDb
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
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
                });*/
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
                            if (mMessages.size() > 0) {
                                mChatMessageRecyclerView.smoothScrollToPosition(
                                        mChatMessageRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });

    }


    private void insertNewMessage() {
        String message = mMessage.getText().toString();

        if (!message.equals("")) {
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
                .document(FirebaseAuth.getInstance().getUid());
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
                .document(FirebaseAuth.getInstance().getUid());

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

        joinChatroomRef.set(mChatroom);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkmark: {
                insertNewMessage();
            }
        }
    }

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
}
