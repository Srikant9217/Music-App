package com.example.music.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Model.SongModel;
import com.example.music.R;
import com.example.music.Storage.StorageUtil;
import com.example.music.ui.library.fragmentTabs.Playlist.FavouriteSongs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SongAdapterHorizontal extends RecyclerView.Adapter<SongAdapterHorizontal.SongViewHolder> {
    private Context context;
    private List<SongModel> songs;
    private OnItemClickListener listener;
    private StorageUtil storage;
    private ArrayList<SongModel> favSongs;
    private FavouriteSongs favouriteSongs;

    public SongAdapterHorizontal(Context context, List<SongModel> songs) {
        this.context = context;
        this.songs = songs;
        storage = new StorageUtil(context);
        favouriteSongs = FavouriteSongs.getInstance(context);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_song_horizontal, parent, false);
        favSongs = storage.loadFavouriteSongs();
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        SongModel currentSong = songs.get(position);
        Picasso.with(context)
                .load(currentSong.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.imageViewSongImage);
        holder.textViewTitle.setText(currentSong.getTitle());
        holder.textViewArtist.setText(currentSong.getArtist());

        if (favSongs != null) {
            if (favouriteSongs.isFavourite(currentSong)) {
                holder.imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else {
                holder.imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
            }
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView textViewTitle;
        private TextView textViewArtist;
        private ImageView imageViewSongImage;
        private ImageView imageViewFavourite;
        private ImageView imageViewMenu;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewArtist = itemView.findViewById(R.id.text_view_artist);
            imageViewSongImage = itemView.findViewById(R.id.image_view_song);
            imageViewFavourite = itemView.findViewById(R.id.image_view_favourite);
            imageViewMenu = itemView.findViewById(R.id.image_view_menu);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            imageViewFavourite.setOnClickListener(this);
            imageViewMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            SongModel song = songs.get(position);
            switch (view.getId()) {
                case R.id.image_view_favourite:
                    boolean isFavourite = favouriteSongs.favouriteOrUnFavourite(songs.get(position), context);
                    if (isFavourite) {
                        imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    } else {
                        imageViewFavourite.setImageResource(R.drawable.ic_baseline_not_favorite);
                    }
                    break;
                case R.id.image_view_menu:
                    if (listener != null) {
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemLongClick(position, song);
                        }
                    }
                    break;
                default:
                    if (listener != null) {
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                SongModel song = songs.get(position);
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(position, song);
                }
            }
            return true;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position, SongModel song);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
