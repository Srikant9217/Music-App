package com.example.music.ui.library.fragmentTabs.Artist;

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
import com.example.music.BottomSheets.BottomSheetArtistSongs;
import com.example.music.MainActivity;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.PlaylistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ArtistFragment extends Fragment implements SongAdapter.OnItemClickListener,
        BottomSheetArtistSongs.BottomSheetListener {
    public static final String CURRENT_ARTIST = "CURRENT_ARTIST";

    private TextView textViewArtistTitle;
    private ImageView buttonFavouriteArtist;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private StorageUtil storage;
    private FavouriteArtists favouriteArtists;

    private ArrayList<SongModel> songList;
    private ArtistModel currentArtist;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private DatabaseReference songDatabaseRef;
    private ValueEventListener dbListener1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist, container, false);

        textViewArtistTitle = v.findViewById(R.id.text_view_artist_title);
        buttonFavouriteArtist = v.findViewById(R.id.favourite_artist);

        currentArtist = (ArtistModel) getArguments().getSerializable(CURRENT_ARTIST);
        storage = new StorageUtil(getActivity());
        favouriteArtists = FavouriteArtists.getInstance(getActivity());
        if (storage.loadFavouriteArtists() != null) {
            if (favouriteArtists.isFavourite(currentArtist)) {
                buttonFavouriteArtist.setImageResource(R.drawable.ic_baseline_favorite_24);
            } else {
                buttonFavouriteArtist.setImageResource(R.drawable.ic_baseline_not_favorite);
            }
        }

        textViewArtistTitle.setText(currentArtist.getName());

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        songList = new ArrayList<>();

        adapter = new SongAdapter(getActivity(), songList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Artists");
        songDatabaseRef = FirebaseDatabase.getInstance().getReference("Song");

        dbListener1 = songDatabaseRef.orderByChild("artist").equalTo(currentArtist.getName())
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

        buttonFavouriteArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favouriteArtist();
            }
        });

        return v;
    }

    private void favouriteArtist() {
        boolean isFavourite = favouriteArtists.favourite(currentArtist, getActivity());
        if (isFavourite) {
            buttonFavouriteArtist.setImageResource(R.drawable.ic_baseline_favorite_24);
        } else {
            buttonFavouriteArtist.setImageResource(R.drawable.ic_baseline_not_favorite);
        }
    }

    @Override
    public void onItemClick(int position) {
        ((MainActivity) getActivity()).playSong(songList, position);
    }

    @Override
    public void onItemLongClick(int position) {
        BottomSheetArtistSongs bottomSheet = new BottomSheetArtistSongs();
        bottomSheet.setBottomSheetListener(this);
        bottomSheet.show(getChildFragmentManager(), "BottomSheetArtistSongs");
    }

    @Override
    public void onOptionClicked(int position) {
        switch (position) {
            case 0:
                Toast.makeText(getActivity(), "0", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(dbListener1);
    }
}
