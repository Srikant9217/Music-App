package com.example.music.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.music.R;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class DialogEditProfileImage extends AppCompatDialogFragment {
    public static final int PICK_PROFILE_IMAGE = 10;

    private ImageView imageView;
    private DialogEditProfileImageListener listener;

    private Uri imageUri;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_profile_image, null);

        imageView = view.findViewById(R.id.dialog_edit_profile_image);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenFileChooser();
            }
        });

        builder.setView(view)
                .setTitle("Set Profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.EditImage(imageUri);
                    }
                });
        return builder.create();
    }

    public void OpenFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_PROFILE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PROFILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.with(getActivity())
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
    }

    public interface DialogEditProfileImageListener{
        void EditImage(Uri uri);
    }

    public void setListener(DialogEditProfileImageListener listener){
        this.listener = listener;
    }
}

