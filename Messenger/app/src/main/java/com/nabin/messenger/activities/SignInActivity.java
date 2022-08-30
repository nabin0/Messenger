package com.nabin.messenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nabin.messenger.R;
import com.nabin.messenger.databinding.ActivitySignInBinding;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setClickListener();

        binding.pickerCountryCode.registerCarrierNumberEditText(binding.inputMobileNumber);

    }

    private void setClickListener() {
        binding.textCreateNewAccount.setOnClickListener(this);
        binding.buttonSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.textCreateNewAccount) {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.buttonSignIn) {
            if (isValidSignInDetails()) {
                signIn();
            }
        } else {
            Toast.makeText(this, "Not valid Click", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidSignInDetails() {
        // TODO: Complete Validation
        if (binding.inputMobileNumber.getText().toString().trim().isEmpty()) {
            showToast("Enter Phone Number");
            return false;
        } else if (!Patterns.PHONE.matcher(binding.inputMobileNumber.getText().toString()).matches()) {
            showToast("Enter a valid phone number");
            return false;
        } else if (!binding.pickerCountryCode.isValidFullNumber()) {
            showToast("Enter a valid Phone Number");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else {
            return true;
        }
    }


    private void signIn() {
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE, binding.pickerCountryCode.getFullNumberWithPlus().replace(" ", ""))
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);

                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put(Constants.KEY_USER_ID, snapshot.getId());
                        userData.put(Constants.KEY_NAME, snapshot.get(Constants.KEY_NAME));
                        userData.put(Constants.KEY_PHONE, binding.pickerCountryCode.getFullNumberWithPlus().replace(" ", ""));
                        userData.put(Constants.KEY_IMAGE, snapshot.get(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), ProcessOTPActivity.class);
                        intent.putExtra(Constants.USER_DATA, userData);
                        intent.putExtra(Constants.KEY_PHONE, binding.pickerCountryCode.getFullNumberWithPlus().replace(" ", ""));
                        intent.putExtra(Constants.STARTER_ACTIVITY, "signIn");
                        startActivity(intent);
                        loading(false);
                    } else {
                        Toast.makeText(this, "Unable to sign in.", Toast.LENGTH_SHORT).show();
                        loading(false);
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

}