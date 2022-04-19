package com.mustafaunlu.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.mustafaunlu.chatapp.databinding.ItemContainerUserBinding;
import com.mustafaunlu.chatapp.listeners.UserListener;
import com.mustafaunlu.chatapp.models.User;

import java.util.Base64;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private final List<User> userList;
    private final UserListener userListener;
    public UsersAdapter(List<User> userList, UserListener userListener){
        this.userList=userList;
        this.userListener=userListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding binding=ItemContainerUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(binding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserBinding binding;
        public UserViewHolder(ItemContainerUserBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        void setUserData(User user){
            binding.nameText.setText(user.name);
            binding.profileImage.setImageBitmap(getUserImage(user.image));
            binding.emailText.setText(user.email);
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

    }
}
