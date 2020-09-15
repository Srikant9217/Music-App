package com.example.music.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.music.R;

public class DialogAddPlaylist extends AppCompatDialogFragment {
    private EditText editTextName;
    private DialogPlaylistListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_playlist, null);

        editTextName = view.findViewById(R.id.dialog_playlist_name);

        builder.setView(view)
                .setTitle("Create Playlist")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String playlistName = editTextName.getText().toString();
                        listener.createPlaylist(playlistName);
                    }
                });
        return builder.create();
    }

    public interface DialogPlaylistListener{
        void createPlaylist(String playlistName);
    }

    public void setListener(DialogPlaylistListener listener){
        this.listener = listener;
    }
}
