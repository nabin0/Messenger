package com.nabin.messenger.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nabin.messenger.databinding.ItemContainerUserBinding;
import com.nabin.messenger.listeners.UserListener;
import com.nabin.messenger.models.User;

import java.util.List;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        public UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.getName());
            binding.textContactInformation.setText(user.getPhone());
            binding.imageProfile.setImageBitmap(getUserImage(user.getImage()));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        if (encodedImage == null) return null;
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
