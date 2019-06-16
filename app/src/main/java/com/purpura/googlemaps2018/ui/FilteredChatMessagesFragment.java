package com.purpura.googlemaps2018.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.adapters.ChatMessageRecyclerAdapter;
import com.purpura.googlemaps2018.models.ChatMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FilteredChatMessagesFragment extends Fragment implements View.OnClickListener, ChatMessageRecyclerAdapter.FilteredChatMessagesRecyclerClickListener {


    public interface OnChatMessageSelectedListener {
        void findSelectChatMessageInChatroom(ChatMessage chatMessage);
    }

    private ArrayList<ChatMessage> filteredChatMessages;
    private RecyclerView filteredChatMessageRecyclerView;
    private ChatMessageRecyclerAdapter chatMessageRecyclerAdapter;
    private OnChatMessageSelectedListener onChatMessageSelectedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filteredChatMessages = new ArrayList<>();
        if (/*filteredChatMessages.size() == 0 && */getArguments() != null) {
            String parcelKey = getString(R.string.intent_filtered_chat_messages);
            List<ChatMessage> filteredChatMessagesList = getArguments().getParcelableArrayList(parcelKey);
            if (filteredChatMessagesList != null) {
                filteredChatMessages.addAll(filteredChatMessagesList);
            }
        }
    }


    public static FilteredChatMessagesFragment newInstance() {
        return new FilteredChatMessagesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom_filtered_chat_messages, container, false);
        filteredChatMessageRecyclerView = view.findViewById(R.id.filtered_chat_message_recycler_view);
        initFilteredChatMessageRecyclerView();
        return view;
    }

    private void initFilteredChatMessageRecyclerView() {
        // chatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(this, filteredChatMessages);
        // TODO; make a separate RecyclerAdapter for this fragment
    }

    @Override
    public void onClick(View v) {
        // unused
    }

    @Override
    public void onChatMessageClicked(int position) {

    }
}
