package com.nabin.messenger.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nabin.messenger.databinding.ActivitySettingsBinding;
import com.nabin.messenger.utilities.BaseActivity;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setUserData();

        setListeners();

    }

    private void setUserData() {
        loading(true);

        binding.imageUserProfile.setImageBitmap(getImageFromEncodedString(preferenceManager.getString(Constants.KEY_IMAGE)));
        binding.textUserName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.textPhoneNumber.setText(preferenceManager.getString(Constants.KEY_PHONE));
        binding.collapsingToolbarLayout.setTitle(preferenceManager.getString(Constants.KEY_NAME));

//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if(document.exists()){
//                            binding.imageUserProfile.setImageBitmap(getImageFromEncodedString(document.getString(Constants.KEY_IMAGE)));
//                            binding.textUserName.setText(document.getString(Constants.KEY_NAME));
//                            binding.textPhoneNumber.setText(document.getString(Constants.KEY_PHONE));
//                            binding.collapsingToolbarLayout.setTitle(document.getString(Constants.KEY_NAME));
//                        }
//                    }
//                });

        loading(false);
    }

    private Bitmap getImageFromEncodedString(String keyImage) {
        if (keyImage == null) return null;
        byte[] bytes = Base64.decode(keyImage, Base64.DEFAULT);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

    private void setListeners() {
        binding.fabSelectUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage.launch("image/*");
            }
        });

        binding.layoutUpdateUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserName();
            }
        });

        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(result);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageUserProfile.setImageBitmap(bitmap);
                        encodedImage = encodeImage(bitmap);
                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                        //Update to firebase
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferenceManager.getString(Constants.KEY_USER_ID)).update(Constants.KEY_IMAGE, encodedImage)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        showToast("Profile Image Updated Successfully");
                                    }
                                });

                        firebaseFirestore.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION)
                                .get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                            if (snapshot.getString(Constants.KEY_SENDER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                                firebaseFirestore.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION).document(snapshot.getId())
                                                        .update(Constants.KEY_SENDER_IMAGE, encodedImage);
                                            } else {
                                                firebaseFirestore.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION).document(snapshot.getId())
                                                        .update(Constants.KEY_RECEIVER_IMAGE, encodedImage);
                                            }
                                        }
                                    }
                                });

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });


    private void updateUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Username");
        builder.setMessage("Enter Your new username");

        //Set edittext to get input form user
        final EditText input = new EditText(this);
        input.setHint("Enter Your Username");
        builder.setView(input);

        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Save new username
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .update(Constants.KEY_NAME, input.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                preferenceManager.putString(Constants.KEY_NAME, input.getText().toString().trim());
                                binding.textUserName.setText(input.getText().toString().trim());
                                binding.collapsingToolbarLayout.setTitle(input.getText().toString());
                                showToast("Username updated successfully.");
                            }
                        });

                database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                    if (snapshot.getString(Constants.KEY_SENDER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION).document(snapshot.getId())
                                                .update(Constants.KEY_SENDER_NAME, input.getText().toString().trim());
                                    } else {
                                        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATION).document(snapshot.getId())
                                                .update(Constants.KEY_RECEIVER_NAME, input.getText().toString().trim());
                                    }
                                }
                            }
                        });
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();

    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}