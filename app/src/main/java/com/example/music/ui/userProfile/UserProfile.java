package com.example.music.ui.userProfile;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.Dialogs.DialogAddSongsToPlaylist;
import com.example.music.Dialogs.DialogEditProfileImage;
import com.example.music.Dialogs.DialogEditProfileName;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class UserProfile extends Fragment implements DialogEditProfileImage.DialogEditProfileImageListener,
        DialogEditProfileName.DialogEditProfileNameListener {
    private ImageView imageViewProfile;
    private TextView textViewName;
    private ImageView editImage;
    private ImageView editName;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private String uploadId;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        imageViewProfile = v.findViewById(R.id.image_view_profile);
        textViewName = v.findViewById(R.id.text_view_name);
        editImage = v.findViewById(R.id.edit_image);
        editName = v.findViewById(R.id.edit_name);

        storageRef = FirebaseStorage.getInstance().getReference("Users");
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogForImage();
            }
        });

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogForName();
            }
        });

        databaseRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserModel user = dataSnapshot.getValue(UserModel.class);

                            String uri = user.getImageUrl();
                            if (uri != null && uri.length() != 0) {
                                Picasso.with(getActivity())
                                        .load(user.getImageUrl())
                                        .fit()
                                        .centerCrop()
                                        .into(imageViewProfile);
                            }
                            textViewName.setText(user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return v;
    }

    private void openDialogForImage() {
        DialogEditProfileImage setImage = new DialogEditProfileImage();
        setImage.setListener(this);
        setImage.show(getActivity().getSupportFragmentManager(), "Set Profile Image");
    }

    private void openDialogForName() {
        DialogEditProfileName setName = new DialogEditProfileName();
        setName.setListener(this);
        setName.show(getActivity().getSupportFragmentManager(), "Set Profile Name");
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    @Override
    public void EditImage(Uri uri) {
        if (uri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." +
                    getFileExtension(uri));
            fileReference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> getUrlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!getUrlTask.isSuccessful()) ;
                            final Uri downloadUri = getUrlTask.getResult();
                            databaseRef.orderByChild("userId").equalTo(currentUser.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                UserModel user = dataSnapshot.getValue(UserModel.class);
                                                user.setImageUrl(downloadUri.toString());
                                                Picasso.with(getActivity())
                                                        .load(user.getImageUrl())
                                                        .fit()
                                                        .centerCrop()
                                                        .into(imageViewProfile);
                                                uploadId = dataSnapshot.getKey();
                                                databaseRef.child(uploadId).setValue(user);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "please Select Image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void editName(final String name) {
        if (name != null) {
            databaseRef.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserModel user = dataSnapshot.getValue(UserModel.class);
                                user.setName(name);
                                textViewName.setText(name);
                                uploadId = dataSnapshot.getKey();
                                databaseRef.child(uploadId).setValue(user);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }
}