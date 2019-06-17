package com.purpura.googlemaps2018.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.adapters.UserRecyclerAdapter;
import com.purpura.googlemaps2018.models.User;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class AddUserToChatFragment extends Fragment implements View.OnClickListener, UserRecyclerAdapter.UserListRecyclerClickListener {

    private OnUserSelectedListener onUserSelectedListener;
    private RecyclerView mUserListRecyclerView;
    private UserRecyclerAdapter mUserRecyclerAdapter;
    private ArrayList<User> mUserList = new ArrayList<>();

    public static AddUserToChatFragment newInstance() {
        return new AddUserToChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mUserList.size() == 0) { // make sure the list doesn't duplicate by navigating back
            if (getArguments() != null) {
                mUserList.addAll(getArguments().getParcelableArrayList(getString(R.string.intent_user_list)));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user_to_chat, container, false);
        mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        initUserListRecyclerView();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initUserListRecyclerView() {
        mUserRecyclerAdapter = new UserRecyclerAdapter(this, mUserList);
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onUserClicked(int position) {
        Log.d(TAG, "onUserClicked: selected a user: " + mUserList.get(position).getUser_id());
        User selectedUser = mUserList.get(position);
        onUserSelectedListener.addSelectedUserToChatroom(selectedUser);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onUserSelectedListener = (OnUserSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnUserSelectedListener");
        }
    }

    public interface OnUserSelectedListener {
        public void addSelectedUserToChatroom(User user);
    }
}
