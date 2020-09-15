package com.example.music.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Model.PlaylistModel;
import com.example.music.R;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private Context context;
    private List<PlaylistModel> playlists;
    private OnItemClickListener listener;

    public PlaylistAdapter(Context context, List<PlaylistModel> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistModel currentPlaylist = playlists.get(position);
        holder.textViewPlaylistName.setText(currentPlaylist.getName());
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageViewPlaylistProfile;
        private TextView textViewPlaylistName;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPlaylistProfile = itemView.findViewById(R.id.image_view_playlist_profile);
            textViewPlaylistName = itemView.findViewById(R.id.text_view_playlist_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position, view);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
