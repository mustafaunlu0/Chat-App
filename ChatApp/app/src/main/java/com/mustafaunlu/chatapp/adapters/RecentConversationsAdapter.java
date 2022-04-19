package com.mustafaunlu.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.mustafaunlu.chatapp.databinding.ItemContainerUserRecentConversionBinding;
import com.mustafaunlu.chatapp.listeners.ConversionListener;
import com.mustafaunlu.chatapp.models.ChatMessage;
import com.mustafaunlu.chatapp.models.User;

import java.util.Base64;
import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {
    private final List<ChatMessage> chatMessageList;
    private ConversionListener conversionListener;
    public RecentConversationsAdapter(List<ChatMessage> chatMessageList,ConversionListener conversionListener) {
        this.chatMessageList = chatMessageList;
        this.conversionListener=conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(ItemContainerUserRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerUserRecentConversionBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        void setData(ChatMessage chatMessage){
            binding.profileImage.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.nameText.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v-> {
                User user=new User();
                user.id=chatMessage.conversionId;
                user.name=chatMessage.conversionName;
                user.image=chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes= Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
