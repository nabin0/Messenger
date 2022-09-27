package com.nabin.messenger.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nabin.messenger.R;
import com.nabin.messenger.databinding.ActivitySignUpBinding;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private boolean userExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        // If already signed in goto main activity
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        binding.pickerCountryCode.registerCarrierNumberEditText(binding.inputMobileNumber);

        setClickListeners();
    }

    private void setClickListeners() {
        binding.textSignIn.setOnClickListener(this);
        binding.buttonSignUp.setOnClickListener(this);
        binding.layoutUserImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.textSignIn) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.buttonSignUp) {
            if (isValidSignUpDetails()) {

                String inputPhone = binding.pickerCountryCode.getFullNumberWithPlus().replace(" ", "");
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_PHONE, inputPhone)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                                // User is already registered
                                showToast("This number is already registered.");
                            } else {
                                // User is not already registered
                                signUp();
                            }
                        });

            }
        } else if (id == R.id.layoutUserImage) {
            pickImage.launch("image/*");
        } else {
            Toast.makeText(this, "Invalid Click", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignUpDetails() {
        // TODO: Complete Validation
        if (encodedImage == null) {
            showToast("Select Profile Image.");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter Name");
            return false;
        } else if (binding.inputMobileNumber.getText().toString().trim().isEmpty()) {
            showToast("Enter Mobile Number");
            return false;
        } else if (!binding.pickerCountryCode.isValidFullNumber()) {
            showToast("Enter a Valid Phone Number");
            return false;
        } else if (!Patterns.PHONE.matcher(binding.inputMobileNumber.getText().toString()).matches()) {
            showToast("Enter Valid Phone Number");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm Your Password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & Confirm Password must be same");
            return false;
        } else {
            return true;
        }
    }


    private void signUp() {
        loading(true);

        HashMap<String, Object> userData = new HashMap<>();
        userData.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        userData.put(Constants.KEY_PHONE, binding.pickerCountryCode.getFullNumberWithPlus().replace(" ", ""));
        userData.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        userData.put(Constants.KEY_IMAGE, encodedImage);

        Intent intent = new Intent(SignUpActivity.this, ProcessOTPActivity.class);
        intent.putExtra(Constants.USER_DATA, userData);
        intent.putExtra(Constants.KEY_PHONE, binding.pickerCountryCode.getFullNumberWithPlus().replace(" ", ""));
        intent.putExtra(Constants.STARTER_ACTIVITY, "signUp");
        startActivity(intent);

        loading(false);
    }

    // Grab Image From Device

    ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(result);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                binding.imageProfile.setImageBitmap(bitmap);
                encodedImage = encodeImage(bitmap);
                binding.textAddImage.setVisibility(View.INVISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    });

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}