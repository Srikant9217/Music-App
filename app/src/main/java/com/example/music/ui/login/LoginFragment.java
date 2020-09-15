package com.example.music.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.music.MainActivity;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {
    private EditText editTextUserName;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonRegister;
    private Button buttonLoginGoogle;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        editTextUserName = v.findViewById(R.id.edit_text_username);
        editTextPassword = v.findViewById(R.id.edit_text_password);
        buttonLogin = v.findViewById(R.id.button_login);
        buttonRegister = v.findViewById(R.id.button_register);
        buttonLoginGoogle = v.findViewById(R.id.button_login_google);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_userProfileFragment);
        }


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        buttonLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Will be implemented soon", Toast.LENGTH_SHORT).show();
            }
        });

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        return v;
    }

    private void loginUser(){
        firebaseAuth.signInWithEmailAndPassword(editTextUserName.getText().toString(),
                editTextPassword.getText().toString())
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            currentUser = firebaseAuth.getCurrentUser();
                            HomeFragment.updateUserUI();
                            Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_userProfileFragment);
                        }else {
                            Log.e(MainActivity.TAG, "Login failure", task.getException());
                            Toast.makeText(getActivity(), "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser() {
        firebaseAuth.createUserWithEmailAndPassword(editTextUserName.getText().toString(),
                editTextPassword.getText().toString())
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            HomeFragment.updateUserUI();

                            UserModel user = new UserModel(
                                    currentUser.getUid(),
                                    currentUser.getDisplayName(),
                                    String.valueOf(currentUser.getPhotoUrl()));
                            String uploadId = databaseRef.push().getKey();
                            databaseRef.child(uploadId).setValue(user);

                            Toast.makeText(getActivity(), "Registration Successful", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_userProfileFragment);
                        } else {
                            Log.e(MainActivity.TAG, "SignUp failure", task.getException());
                            Toast.makeText(getActivity(), "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}