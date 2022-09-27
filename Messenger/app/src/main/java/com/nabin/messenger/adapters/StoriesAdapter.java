package com.nabin.messenger.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nabin.messenger.databinding.ItemContainerUserStoryBinding;
import com.nabin.messenger.listeners.StoryListener;
import com.nabin.messenger.models.StoryModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.StoryViewHolder> {

    private final List<StoryModel> storiesList;
    private final StoryListener storyListener;
    private  Context context;

    public StoriesAdapter(List<StoryModel> storiesList, StoryListener storyListener, Context context) {
        this.storiesList = storiesList;
        this.storyListener = storyListener;
        this.context = context;
    }



    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserStoryBinding binding = ItemContainerUserStoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new StoryViewHolder(binding, storyListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.setStoryData(storiesList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return storiesList.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUserStoryBinding binding;
        private final StoryListener storyListener;

        public StoryViewHolder(ItemContainerUserStoryBinding itemContainerUserStoryBinding, StoryListener storyListener) {
            super(itemContainerUserStoryBinding.getRoot());
            binding = itemContainerUserStoryBinding;
            this.storyListener = storyListener;
        }

        void setStoryData(StoryModel model, Context context) {
            binding.textName.setText(model.getUserName());
            binding.textDateAndTime.setText(model.getUploadDate());

            Glide.with(context)
                    .load(model.getMediaUrl())
                    .into(binding.imageStoryPost);

            binding.getRoot().setOnClickListener(view -> {
                storyListener.onStoryClicked(model);
            });
        }

    }
}
