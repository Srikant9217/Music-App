package com.example.music.Adapter;

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

import com.example.music.Model.ArtistModel;
import com.example.music.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {
    private Context context;
    private List<ArtistModel> artists;
    private OnItemClickListener listener;

    public ArtistAdapter(Context context, List<ArtistModel> artists) {
        this.context = context;
        this.artists = artists;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        ArtistModel currentArtist = artists.get(position);
        Picasso.with(context)
                .load(currentArtist.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.imageViewArtistProfile);
        holder.textViewArtistName.setText(currentArtist.getName());
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private ImageView imageViewArtistProfile;
        private TextView textViewArtistName;
        private ImageView imageViewArtistMenu;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewArtistProfile = itemView.findViewById(R.id.image_view_artist_profile);
            textViewArtistName = itemView.findViewById(R.id.text_view_artist_name);
            imageViewArtistMenu = itemView.findViewById(R.id.image_view_artist_menu);

            itemView.setOnClickListener(this);
            imageViewArtistMenu.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
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
                            listener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
