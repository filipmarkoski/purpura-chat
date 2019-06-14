package com.purpura.googlemaps2018.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.models.ChatMessage;
import com.purpura.googlemaps2018.models.User;
import com.purpura.googlemaps2018.ui.ChatroomActivity;

import java.io.IOException;
import java.util.ArrayList;

public class ChatMessageRecyclerAdapter extends RecyclerView.Adapter<ChatMessageRecyclerAdapter.ViewHolder> {

    private static final String TAG = "ChatMessageRecyclerAdap";

    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private ArrayList<User> mUsers = new ArrayList<>();
    private Context mContext;

    public ChatMessageRecyclerAdapter(ArrayList<ChatMessage> messages, ArrayList<User> users, Context context) {
        this.mMessages = messages;
        this.mUsers = users;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_message_list_item_other, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: ");
        ChatMessage chatMessage = mMessages.get(position);
        User user = chatMessage.getUser();

        if (FirebaseAuth.getInstance().getUid().equals(user.getUser_id())) {
            ((ViewHolder) holder).username.setTextColor(ContextCompat.getColor(mContext, R.color.green1));
        } else {
            ((ViewHolder) holder).username.setTextColor(ContextCompat.getColor(mContext, R.color.blue2));
        }

        ((ViewHolder) holder).username.setText(user.getUsername());

        if (chatMessage.hasMessage()) {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasMessage=true");
            ((ViewHolder) holder).message.setText(chatMessage.getMessage());
        } else {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasMessage=false");
            ((ViewHolder) holder).message.setVisibility(View.GONE);
        }

        if (chatMessage.hasImageUrl()) {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasImageUrl=true");
            String imageUrl = chatMessage.getImageUrl();
            try {
                Bitmap bitmapFromUrl = ChatroomActivity.getBitmapFromUrl(imageUrl);
                ((ViewHolder) holder).image.setImageBitmap(bitmapFromUrl);
                ((ViewHolder) holder).image.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                String message = String.format("%s's image failed to load", user.getUsername());
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }/* else {
            ((ViewHolder) holder).image.setVisibility(View.GONE);
        }*/
    }


    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, username;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            this.message = itemView.findViewById(R.id.chat_message_message);
            this.username = itemView.findViewById(R.id.chat_message_username);
            this.image = itemView.findViewById(R.id.chat_message_image);
        }
    }


}
















