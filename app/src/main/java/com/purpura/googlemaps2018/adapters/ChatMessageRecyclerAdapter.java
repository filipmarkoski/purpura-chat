package com.purpura.googlemaps2018.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
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
import com.purpura.googlemaps2018.models.UserSetting;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatMessageRecyclerAdapter extends RecyclerView.Adapter<ChatMessageRecyclerAdapter.ViewHolder> {

    private static final String TAG = "ChatMessageRecyclerAdap";

    private ArrayList<ChatMessage> mMessages = new ArrayList<>();

    private Context mContext;

    private Resources.Theme theme;

    private HashMap<User, String> nicknames;

    public ChatMessageRecyclerAdapter(Context context, ArrayList<ChatMessage> messages, Resources.Theme theme, ArrayList<UserSetting> settings) {
        this.mContext = context;
        this.mMessages = messages;
        this.theme = theme;
        nicknames = new HashMap<>();
        for (UserSetting userSetting : settings)
            nicknames.put(userSetting.getUser(), userSetting.getUserNickname());
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
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
        if (FirebaseAuth.getInstance().getUid().equals(user.getUser_id())) {
            // current user
            ((ViewHolder) holder).username.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));

        } else {
            ((ViewHolder) holder).username.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        String nickname = nicknames.get(user);
        if (nickname != null) {
            ((ViewHolder) holder).username.setText(nickname);
        } else {
            ((ViewHolder) holder).username.setText(user.getUsername());
        }
        if (chatMessage.getTimestamp() != null)
            ((ViewHolder) holder).timestamp.setText(format.format(chatMessage.getTimestamp()));
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

            Picasso.get().load(imageUrl)
                    .resize(100, 100)
                    /*.fit()*/
                    .into(holder.image, new com.squareup.picasso.Callback() {

                        @Override
                        public void onSuccess() {
                            holder.image.setVisibility(View.VISIBLE);
                            holder.image.setAdjustViewBounds(true);
                            holder.image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // TODO: Open image in web browser
                                    Intent viewImageIntent = new Intent();
                                    viewImageIntent.setAction(Intent.ACTION_VIEW);
                                    viewImageIntent.setData(Uri.parse(imageUrl));
                                    mContext.startActivity(viewImageIntent);
                                }
                            });
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
        return mMessages.size();
    }

    public final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, username, timestamp;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            this.message = itemView.findViewById(R.id.chat_message_message);
            this.username = itemView.findViewById(R.id.chat_message_username);
            this.image = itemView.findViewById(R.id.chat_message_image);
            this.timestamp = itemView.findViewById(R.id.chat_message_timestamp);
        }
    }


    public interface FilteredChatMessagesRecyclerClickListener {
        void onChatMessageClicked(int position);
    }

}
















