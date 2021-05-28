package ch.epfl.sdp.peakar.fragments;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private OSMMap osmMap;
    private ImageButton zoomOnUserLocationButton;
    private ImageButton changeMapTileSourceButton;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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

        ConstraintLayout container = (ConstraintLayout) view;

        //Instantiate Map
        osmMap = new OSMMap(getContext(), container.findViewById(R.id.map));


        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(AuthService.getInstance().getAuthAccount() != null);

        osmMap.displayUserLocation();

        zoomOnUserLocationButton = (ImageButton) container.findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(v -> osmMap.zoomOnUserLocation());

        changeMapTileSourceButton = (ImageButton) container.findViewById(R.id.changeMapTile);
        changeMapTileSourceButton.setOnClickListener(v -> osmMap.changeMapTileSource(zoomOnUserLocationButton,changeMapTileSourceButton ));
    }

    /**
     * Gets the currently used osmMap
     * @return Currently used OSMMap
     */
    public OSMMap getOsmMap() {
        return osmMap;
    }
}