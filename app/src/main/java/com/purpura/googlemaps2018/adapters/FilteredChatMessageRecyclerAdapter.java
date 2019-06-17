package com.purpura.googlemaps2018.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.models.ChatMessage;
import com.purpura.googlemaps2018.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FilteredChatMessageRecyclerAdapter extends RecyclerView.Adapter<FilteredChatMessageRecyclerAdapter.ViewHolder> {

    private static final String TAG = "FilteredChatMessageRecy";

    private Context mContext;
    private List<ChatMessage> filteredChatMessages;
    private FilteredChatMessagesRecyclerClickListener filteredChatMessagesRecyclerClickListener;

    public FilteredChatMessageRecyclerAdapter(Context mContext, List<ChatMessage> messages, FilteredChatMessagesRecyclerClickListener filteredChatMessagesRecyclerClickListener) {
        this.mContext = mContext;
        this.filteredChatMessages = messages;
        this.filteredChatMessagesRecyclerClickListener = filteredChatMessagesRecyclerClickListener;
    }

    @NonNull
    @Override
    public FilteredChatMessageRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_message_list_item_other, parent, false);
        return new FilteredChatMessageRecyclerAdapter.ViewHolder(view, filteredChatMessagesRecyclerClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilteredChatMessageRecyclerAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: ");
        ChatMessage chatMessage = filteredChatMessages.get(position);
        User user = chatMessage.getUser();

        if (FirebaseAuth.getInstance().getUid() != null && FirebaseAuth.getInstance().getUid().equals(user.getUser_id())) {
            holder.username.setTextColor(ContextCompat.getColor(mContext, R.color.green1));
        } else {
            holder.username.setTextColor(ContextCompat.getColor(mContext, R.color.blue2));
        }

        holder.username.setText(user.getUsername());

        if (chatMessage.hasMessage()) {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasMessage=true");
            holder.message.setText(chatMessage.getMessage());
        } else {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasMessage=false");
            holder.message.setVisibility(View.GONE);
        }

        if (chatMessage.hasImageUrl()) {
            Log.d(TAG, "onBindViewHolder: chatMessage.hasImageUrl=true");
            String imageUrl = chatMessage.getImageUrl();

            Picasso.get().load(imageUrl)
                    /*.resize(100, 100)*/
                    /*.fit()*/
                    .into(holder.image, new com.squareup.picasso.Callback() {

                        @Override
                        public void onSuccess() {
                            holder.image.setVisibility(View.VISIBLE);
                            holder.image.setAdjustViewBounds(true);
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    });


        } /* else: by default the imageView visibility is set to View.GONE */
    }


    @Override
    public int getItemCount() {
        return filteredChatMessages.size();
    }

    public final static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView message, username;
        ImageView image;

        FilteredChatMessagesRecyclerClickListener filteredChatMessagesRecyclerClickListener;

        public ViewHolder(View itemView, FilteredChatMessagesRecyclerClickListener filteredChatMessagesRecyclerClickListener) {
            super(itemView);
            this.message = itemView.findViewById(R.id.chat_message_message);
            this.username = itemView.findViewById(R.id.chat_message_username);
            this.image = itemView.findViewById(R.id.chat_message_image);

            this.filteredChatMessagesRecyclerClickListener = filteredChatMessagesRecyclerClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            filteredChatMessagesRecyclerClickListener.onChatMessageClicked(getAdapterPosition());
        }
    }


    public interface FilteredChatMessagesRecyclerClickListener {
        void onChatMessageClicked(int position);
    }

}
















