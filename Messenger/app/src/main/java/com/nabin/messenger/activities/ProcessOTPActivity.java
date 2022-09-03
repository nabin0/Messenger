package com.nabin.messenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nabin.messenger.databinding.ActivityProcessOtpactivityBinding;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ProcessOTPActivity extends AppCompatActivity {

    private ActivityProcessOtpactivityBinding binding;
    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private HashMap<String, Object> userData = new HashMap<>();
    private String phoneNo;
    private String starterActivity;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProcessOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        getIntentData();

        firebaseAuth = FirebaseAuth.getInstance();

        binding.textVerifyPhoneNumber.setText("Verify " + phoneNo);

        processOTP(phoneNo);

        binding.buttonVerifyWithOTP.setOnClickListener(view -> {
            if (isValidOTP()) {
                loading(true);

                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, binding.inputOTP.getText().toString());
                firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        //Store Data To FireStore
                        if (starterActivity.equals("signUp")) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(userData)
                                    .addOnSuccessListener(documentReference -> {
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                        preferenceManager.putString(Constants.KEY_NAME, (String) userData.get(Constants.KEY_NAME));
                                        preferenceManager.putString(Constants.KEY_IMAGE, (String) userData.get(Constants.KEY_IMAGE));
                                        showToast("Account Created successfully.");

                                        // Start MainActivity
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast(e.getMessage());
                                    });
                        } else if (starterActivity.equals("signIn")) {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, (String) userData.get(Constants.KEY_USER_ID));
                            preferenceManager.putString(Constants.KEY_NAME, (String) userData.get(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_IMAGE, (String) userData.get(Constants.KEY_IMAGE));
                            showToast("Signed In Successfully.");

                            // Start MainActivity
                            finishAffinity();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Invalid starter Activity", Toast.LENGTH_SHORT).show();
                        }

                        loading(false);

                    } else {
                        Toast.makeText(ProcessOTPActivity.this, "Unable to Sign In", Toast.LENGTH_SHORT).show();
                        loading(false);
                    }
                });
            }
        });

    }

    private boolean isValidOTP() {
        if (binding.inputOTP.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Your OTP", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.inputOTP.getText().toString().length() < 6) {
            Toast.makeText(this, "Enter a valid OTP", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void getIntentData() {
        phoneNo = getIntent().getStringExtra(Constants.KEY_PHONE);
        // TODO: Unchecked warning
        userData = (HashMap<String, Object>) getIntent().getSerializableExtra(Constants.USER_DATA);
        starterActivity = getIntent().getStringExtra(Constants.STARTER_ACTIVITY);
    }

    private void processOTP(String phoneNo) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(ProcessOTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(ProcessOTPActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        Log.d("MyTag", "onVerificationFailed: " + e);

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Log.d("MyTag", "onVerificationFailed: invalid cred");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Log.d("MyTag", "onVerificationFailed: too many request");
                        } else {
                            Log.d("MyTag", "onVerificationFailed: " + e);
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        Toast.makeText(ProcessOTPActivity.this, "code sent", Toast.LENGTH_SHORT).show();
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonVerifyWithOTP.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonVerifyWithOTP.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}