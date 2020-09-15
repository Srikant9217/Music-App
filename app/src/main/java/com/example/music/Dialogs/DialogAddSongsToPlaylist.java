package com.example.music.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.music.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DialogAddSongsToPlaylist extends AppCompatDialogFragment {
    private AutoCompleteTextView autoCompleteAddSongs;
    private DialogAddSongsListener listener;

    private ArrayList<String> songList;

    private DatabaseReference DatabaseRef;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_songs_to_playlist, null);

        autoCompleteAddSongs = view.findViewById(R.id.auto_complete_add_songs);

        songList = new ArrayList<>();
        DatabaseRef = FirebaseDatabase.getInstance().getReference("Song");

        DatabaseRef.orderByChild("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String songName = postSnapshot.child("title").getValue(String.class);
                    songList.add(songName);
                }
                ArrayAdapter<String> arrayAdapterAlbum = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, songList);
                autoCompleteAddSongs.setAdapter(arrayAdapterAlbum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(view)
                .setTitle("Add Songs")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String songName = autoCompleteAddSongs.getText().toString();
                        listener.AddSong(songName);
                    }
                });
        return builder.create();
    }

    public interface DialogAddSongsListener{
        void AddSong(String songName);
    }

    public void setListener(DialogAddSongsListener listener){
        this.listener = listener;
    }

}
