package com.mustafaunlu.chatapp.activities;

import androidx.annotation.RequiresApi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mustafaunlu.chatapp.adapters.ChatAdapter;
import com.mustafaunlu.chatapp.databinding.ActivityChatBinding;
import com.mustafaunlu.chatapp.models.ChatMessage;
import com.mustafaunlu.chatapp.models.User;
import com.mustafaunlu.chatapp.network.ApiClient;
import com.mustafaunlu.chatapp.network.ApiService;
import com.mustafaunlu.chatapp.utilities.Constants;
import com.mustafaunlu.chatapp.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId=null;
    private Boolean isReceiverAvailable=false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if(encodedImage !=null){
            byte[] bytes = Base64.getDecoder().decode(encodedImage);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }
        else{
            return null;
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void init(){
        preferenceManager=new PreferenceManager(getApplicationContext());
        chatMessageList=new ArrayList<>();
        chatAdapter=new ChatAdapter(chatMessageList,getBitmapFromEncodedString(receiverUser.image),preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database=FirebaseFirestore.getInstance();

    }
    private void sendMessage(){
        HashMap<String,Object> message=new HashMap<>();
        message.put(Constants.KEY_SENDERID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.messageInput.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId !=null){
            updateConversion(binding.messageInput.getText().toString());
        }else{
            HashMap<String,Object> conversion=new HashMap<>();
            conversion.put(Constants.KEY_SENDERID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.messageInput.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversation(conversion);
        }
        if(!isReceiverAvailable){
            try{
                JSONArray tokens=new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data =new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.messageInput.getText().toString());

                JSONObject body=new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());

            }catch (Exception e){
                showToast(e.getMessage());
            }
        }
        binding.messageInput.setText(null);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this,(value,error)->{
           if(error!=null)
               return;
           if(value !=null){
               if(value.getLong(Constants.KEY_AVAILABILITY)!=null){
                   int availability= Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                   isReceiverAvailable=availability==1;
               }
               receiverUser.token=value.getString(Constants.KEY_FCM_TOKEN);
               if(receiverUser.image==null){
                   receiverUser.image=value.getString(Constants.KEY_IMAGE);
                   chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                   chatAdapter.notifyItemRangeInserted(0,chatMessageList.size());
               }
           }
           if (isReceiverAvailable)
               binding.textAvailability.setVisibility(View.VISIBLE);
           else
               binding.textAvailability.setVisibility(View.GONE);

        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    try{
                        if(response.body()!=null){
                            JSONObject responseJson=new JSONObject(response.body());
                            JSONArray results=responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure")==1){
                                JSONObject error= (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }else{
                    showToast("Error: "+response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());

            }
        });
    }
    private void listenMessages(){
        //burayi incele
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDERID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDERID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener=(value, error) ->{
        if(error!=null)
            return ;
        if(value!=null){
            int count=chatMessageList.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderId=documentChange.getDocument().getString(Constants.KEY_SENDERID);
                    chatMessage.receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message=documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime=getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList,(obj1,obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            //Collections.sort(chatMessageList, Comparator.comparing(obj -> obj.dateObject));
            if(count==0){
                chatAdapter.notifyDataSetChanged();
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(),chatMessageList.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size()-1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversationId==null){
            checkForConversation();
        }

    };
    private void setListeners() {
        binding.backImage.setOnClickListener(v->onBackPressed());
    }

    private void loadReceiverDetails(){
        receiverUser= (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.nameText.setText(receiverUser.name);
        binding.sendLayout.setOnClickListener(v-> sendMessage());
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId=documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,new Date());
    }
    private void checkForConversation(){
        if(chatMessageList.size()!=0){
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.id);
            checkForConversationRemotely(receiverUser.id,preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }
    private void checkForConversationRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDERID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId=documentSnapshot.getId();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}