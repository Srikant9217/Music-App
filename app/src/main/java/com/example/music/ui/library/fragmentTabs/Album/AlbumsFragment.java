package com.example.music.ui.library.fragmentTabs.Album;

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

import com.example.music.Adapter.AlbumAdapter;
import com.example.music.Adapter.ArtistAdapter;
import com.example.music.BottomSheets.BottomSheetAlbums;
import com.example.music.BottomSheets.BottomSheetArtists;
import com.example.music.Model.AlbumModel;
import com.example.music.Model.ArtistModel;
import com.example.music.Model.UserModel;
import com.example.music.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnItemClickListener,
        BottomSheetAlbums.BottomSheetListener {
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private ProgressBar progressBarRecycler;

    private ArrayList<AlbumModel> albumList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private ValueEventListener dbListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_albums, container, false);
        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        albumList = new ArrayList<>();

        adapter = new AlbumAdapter(getActivity(), albumList);
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
                            albumList.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                UserModel user = postSnapshot.getValue(UserModel.class);
                                ArrayList<AlbumModel> albums = user.getFavouriteAlbums();

                                if (albums != null && !albums.isEmpty()) {
                                    for (AlbumModel album : albums) {
                                        album.setKey(postSnapshot.getKey());
                                        albumList.add(album);
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "No Favourite Artists", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Please Login", Toast.LENGTH_SHORT).show();
            progressBarRecycler.setVisibility(View.INVISIBLE);
        }
        return v;
    }

    @Override
    public void onAlbumItemClick(View view, AlbumModel album) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AlbumFragment.CURRENT_ALBUM, album);
        view.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.albumFragment, bundle));
        view.callOnClick();
    }

    @Override
    public void onAlbumItemLongClick(int position) {
        BottomSheetAlbums bottomSheetAlbums = new BottomSheetAlbums();
        bottomSheetAlbums.setBottomSheetListener(this);
        bottomSheetAlbums.show(getChildFragmentManager(), "BottomSheetAlbums");
    }

    @Override
    public void onOptionClicked(int position) {
        switch (position) {
            case 0:
                Toast.makeText(getActivity(), "0", Toast.LENGTH_SHORT).show();
            case 1:
                Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
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