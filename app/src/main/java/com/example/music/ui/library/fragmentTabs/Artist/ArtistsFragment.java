package com.example.music.ui.library.fragmentTabs.Artist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.Adapter.ArtistAdapter;
import com.example.music.Adapter.SongAdapter;
import com.example.music.BottomSheets.BottomSheetArtists;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ArtistsFragment extends Fragment implements ArtistAdapter.OnItemClickListener,
        BottomSheetArtists.BottomSheetListener {
    private RecyclerView recyclerView;
    private ArtistAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<ArtistModel> artistList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private ValueEventListener dbListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artists, container, false);
        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        artistList = new ArrayList<>();

        adapter = new ArtistAdapter(getActivity(), artistList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            dbListener = databaseReference.orderByChild("userId").equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            artistList.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                UserModel user = postSnapshot.getValue(UserModel.class);
                                ArrayList<ArtistModel> artists = user.getFavouriteArtists();
                                if (artists != null && !artists.isEmpty()) {
                                    for (ArtistModel artist : artists) {
                                        artist.setKey(postSnapshot.getKey());
                                        artistList.add(artist);
                                    }
                                }
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
        } else {
            progressBarRecycler.setVisibility(View.INVISIBLE);
        }
        return v;
    }

    @Override
    public void onArtistItemClick(View view, ArtistModel artist) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ArtistFragment.CURRENT_ARTIST, artist);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.artistFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onArtistItemLongClick(int position, ArtistModel artist) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putSerializable("artist", artist);

        BottomSheetArtists bottomSheetArtists = new BottomSheetArtists();
        bottomSheetArtists.setBottomSheetListener(this);
        bottomSheetArtists.setArguments(bundle);
        bottomSheetArtists.show(getChildFragmentManager(), "BottomSheetArtists");
    }

    @Override
    public void onOptionClicked(int option, int position, ArtistModel artist) {
        switch (option) {
            case 0:
                FavouriteArtists favouriteArtists = FavouriteArtists.getInstance(getActivity());
                favouriteArtists.favourite(artist, getActivity());
                break;
            case 1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(ArtistFragment.CURRENT_ARTIST, artist);
                View view = getActivity().findViewById(R.id.recycler_view);
                view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.artistFragment, bundle));
                view.callOnClick();
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dbListener != null) {
            databaseReference.removeEventListener(dbListener);
        }
    }
}