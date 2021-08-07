package com.purpura.googlemaps2018.adapters;

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
        Log.d(TAG, "onBindViewHolder: ");
        Chatroom chatRoom = mChatrooms.get(position);

        String title = chatRoom.getTitle();
        if (chatRoom.getPredictedReviewRatingAverage() != null && chatRoom.getPredictedReviewRatingAverage() > 0){
            title = String.format("%s (%1.2f)", title, chatRoom.getPredictedReviewRatingAverage());
        }
        holder.chatroomTitle.setText(title);

        if (chatRoom.isPublic()) {
            holder.icon.setImageResource(R.drawable.ic_chat_public);
        } else {
            holder.icon.setImageResource(R.drawable.ic_chat_private);
        }
        if (chatRoom.getIsShowingNearby()) {
            holder.icon.setImageResource(R.drawable.ic_chat_nearby);
        }
        String imageUrl = null;

        if (chatRoom.hasImageUrl()) {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasImageUrl=true");
            imageUrl = chatRoom.getImageUrl();
        } else {
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
        private ImageView icon;

        public ViewHolder(View itemView, ChatroomRecyclerClickListener clickListener) {
            super(itemView);
            this.chatroomTitle = itemView.findViewById(R.id.chatroom_title);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
            this.image = itemView.findViewById(R.id.imageView);
            this.icon = itemView.findViewById(R.id.chatroom_access_icon);
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
















