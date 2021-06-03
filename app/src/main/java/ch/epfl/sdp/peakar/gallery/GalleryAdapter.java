package ch.epfl.sdp.peakar.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.ImageHandler;
import ch.epfl.sdp.peakar.utils.OnSwipeTouchListener;

/**
 * Adapter for gallery to show all images
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final Context mContext;
    private final List<String> imagePaths;
    protected PhotoListener photoListener;

    /**
     * Constructor
     * @param mContext context to show images
     * @param imagePaths list of all images that gallery should show
     * @param photoListener interface with function that should get called when pressing imagethumbnail
     */
    public GalleryAdapter(Context mContext, List<String> imagePaths, PhotoListener photoListener) {
        this.mContext = mContext;
        this.imagePaths = imagePaths;
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
     * @param holder the layout to add the image to
     * @param position used to get correct image
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //String imagePath = imagePaths.get(position);
        //Bitmap imageBitmap = ImageHandler.getBitmapUpwards(imagePath);


        String path = imagePaths.get(position);
        Glide.with(mContext).load(new File(path))
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);

        holder.itemView.setOnTouchListener(new OnSwipeTouchListener(mContext){
            @Override
            public void onClick() {
                super.onClick();
                photoListener.onPhotoClick(path);
            }
        });
    }

    /**
     * Overridden method to get the amount of items.
     * @return the amount of items.
     */
    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    /**
     * View holder for the gallery adapter.
     * Contains the image view to be able to set the correct image.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;

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
     */
    public interface PhotoListener {
        /**
         * Function that is called
         * @param imagePath to get the image.
         */
        void onPhotoClick(String imagePath);
    }
}
