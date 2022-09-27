package com.nabin.messenger.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nabin.messenger.activities.ConversationActivity;
import com.nabin.messenger.activities.UsersActivity;
import com.nabin.messenger.adapters.RecentConversationUsersAdapter;
import com.nabin.messenger.databinding.FragmentChatsBinding;
import com.nabin.messenger.listeners.RecentConversationUserListener;
import com.nabin.messenger.models.ChatMessage;
import com.nabin.messenger.models.User;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChatsFragment extends Fragment implements RecentConversationUserListener {

    private FragmentChatsBinding binding;
    private List<ChatMessage> conversationUserList;
    private RecentConversationUsersAdapter conversationUsersAdapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        setClickListeners();
        listenConversationConversion();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        conversationUserList = new ArrayList<>();
        conversationUsersAdapter = new RecentConversationUsersAdapter(conversationUserList, this);
        binding.recentConversationUsersRecyclerView.setAdapter(conversationUsersAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversationConversion() {
        //When I first sent the message
        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        // When You first sent me message
        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        // When i first send message iam the sender

                        chatMessage.setConversationImage(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));
                        chatMessage.setConversationName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        chatMessage.setConversationId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    } else {
                        //When other send me message they will be sender
                        chatMessage.setConversationImage(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                        chatMessage.setConversationName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        chatMessage.setConversationId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));

                    conversationUserList.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversationUserList.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversationUserList.get(i).getSenderId().equals(senderId) && conversationUserList.get(i).getReceiverId().equals(receiverId)) {
                            conversationUserList.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            conversationUserList.get(i).setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversationUserList, (obj1, obj2) -> obj2.getDateObject().compareTo(obj1.getDateObject()));
            conversationUsersAdapter.notifyDataSetChanged();
            try {
                binding.recentConversationUsersRecyclerView.smoothScrollToPosition(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.recentConversationUsersRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void setClickListeners() {
        binding.fabNewChat.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), UsersActivity.class));
        });
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getContext(), ConversationActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}