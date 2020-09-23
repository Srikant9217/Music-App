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

import com.example.music.Model.ArtistModel;
import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.ui.library.fragmentTabs.Artist.FavouriteArtists;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class BottomSheetArtists extends BottomSheetDialogFragment {
    private BottomSheetListener listener;
    private Integer artistPosition;
    private ArtistModel currentArtist;
    private String favourite;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet, container, false);

        artistPosition = getArguments().getInt("position");
        currentArtist = (ArtistModel) getArguments().getSerializable("artist");

        if (FavouriteArtists.getInstance(getActivity()).isFavourite(currentArtist)){
            favourite = "UnFollow";
        }else {
            favourite = "Follow";
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(favourite);
        options.add("View Artist");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                options);

        ListView listView = v.findViewById(R.id.list_view_options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.onOptionClicked(i, artistPosition, currentArtist);
                dismiss();
            }
        });
        return v;
    }


    public interface BottomSheetListener {
        void onOptionClicked(int option, int position, ArtistModel artist);
    }

    public void setBottomSheetListener(BottomSheetListener listener) {
        this.listener = listener;
    }
}