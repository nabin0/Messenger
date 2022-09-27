package com.nabin.messenger.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nabin.messenger.databinding.ActivityViewStoryBinding;
import com.nabin.messenger.models.StoryModel;
import com.nabin.messenger.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ViewStory extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    private ActivityViewStoryBinding binding;

    private String userId;
    private int counter = 0;
    private List<String> postUri;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set user data
        setData();

        binding.viewClickLeft.setOnClickListener(view -> {
            binding.storiesProgressView.reverse();
        });

        binding.viewClickLeft.setOnLongClickListener(view -> {
            binding.storiesProgressView.pause();
            return false;
        });
        binding.viewClickLeft.setOnTouchListener(onTouchListener);

        binding.viewClickRight.setOnClickListener(view -> {
            binding.storiesProgressView.skip();
        });

        binding.viewClickRight.setOnLongClickListener(view -> {
            binding.storiesProgressView.pause();
            return false;
        });
        binding.viewClickRight.setOnTouchListener(onTouchListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        postUri = new ArrayList<>();
        fetchStories();
    }

    private void fetchStories() {
        postUri.clear();

        database.collection(Constants.STORY_POST_DIRECTORY)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                StoryModel model = snapshot.toObject(StoryModel.class);
                                postUri.add(model.getMediaUrl());
                            }

                            binding.storiesProgressView.setStoriesCount(postUri.size());
                            binding.storiesProgressView.setStoryDuration(5000);
                            binding.storiesProgressView.setStoriesListener(ViewStory.this);
                            binding.storiesProgressView.startStories(counter);

                            Glide.with(getApplicationContext())
                                    .load(postUri.get(counter))
                                    .into(binding.imageStoryPost);
                            
                            binding.textUserName.setText("this");
                        } else {
                            Toast.makeText(ViewStory.this, "Some error occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setData() {
        if (getIntent().hasExtra(Constants.KEY_USER_ID)) {
            userId = getIntent().getStringExtra(Constants.KEY_USER_ID);

            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                binding.textUserName.setText(task.getResult().getString(Constants.KEY_NAME));
                                Glide.with(getApplicationContext())
                                        .load(getBitmapFromEncodedString(task.getResult().getString(Constants.KEY_IMAGE)))
                                        .into(binding.imageUserProfile);
                            } else {
                                Toast.makeText(ViewStory.this, "Unable to fetch user data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private Bitmap getBitmapFromEncodedString(String string) {
        if (string != null) {
            byte[] data = Base64.decode(string, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext())
                .load(postUri.get(++counter))
                .into(binding.imageStoryPost);
    }

    @Override
    public void onPrev() {
        if (counter - 1 < 0) return;
        Glide.with(getApplicationContext())
                .load(postUri.get(--counter))
                .into(binding.imageStoryPost);
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.storiesProgressView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding.storiesProgressView.destroy();
    }
}