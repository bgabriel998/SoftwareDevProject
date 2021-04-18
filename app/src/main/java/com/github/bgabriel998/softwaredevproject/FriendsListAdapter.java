package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class FriendsListAdapter extends ArrayAdapter<FriendItem> {

    private final int resourceLayout;
    private final Context mContext;

    /**
     * Constructor
     * @param context the context
     * @param resource the resource layout to create list items of
     * @param items list items.
     */
    public FriendsListAdapter(Context context, int resource, List<FriendItem> items) {
        super(context, resource, items);
        resourceLayout = resource;
        mContext = context;
    }

    /**
     * Overridden method to create correct friend items.
     * @param position position of item
     * @param convertView item view
     * @param parent parent view
     * @return a view with an added item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ListAdapterInflater.createLayout(resourceLayout, mContext, parent);
        }

        setItemText(convertView, getItem(position));

        return convertView;
    }

    /**
     * Sets the correct text on a view based on an item.
     * @param view the view to place text on.
     * @param item the item to base text off.
     */
    private void setItemText(View view, FriendItem item) {
        if (item != null) {
            TextView nameText = view.findViewById(R.id.friend_username);

            nameText.setText(item.getUsername());
        }
    }
}
