package com.purpura.googlemaps2018.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.adapters.FilteredChatMessageRecyclerAdapter;
import com.purpura.googlemaps2018.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class FilteredChatMessagesFragment extends Fragment implements View.OnClickListener, FilteredChatMessageRecyclerAdapter.FilteredChatMessagesRecyclerClickListener {


    private static final String TAG = "FilteredChatMessagesFra";


    private final FilteredChatMessagesFragment filteredChatMessagesFragment = this;

    public interface OnChatMessageSelectedListener {
        void findSelectChatMessageInChatroom(ChatMessage chatMessage);
    }

    private List<ChatMessage> filteredChatMessages;
    private RecyclerView filteredChatMessageRecyclerView;
    private FilteredChatMessageRecyclerAdapter filteredChatMessageRecyclerAdapter;
    private OnChatMessageSelectedListener onChatMessageSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onChatMessageSelectedListener = (OnChatMessageSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement onChatMessageSelectedListener");
        }
    }

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
        this.filteredChatMessageRecyclerAdapter =
                new FilteredChatMessageRecyclerAdapter(
                        getContext(),
                        filteredChatMessages,
                        this
                );

        this.filteredChatMessageRecyclerView.setAdapter(this.filteredChatMessageRecyclerAdapter);
        this.filteredChatMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onClick(View v) {
        // unused
        Log.d(TAG, "onClick: ");
    }

    @Override
    public void onChatMessageClicked(int position) {
        Log.d(TAG, "onChatMessageClicked: ");
        ChatMessage chatMessage = filteredChatMessages.get(position);
        onChatMessageSelectedListener.findSelectChatMessageInChatroom(chatMessage);
    }
}
