package ch.epfl.sdp.peakar.fragments;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.gallery.GalleryAdapter;
import ch.epfl.sdp.peakar.gallery.ImageActivity;
import ch.epfl.sdp.peakar.utils.OnSwipeTouchListener;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.GALLERY_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupDots;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupGreyTopBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    private boolean returnToFragment;
    private ConstraintLayout container;
    private static final int COLUMNS = 3;

    public GalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
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
            initFragment();
            returnToFragment = true;
        }
    }

    private void reloadFragment() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarLightGrey(this);
        setupGreyTopBar(this);

        setupGallery();
    }

    /**
     * Gets the recycler view and fills it up with all images.
     */
    private void setupGallery(){
        List<String> imagePaths = StorageHandler.getImagePaths(getContext());
        if (!imagePaths.isEmpty()) {
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

    private void initFragment() {
        reloadFragment();

        setupDots(this, v -> {
            // TODO Sort images
        });

        container.findViewById(R.id.gallery_recyclerview).setOnTouchListener(new OnSwipeTouchListener(getContext()));
    }
}