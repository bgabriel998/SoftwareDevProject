package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for gallery to show all images
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final Context mContext;    
    // TODO change to list of paths to images in folder
    private List<Integer> images;
    protected PhotoListener photoListener;

    /**
     * Constructor
     * @param mContext context to show images
     * @param images list of all images that gallery should show
     * @param photoListener interface with function that should get called when pressing imagethumbnail
     */
    public GalleryAdapter(Context mContext, List<Integer> images, PhotoListener photoListener) {
        this.mContext = mContext;
        this.images = images;
        this.photoListener = photoListener;
    }

    /**
     * Overridden function to inflate a new image thumbnail
     * @param parent parent of new layout
     * @param viewType unused
     * @return a new inflated layout
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.gallery_item, parent, false)
        );
    }

    /**
     * Overridden function to bind an image to image thumbnail.
     * TODO take image from folder via path instead of image resource.
     * @param holder the layout to add the image to
     * @param position used to get correct image
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int image = images.get(position);

        holder.image.setImageResource(image);
        //Glide.with(context).load(image).into(holder.image)

        holder.itemView.setOnClickListener(v -> photoListener.onPhotoClick(image));
    }

    /**
     * Overridden method to get the amount of items.
     * @return the amount of items.
     */
    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * View holder for the gallery adapter.
     * Contains the image view to be able to set the correct image.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        /**
         * Constructor to get the image view.
         * @param itemView used in parent method.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.gallery_image);
        }
    }

    /**
     * Interface to be able to set function to be called when thumbnail is pressed.
     * TODO Send path instead of int ID.
     */
    public interface PhotoListener {
        /**
         * Function that is called
         * @param imageID to get the resource image.
         */
        void onPhotoClick(int imageID);
    }
}
