package com.mustafaunlu.chatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mustafaunlu.chatapp.databinding.ItemContainerReceivedMessageBinding;
import com.mustafaunlu.chatapp.databinding.ItemContainerSendMessageBinding;
import com.mustafaunlu.chatapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECEIVED=2;
    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessageList, Bitmap receiverProfileImage, String senderId) {
        this.chatMessageList = chatMessageList;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(ItemContainerSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));

        }else{
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));

        }



    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessageList.get(position));
        }else{
            ((ReceivedMessageViewHolder)holder).setData(chatMessageList.get(position),receiverProfileImage);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }


    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSendMessageBinding binding;

        SentMessageViewHolder(ItemContainerSendMessageBinding binding){
            super(binding.getRoot());
            this.binding=binding;

        }
        void setData(ChatMessage chatMessage){
            binding.messageText.setText(chatMessage.message);
            binding.dateTimeText.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage){
            binding.messageText.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receiverProfileImage !=null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }

}
