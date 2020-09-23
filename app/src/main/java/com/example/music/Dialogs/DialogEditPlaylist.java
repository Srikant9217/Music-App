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

public class DialogEditPlaylist extends AppCompatDialogFragment {
    private EditText editTextName;
    private DialogPlaylistListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_playlist, null);

        final int position = getArguments().getInt("position");
        String currentName = getArguments().getString("currentName");
        editTextName = view.findViewById(R.id.dialog_playlist_name);
        editTextName.setText(currentName);

        builder.setView(view)
                .setTitle("Edit Playlist")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String playlistName = editTextName.getText().toString();
                        listener.editPlaylistName(playlistName, position);
                    }
                });
        return builder.create();
    }

    public interface DialogPlaylistListener{
        void editPlaylistName(String playlistName, int position);
    }

    public void setListener(DialogPlaylistListener listener){
        this.listener = listener;
    }
}
