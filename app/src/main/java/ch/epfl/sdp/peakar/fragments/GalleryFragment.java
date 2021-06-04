package ch.epfl.sdp.peakar.fragments;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.gallery.GalleryAdapter;
import ch.epfl.sdp.peakar.gallery.ImageActivity;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.GALLERY_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupDots;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupGreyTopBar;

/**
 * A simple {@link Fragment} subclass that represents the gallery
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    private boolean returnToFragment;
    private ConstraintLayout container;
    private static final int COLUMNS = 3;

    /**
     * Constructor for the CameraPreview
     * Is required to be empty for the fragments
     */
    public GalleryFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment GalleryFragment.
     */
    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnToFragment = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = (ConstraintLayout) view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(lastFragmentIndex.contains(GALLERY_FRAGMENT_INDEX)) {
            lastFragmentIndex.remove((Object)GALLERY_FRAGMENT_INDEX);
        }
        lastFragmentIndex.push(GALLERY_FRAGMENT_INDEX);

        updateSelectedIcon(this);
        if(returnToFragment){
            reloadFragment();
        }
        else{
            returnToFragment = true;
            initFragment();
        }
    }

    /**
     * Reloads the fragment: Sets the status bar, top bar and reloads the gallery
     */
    private void reloadFragment() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setupGallery();

        StatusBarLightGrey(this);
        setupGreyTopBar(this);
    }

    /**
     * Gets the recycler view and fills it up with all images.
     */
    private void setupGallery(){
        List<String> imagePaths = StorageHandler.getImagePaths(getContext());
        if (imagePaths.isEmpty()) {
            container.findViewById(R.id.gallery_empty).setVisibility(View.VISIBLE);
        }
        else {
            container.findViewById(R.id.gallery_empty).setVisibility(View.GONE);
        }

        GalleryAdapter galleryAdapter = new GalleryAdapter(getContext(), imagePaths, (imagePath) -> {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("images", new ArrayList<>(imagePaths));
//            bundle.putInt("position", position);
//
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            SlideshowFragment newFragment = SlideshowFragment.newInstance();
//            newFragment.setArguments(bundle);
//            newFragment.show(ft, "slideshow");


            Intent intent = new Intent(getContext(), ImageActivity.class);
            intent.putExtra(ImageActivity.IMAGE_PATH_INTENT, imagePath);
            startActivity(intent);
        });

        RecyclerView recyclerView = container.findViewById(R.id.gallery_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMNS));
        recyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Initialises the fragment
     */
    private void initFragment() {
        reloadFragment();

        setupDots(this, v -> {
            // nothing, could expand to add functionalities
        });
    }
}