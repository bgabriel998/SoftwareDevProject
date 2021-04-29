package ch.epfl.sdp.peakar.utils;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.peakar.R;

/**
 * Handler for toolbar to setup title and function for back button
 */
public class ToolbarHandler {

    /**
     * Sets toolbar title and so that back button calls activity finish.
     * @param activity the activity that currently shows toolbar.
     * @param title the title the toolbar should get.
     */
    public static void SetupToolbar(AppCompatActivity activity, String title){
        ImageButton backToolbarButton = activity.findViewById(R.id.toolbarBackButton);
        backToolbarButton.setOnClickListener(v -> activity.finish());
        TextView toolbarTitle = activity.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(title);
    }
}
