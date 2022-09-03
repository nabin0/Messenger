package com.nabin.messenger.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nabin.messenger.adapters.ConversationAdapter;
import com.nabin.messenger.databinding.ActivityConversationBinding;
import com.nabin.messenger.models.ChatMessage;
import com.nabin.messenger.models.User;
import com.nabin.messenger.networks.ApiClient;
import com.nabin.messenger.networks.ApiService;
import com.nabin.messenger.utilities.BaseActivity;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationActivity extends BaseActivity {

    private ActivityConversationBinding binding;
    private ConversationAdapter conversationAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<ChatMessage> chatMessageList;
    private User receiverUser;
    private String conversationId;
    private boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadReceiverDetails();
        setListeners();
        init();
        listMessages();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        binding.layoutSend.setOnClickListener(view -> sendMessage());
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();

        //Set adapter to  recyclerview
        conversationAdapter = new ConversationAdapter(
                chatMessageList,
                getBitmapFromEncodedUrl(receiverUser.getImage()),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.conversationRecyclerView.setAdapter(conversationAdapter);

        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        if (binding.inputMessage.getText().toString().trim().isEmpty()) {
            return;
        }
        HashMap<String, Object> chatMessage = new HashMap<>();
        chatMessage.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        chatMessage.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        chatMessage.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        chatMessage.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .add(chatMessage);

        if (conversationId != null) {
            updateConversationConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.getName());
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.getImage());
            conversation.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversation);
        }

        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.getToken());

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MESSAGE_DATA, data);
                body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());

            } catch (Exception e) {
                showToast("sent failed" + e.getMessage());
            }
        }

        binding.inputMessage.setText(null);
    }

    private void listMessages() {
        // Iam sender You are receiver
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(eventListener);

        // You are sender Iam receiver
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    chatMessage.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    chatMessage.setDateTime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessageList.add(chatMessage);
                }
            }

            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.getDateObject()));
            if (count == 0) {
                conversationAdapter.notifyDataSetChanged();
            } else {
                conversationAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.conversationRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

        if (conversationId == null) {
            checkForConversationConversion();
        }
    };

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getApiClient().create(ApiService.class)
                .sendMessage(Constants.getRemoteMessageHeaders(),
                        messageBody).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            try {
                                if (response.body() != null) {
                                    JSONObject responseJson = new JSONObject(response.body());
                                    JSONArray results = responseJson.getJSONArray("results");
                                    if (responseJson.getInt("failure") == 1) {
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToast(error.getString("error"));
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            showToast("Notification sent successfully");
                        } else {
                            showToast("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showToast(t.getMessage());
                    }
                });
    }

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.getId())
                .addSnapshotListener(ConversationActivity.this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_USER_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_USER_AVAILABILITY))
                                    .intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.setToken(value.getString(Constants.KEY_FCM_TOKEN));
                        if (receiverUser.getImage() == null) {
                            receiverUser.setImage(value.getString(Constants.KEY_IMAGE));
                            conversationAdapter.setReceiverProfileImage(getBitmapFromEncodedUrl(receiverUser.getImage()));
                            conversationAdapter.notifyItemRangeChanged(0, chatMessageList.size());
                        }
                    }
                    if (isReceiverAvailable) {
                        // Receiver is available (online)
                        binding.textUserAvailability.setVisibility(View.VISIBLE);
                    } else {
                        //Receiver is not available (offline)
                        binding.textUserAvailability.setVisibility(View.GONE);
                    }

                });
    }

    void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textUserName.setText(receiverUser.getName());
    }

    private Bitmap getBitmapFromEncodedUrl(String image) {
        if (image != null) {
            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void checkForConversationConversion() {
        if (chatMessageList.size() != 0) {
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.getId());
            checkForConversionRemotely(receiverUser.getId(),
                    preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void updateConversationConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION).document(conversationId);
        HashMap<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_LAST_MESSAGE, message);
        update.put(Constants.KEY_TIMESTAMP, new Date());
        documentReference.update(update);
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                conversationId = documentSnapshot.getId();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}