package com.purpura.googlemaps2018.adapters;


import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.models.User;
import com.purpura.googlemaps2018.models.UserSetting;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    private ArrayList<User> mUsers = new ArrayList<>();
    private UserListRecyclerClickListener mClickListener;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public UserRecyclerAdapter(ArrayList<UserSetting> userSettings, UserListRecyclerClickListener clickListener) {
        mUsers = (ArrayList<User>) userSettings.stream().map(UserSetting::getUser).collect(Collectors.toList());
        mClickListener = clickListener;
    }

    public UserRecyclerAdapter(UserListRecyclerClickListener clickListener, ArrayList<User> users) {
        mUsers = users;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_list_item, parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ((ViewHolder) holder).username.setText(mUsers.get(position).getUsername());
        ((ViewHolder) holder).email.setText(mUsers.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView username, email;
        UserListRecyclerClickListener mClickListener;

        public ViewHolder(View itemView, UserListRecyclerClickListener clickListener) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);

            mClickListener = clickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onUserClicked(getAdapterPosition());
        }
    }

    public interface UserListRecyclerClickListener {
        void onUserClicked(int position);
    }
}

















