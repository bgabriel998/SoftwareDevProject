package ch.epfl.sdp.peakar.fragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.map.OSMMap;
import ch.epfl.sdp.peakar.user.services.AuthService;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.MAP_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarTransparentBlack;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupTransparentTopBar;

/**
 * A simple {@link Fragment} subclass that represents the map.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private OSMMap osmMap;
    private ImageButton zoomOnUserLocationButton;
    private ImageButton changeMapTileSourceButton;
    private ConstraintLayout container;
    private boolean returnToFragment;

    /**
     * Constructor for the CameraPreview
     * Is required to be empty for the fragments
     */
    public MapFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = (ConstraintLayout) view;
    }

    /**
     * Initialises the map fragment when the fragment is created the first time
     */
    private void initMapFragment(){
        StatusBarTransparentBlack(this);
        setupTransparentTopBar(this, R.color.Black);

        //Instantiate Map
        osmMap = new OSMMap(requireContext(), container.findViewById(R.id.map));

        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(AuthService.getInstance().getAuthAccount() != null);

        osmMap.displayUserLocation();

        zoomOnUserLocationButton = container.findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(v -> osmMap.zoomOnUserLocation());

        changeMapTileSourceButton = container.findViewById(R.id.changeMapTile);
        changeMapTileSourceButton.setOnClickListener(v -> osmMap.changeMapTileSource(zoomOnUserLocationButton,changeMapTileSourceButton ));
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        updateSelectedIcon(this);
        if(lastFragmentIndex.contains(MAP_FRAGMENT_INDEX)) {
            lastFragmentIndex.remove((Object)MAP_FRAGMENT_INDEX);
        }
        lastFragmentIndex.push(MAP_FRAGMENT_INDEX);
        if(returnToFragment){
            reloadFragment();
        }
        else {
            returnToFragment = true;
            initMapFragment();
        }
    }

    /**
     * Reloads the fragment
     */
    private void reloadFragment() {
        StatusBarTransparentBlack(this);
        setupTransparentTopBar(this, R.color.Black);

        //Display markers on the map
        osmMap.updateMarkers(AuthService.getInstance().getAuthAccount() != null);
    }

    /**
     * Gets the currently used osmMap
     * @return Currently used OSMMap
     */
    public OSMMap getOsmMap() {
        return osmMap;
    }
}