package com.example.music.BottomSheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music.Model.AlbumModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Album.FavouriteAlbums;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BottomSheetAlbums extends BottomSheetDialogFragment {
    private BottomSheetListener listener;
    private Integer albumPosition;
    private AlbumModel currentAlbum;
    private String favourite;
    private String favouriteAll;
    private ValueEventListener dbListener1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.bottom_sheet, container, false);
        final ListView listView = v.findViewById(R.id.list_view_options);

        albumPosition = getArguments().getInt("position");
        currentAlbum = (AlbumModel) getArguments().getSerializable("album");

        if (FavouriteAlbums.getInstance(getActivity()).isFavourite(currentAlbum)){
            favourite = "UnFavourite";
        }else {
            favourite = "Favourite";
        }

        final DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference("Song");
        dbListener1 = albumReference.orderByChild("album").equalTo(currentAlbum.getName())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<SongModel> songList = new ArrayList<>();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            SongModel song = postSnapshot.getValue(SongModel.class);
                            songList.add(song);
                        }
                        FavouriteSongs favouriteSongs = FavouriteSongs.getInstance(getActivity());
                        if (favouriteSongs.isFavouriteAll(songList)){
                            favouriteAll = "Unlike All Songs";
                        }else {
                            favouriteAll = "Like All Songs";
                        }

                        ArrayList<String> options = new ArrayList<>();
                        options.add(favourite);
                        options.add(favouriteAll);
                        options.add("View Album");

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1,
                                options);

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                listener.onOptionClicked(i, albumPosition, currentAlbum);
                                dismiss();
                            }
                        });

                        albumReference.removeEventListener(dbListener1);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return v;
    }


    public interface BottomSheetListener {
        void onOptionClicked(int option, int position, AlbumModel album);
    }

    public void setBottomSheetListener(BottomSheetListener listener) {
        this.listener = listener;
    }
}