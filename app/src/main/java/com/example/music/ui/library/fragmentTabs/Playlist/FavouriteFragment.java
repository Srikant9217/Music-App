package com.example.music.ui.library.fragmentTabs.Playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapter.SongAdapter;
import com.example.music.BottomSheets.BottomSheetPlaylistSongs;
import com.example.music.MainActivity;
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

public class FavouriteFragment extends Fragment implements SongAdapter.OnItemClickListener,
        BottomSheetPlaylistSongs.BottomSheetListener {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBarRecycler;

    private StorageUtil storage;
    private ArrayList<SongModel> favouriteSongList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist_favourite, container, false);

        progressBarRecycler = v.findViewById(R.id.recycler_view_progress_bar);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        storage = new StorageUtil(getActivity());
        favouriteSongList = storage.loadFavouriteSongs();

        if (favouriteSongList == null){
            favouriteSongList = new ArrayList<>();
        }

        if (favouriteSongList.isEmpty()){
            progressBarRecycler.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Please Add Songs", Toast.LENGTH_SHORT).show();
        }

        adapter = new SongAdapter(getActivity(), favouriteSongList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onItemClick(int position) {
        ((MainActivity) getActivity()).playSong(favouriteSongList, position);
    }

    @Override
    public void onItemLongClick(int position) {
        BottomSheetPlaylistSongs bottomSheet = new BottomSheetPlaylistSongs();
        bottomSheet.setBottomSheetListener(this);
        bottomSheet.show(getChildFragmentManager(), "BottomSheetPlaylistSongs");
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
}
