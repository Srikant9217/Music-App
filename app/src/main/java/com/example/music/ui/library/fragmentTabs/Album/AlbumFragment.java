package com.example.music.ui.library.fragmentTabs.Album;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.SongAdapter;
import com.example.music.BottomSheets.BottomSheetAlbumSongs;
import com.example.music.MainActivity;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.StorageUtil;
import com.example.music.ui.library.fragmentTabs.Artist.FavouriteArtists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements SongAdapter.OnItemClickListener,
        BottomSheetAlbumSongs.BottomSheetListener {
    public static final String CURRENT_ALBUM = "CURRENT_ALBUM";

    private TextView textViewAlbumTitle;
    private ImageView buttonFavouriteAlbum;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private StorageUtil storage;
    private FavouriteAlbums favouriteAlbums;

    private ArrayList<SongModel> songList;
    private AlbumModel currentAlbum;

    private DatabaseReference songDatabaseRef;
    private ValueEventListener dbListener1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);

        textViewAlbumTitle = v.findViewById(R.id.text_view_album_title);
        buttonFavouriteAlbum = v.findViewById(R.id.favourite_album);

        currentAlbum = (AlbumModel) getArguments().getSerializable(CURRENT_ALBUM);
        storage = new StorageUtil(getActivity());
        favouriteAlbums = FavouriteAlbums.getInstance(getActivity());
        if (storage.loadFavouriteAlbums() != null) {
            if (favouriteAlbums.isFavourite(currentAlbum)) {
                buttonFavouriteAlbum.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else {
                buttonFavouriteAlbum.setImageResource(R.drawable.ic_baseline_not_favorite);
            }
        }

        textViewAlbumTitle.setText(currentAlbum.getName());

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        songList = new ArrayList<>();

        adapter = new SongAdapter(getActivity(), songList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        songDatabaseRef = FirebaseDatabase.getInstance().getReference("Song");

        dbListener1 = songDatabaseRef.orderByChild("album").equalTo(currentAlbum.getName())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        songList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            SongModel song = postSnapshot.getValue(SongModel.class);
                            song.setKey(postSnapshot.getKey());
                            songList.add(song);
                        }
                        adapter.notifyDataSetChanged();
                        progressBarRecycler.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBarRecycler.setVisibility(View.INVISIBLE);
                    }
                });

        buttonFavouriteAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favouriteAlbum();
            }
        });

        return v;
    }

    private void favouriteAlbum(){
        boolean isFavourite = favouriteAlbums.favourite(currentAlbum, getActivity());
        if (isFavourite) {
            buttonFavouriteAlbum.setImageResource(R.drawable.ic_baseline_favorite_24);
        } else {
            buttonFavouriteAlbum.setImageResource(R.drawable.ic_baseline_not_favorite);
        }
    }

    @Override
    public void onItemClick(int position) {
        ((MainActivity)getActivity()).playSong(songList, position);
    }

    @Override
    public void onItemLongClick(int position) {
        BottomSheetAlbumSongs bottomSheet = new BottomSheetAlbumSongs();
        bottomSheet.setBottomSheetListener(this);
        bottomSheet.show(getChildFragmentManager(), "BottomSheetAlbumSongs");
    }

    @Override
    public void onOptionClicked(int position) {
        switch (position){
            case 0:
                Toast.makeText(getActivity(), "0", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        songDatabaseRef.removeEventListener(dbListener1);
    }
}
