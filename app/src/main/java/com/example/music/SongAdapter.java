package com.example.music;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private Context context;
    private List<SongModel> songs;
    private OnItemClickListener listener;

    public SongAdapter(Context context, List<SongModel> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
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
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
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
            imageViewFavourite.setOnClickListener(this);
            imageViewMenu.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.image_view_favourite:
                    imageViewFavourite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    break;
                default:
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem delete = contextMenu.add(Menu.NONE, 1, 1, "Delete");
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (listener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    switch (menuItem.getItemId()){
                        case 1:
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
