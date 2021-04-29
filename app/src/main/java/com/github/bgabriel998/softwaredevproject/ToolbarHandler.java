package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    /**
     * Special toolbar listener. Used only to return to Main menu from settings
     * activity
     * @param activity the activity that currently shows toolbar.
     * @param context context of the calling activity
     * @param title the title the toolbar should get.
     */
    public static void SetupToolbarCustom(AppCompatActivity activity, Context context, String title){
        ImageButton backToolbarButton = activity.findViewById(R.id.toolbarBackButton);
        backToolbarButton.setOnClickListener(v ->  {
            Intent setIntent = new Intent(context,MainMenuActivity.class);
            context.startActivity(setIntent);
        });
        TextView toolbarTitle = activity.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(title);
    }
}
