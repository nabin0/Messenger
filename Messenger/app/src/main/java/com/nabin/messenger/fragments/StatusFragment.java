package com.nabin.messenger.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nabin.messenger.R;
import com.nabin.messenger.activities.EditStoryActivity;
import com.nabin.messenger.activities.ViewStory;
import com.nabin.messenger.adapters.StoriesAdapter;
import com.nabin.messenger.databinding.FragmentStatusBinding;
import com.nabin.messenger.listeners.StoryListener;
import com.nabin.messenger.models.StoryModel;
import com.nabin.messenger.utilities.Constants;
import com.nabin.messenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;


public class StatusFragment extends Fragment implements View.OnClickListener, StoryListener {

    private FragmentStatusBinding binding;
    private PreferenceManager preferenceManager;

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init necessary vars
        init();

        // Show Userdata
        showData();

        // listeners
        setListeners();
    }

    @Override
    public void onStart() {
        super.onStart();

        // set recycler view
        getStories();
    }


    private void getStories() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.ALL_STORY_POST_DIRECTORY)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            List<StoryModel> allStories = new ArrayList<>();
                            for (QueryDocumentSnapshot snapshot : task.getResult()){
                                StoryModel story = new StoryModel();
                                story.setUserName(snapshot.getString("userName"));
                                story.setMediaUrl(snapshot.getString("mediaUrl"));
                                story.setUserId(snapshot.getString("userId"));
                                story.setUploadDate(snapshot.getString("uploadDate"));

                                allStories.add(story);
                            }

                            if(allStories.size() > 0){
                                StoriesAdapter adapter = new StoriesAdapter(allStories, StatusFragment.this, getContext());
                                LinearLayoutManager layoutManager =  new LinearLayoutManager(getContext());
                                binding.storiesRecyclerView.setLayoutManager(layoutManager);
                                binding.storiesRecyclerView.setAdapter(adapter);
                            }else{
                                showToast("no stories found");
                            }

                        }
                    }
                });
    }

    private void setListeners() {
        binding.myStatusUploadContainerConstraintLayout.setOnClickListener(this);
    }

    private void init() {
        preferenceManager = new PreferenceManager(getContext());
    }

    private void showData() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.textMessageOrTime.setText("Tap to add status update");
        binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_IMAGE)));
    }

    private Bitmap getBitmapFromEncodedString(String encodedString) {
        if (encodedString == null) return null;
        byte[] data = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.myStatusUploadContainerConstraintLayout) {
            pickImage.launch("image/*");
        } else {
            showToast("Invalid click");
        }
    }

    // Grab Image From Device
    ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            try {
                Intent intent = new Intent(getContext(), EditStoryActivity.class);
                intent.putExtra(Constants.STORY_POST_MEDIA, result.toString());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStoryClicked(StoryModel storyModel) {
        Intent intent = new Intent(getContext(), ViewStory.class);
        intent.putExtra(Constants.KEY_USER_ID, storyModel.getUserId());
        startActivity(intent);
    }
}