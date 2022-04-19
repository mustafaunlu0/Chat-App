package com.mustafaunlu.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mustafaunlu.chatapp.adapters.UsersAdapter;
import com.mustafaunlu.chatapp.databinding.ActivityUsersBinding;
import com.mustafaunlu.chatapp.listeners.UserListener;
import com.mustafaunlu.chatapp.models.User;
import com.mustafaunlu.chatapp.utilities.Constants;
import com.mustafaunlu.chatapp.utilities.PreferenceManager;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }
    private void setListeners(){
        binding.backImage.setOnClickListener(v-> onBackPressed());
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId=preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() !=null){
                        List<User> userList=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email=queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image=queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id= queryDocumentSnapshot.getId();;
                            userList.add(user);
                        }
                        if(userList.size()>0){
                            UsersAdapter usersAdapter=new UsersAdapter(userList,this);
                            binding.recyclerView.setAdapter(usersAdapter);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }
                    }
                    else{
                        showErrorMessage();
                    }
                });

    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void showErrorMessage(){
        binding.errorMessageTextView.setText((String.format("%s","No user available")));
        binding.errorMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}