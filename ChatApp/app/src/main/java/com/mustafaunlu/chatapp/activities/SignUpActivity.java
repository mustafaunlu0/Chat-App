package com.mustafaunlu.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mustafaunlu.chatapp.R;
import com.mustafaunlu.chatapp.databinding.ActivitySignUpBinding;
import com.mustafaunlu.chatapp.utilities.Constants;
import com.mustafaunlu.chatapp.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        preferenceManager=new PreferenceManager(getApplicationContext());
    }
    private void setListeners(){
        binding.SignInText.setOnClickListener(v->onBackPressed());
        binding.signUpButton.setOnClickListener(v->{
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.imageLayout.setOnClickListener(v->{
            //burada izin alınmalı kullanıcı izin verilirse gidilmeli!!Projene ekle
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    private  void signUp(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.emailInput.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.passwordInput.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS).add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                }).addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());

        });

    }
    private final ActivityResultLauncher<Intent> pickImage=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
        if(result.getResultCode()==RESULT_OK){
            if(result.getData() !=null){
                Uri imageUri=result.getData().getData();
                try{
                    InputStream inputStream= getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                    binding.imageProfile.setImageBitmap(bitmap);
                    binding.addImageTextView.setVisibility(View.GONE);
                    encodedImage=encodeImage(bitmap);


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    );
    @RequiresApi(api = Build.VERSION_CODES.O)
    private  String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);


    }
    private Boolean isValidSignUpDetails(){
        if(encodedImage==null){
            showToast("Select profile image");
            return false;
        }
        else if(binding.inputName.getText().toString().trim().isEmpty()){
            //trim fonksiyonu text ön ve arkasındaki boşlukları kaldırır.
            showToast("Enter name");
            return false;
        }
        else if(binding.emailInput.getText().toString().trim().isEmpty()){
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
        else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        }
        else if(!binding.inputConfirmPassword.getText().toString().equals(binding.passwordInput.getText().toString())){
            showToast("Password & confirm password must be same");
            return false;
        }
        else{
            return true;
        }

    }
    private  void loading(Boolean isLoading){
        if(isLoading){
            binding.signUpButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.signUpButton.setVisibility(View.VISIBLE);
        }
    }

}