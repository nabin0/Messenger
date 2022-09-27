package com.nabin.messenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nabin.messenger.R;
import com.nabin.messenger.adapters.MainActivityViewPagerFragmentsAdapter;
import com.nabin.messenger.databinding.ActivityMainBinding;
import com.nabin.messenger.utilities.BaseActivity;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.HashMap;

public class MainActivity extends BaseActivity {


    private ActivityMainBinding binding;
    private MainActivityViewPagerFragmentsAdapter viewPagerFragmentsAdapter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Messenger");

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            finish();
        }

        // Set Viewpager with tab-layout
        viewPagerFragmentsAdapter = new MainActivityViewPagerFragmentsAdapter(this);
        binding.viewPager.setAdapter(viewPagerFragmentsAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(Constants.TITLES[position]);
        }).attach();

        //Set Menu
        setToolbarMenu();

        getToken();

    }

    private void setToolbarMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_main_activity);
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.settings) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }else if(id == R.id.signOut){
                    signOut();
                    Toast.makeText(MainActivity.this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateFCMToken);
    }

    private void updateFCMToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Token Updation Failed."));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(update)
                .addOnSuccessListener(unused -> {
                    goToSignInActivity();
                }).addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    private void goToSignInActivity() {
        finishAffinity();
        preferenceManager.clear();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}