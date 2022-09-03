package com.nabin.messenger.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nabin.messenger.databinding.ItemContainerRecentConversationUserBinding;
import com.nabin.messenger.listeners.RecentConversationUserListener;
import com.nabin.messenger.models.ChatMessage;
import com.nabin.messenger.models.User;

import java.util.List;

public class RecentConversationUsersAdapter extends RecyclerView.Adapter<RecentConversationUsersAdapter.RecentConversationViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final RecentConversationUserListener recentConversationUserListener;

    public RecentConversationUsersAdapter(List<ChatMessage> chatMessageList, RecentConversationUserListener recentConversationUserListener) {
        this.chatMessageList = chatMessageList;
        this.recentConversationUserListener = recentConversationUserListener;
    }

    @NonNull
    @Override
    public RecentConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentConversationViewHolder(ItemContainerRecentConversationUserBinding.inflate(LayoutInflater.from(
                parent.getContext()
        ), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationViewHolder holder, int position) {
            holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class RecentConversationViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerRecentConversationUserBinding binding;

        RecentConversationViewHolder(ItemContainerRecentConversationUserBinding itemContainerRecentConversationUserBinding) {
            super(itemContainerRecentConversationUserBinding.getRoot());
            binding = itemContainerRecentConversationUserBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversationBitmap(chatMessage.getConversationImage()));
            binding.textName.setText(chatMessage.getConversationName());
            binding.textRecent.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.setId(chatMessage.getConversationId());
                user.setImage(chatMessage.getConversationImage());
                user.setName(chatMessage.getConversationName());
                recentConversationUserListener.onUserClicked(user);
            });
        }
    }

    private Bitmap getConversationBitmap(String encodedString) {
        if(encodedString == null) return null;
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
