package com.example.music.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.music.R;

public class DialogEditProfileName extends AppCompatDialogFragment {
    private EditText editTextName;
    private DialogEditProfileNameListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_profile_name, null);

        editTextName = view.findViewById(R.id.dialog_edit_profile_name);

        builder.setView(view)
                .setTitle("Set Username")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = editTextName.getText().toString();
                        listener.editName(name);
                    }
                });
        return builder.create();
    }

    public interface DialogEditProfileNameListener{
        void editName(String name);
    }

    public void setListener(DialogEditProfileNameListener listener){
        this.listener = listener;
    }
}

