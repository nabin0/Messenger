package com.nabin.messenger.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.nabin.messenger.databinding.ActivityEditStoryBinding;
import com.nabin.messenger.models.StoryModel;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.Date;

public class EditStoryActivity extends AppCompatActivity {

    private ActivityEditStoryBinding binding;
    private Uri imageUri;
    private FirebaseFirestore database;
    private FirebaseStorage firebaseStorage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Get intent and set it
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            imageUri = Uri.parse(bundle.getString(Constants.STORY_POST_MEDIA));
            binding.imageStoryPost.setImageURI(imageUri);
        } else {
            showToast("No uri found");
        }

        binding.textUploadStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.progressBar.setVisibility(View.VISIBLE);
                uploadStory();
                binding.progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    private void uploadStory() {
        firebaseStorage.getReference(Constants.STORY_POST_DIRECTORY).putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        firebaseStorage.getReference(Constants.STORY_POST_DIRECTORY).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {


                                StoryModel story = new StoryModel();
                                story.setUserName(preferenceManager.getString(Constants.KEY_NAME));
                                story.setUserId(preferenceManager.getString(Constants.KEY_USER_ID));
                                story.setCaption(binding.editTextCaption.getText().toString().trim());
                                story.setUploadDate(new Date().toString());
                                story.setMediaUrl(uri.toString());
                                story.setEndDate(System.currentTimeMillis());

                                database.collection(Constants.STORY_POST_DIRECTORY)
                                        .add(story).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    showToast("Status Uploaded successfully.");
                                                }
                                            }
                                        });

                                database.collection(Constants.ALL_STORY_POST_DIRECTORY)
                                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                        .set(story)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    showToast("Story updated");
                                                }
                                            }
                                        });
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Upload failed" + e.getMessage());
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}