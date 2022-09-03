package com.nabin.messenger.utilities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        reference = database.collection(Constants.KEY_COLLECTION_USERS).
                document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.update(Constants.KEY_USER_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reference.update(Constants.KEY_USER_AVAILABILITY, 1);
    }
}
