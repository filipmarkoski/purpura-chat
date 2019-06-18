package com.purpura.googlemaps2018.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.purpura.googlemaps2018.Constants;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.models.Chatroom;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatroomRecyclerAdapter extends RecyclerView.Adapter<ChatroomRecyclerAdapter.ViewHolder>{

    private static final String TAG = "ChatroomRecyclerAdapter";

    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private ChatroomRecyclerClickListener mChatroomRecyclerClickListener;

    public ChatroomRecyclerAdapter(ArrayList<Chatroom> chatrooms, ChatroomRecyclerClickListener chatroomRecyclerClickListener) {
        this.mChatrooms = chatrooms;
        mChatroomRecyclerClickListener = chatroomRecyclerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chatroom_list_item, parent, false);
        return new ViewHolder(view, mChatroomRecyclerClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((ViewHolder)holder).chatroomTitle.setText(mChatrooms.get(position).getTitle());

        Log.d(TAG, "onBindViewHolder: ");
        Chatroom chatMessage = mChatrooms.get(position);

        String imageUrl = null;

        if (chatMessage.hasImageUrl()) {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasImageUrl=true");
            imageUrl = chatMessage.getImageUrl();
        } else {
            // use a default image URL if there is none
            imageUrl = Constants.DEFAULT_CHATROOM_IMAGE_URL;
        }

        if (imageUrl != null) {
            Picasso.get().load(imageUrl)
                    .resize(80, 80)
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return mChatrooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener
    {
        TextView chatroomTitle;
        ChatroomRecyclerClickListener clickListener;
        private ImageView image;

        public ViewHolder(View itemView, ChatroomRecyclerClickListener clickListener) {
            super(itemView);
            this.chatroomTitle = itemView.findViewById(R.id.chatroom_title);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
            this.image = itemView.findViewById(R.id.imageView);
        }

        @Override
        public void onClick(View v) {
            clickListener.onChatroomSelected(getAdapterPosition());
        }
    }

    public interface ChatroomRecyclerClickListener {
        public void onChatroomSelected(int position);
    }
}
















