package ch.epfl.sdp.peakar.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.epfl.sdp.peakar.R;

import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupGreyTopBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SocialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialFragment extends Fragment {

    private boolean returnToFragment;

    public SocialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SocialFragment.
     */
    public static SocialFragment newInstance() {
        return new SocialFragment();
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
        return inflater.inflate(R.layout.fragment_social, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(returnToFragment){
            initFragment();
        }
        else
            returnToFragment = true;
    }

    private void initFragment() {
        StatusBarLightGrey(this);
        setupGreyTopBar(this);
        //MenuBarHandler.setup(this);
    }
}