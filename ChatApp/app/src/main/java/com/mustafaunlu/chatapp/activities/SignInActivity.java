package com.mustafaunlu.chatapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustafaunlu.chatapp.databinding.ActivitySignInBinding;
import com.mustafaunlu.chatapp.utilities.Constants;
import com.mustafaunlu.chatapp.utilities.PreferenceManager;


public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }
    private void setListeners(){
        binding.createNewAccountText.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.signInButton.setOnClickListener(v->{
            if(isValidSignInDetails()){
                signIn();
            }
        });
    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.emailInput.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.passwordInput.getText().toString())
                .get()
                .addOnCompleteListener(task->{
                   if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                       DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                       preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                       preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                       preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                       Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }
                   else{
                       loading(false);
                       showToast("Unable to sign in");
                   }
                });

    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signInButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.signInButton.setVisibility(View.VISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails(){
        if(binding.emailInput.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }
        else if(binding.passwordInput.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else{
            return true;
        }

    }


}