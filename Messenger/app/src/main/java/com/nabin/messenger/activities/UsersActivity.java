package com.nabin.messenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nabin.messenger.adapters.UsersAdapter;
import com.nabin.messenger.databinding.ActivityUsersBinding;
import com.nabin.messenger.listeners.UserListener;
import com.nabin.messenger.models.User;
import com.nabin.messenger.utilities.BaseActivity;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }

    private void getUsers() {
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);

                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.getId().equals(currentUserId)) {
                                continue;
                            }

                            User user = new User();
                            user.setName(documentSnapshot.getString(Constants.KEY_NAME));
                            user.setPhone(documentSnapshot.getString(Constants.KEY_PHONE));
                            user.setToken(documentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            user.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));

                            user.setId(documentSnapshot.getId());
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter adapter = new UsersAdapter(users, this);
                            binding.userRecyclerView.setAdapter(adapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No User Available."));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}