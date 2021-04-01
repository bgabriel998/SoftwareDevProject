package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * List adapter for collected items to set correct text for each collected item.
 */
public class CollectionListAdapter extends ArrayAdapter<CollectedItem> {

    private final int resourceLayout;
    private final Context mContext;

    /**
     * Constructor
     * @param context the context
     * @param resource the resource layout to create list items of
     * @param items list items.
     */
    public CollectionListAdapter(Context context, int resource, List<CollectedItem> items) {
        super(context, resource, items);
        resourceLayout = resource;
        mContext = context;
    }

    /**
     * Overridden method to create correct collected items.
     * @param position position of item
     * @param convertView item view
     * @param parent parent view
     * @return a view with an added item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ListAdapterInflater.createLayout(resourceLayout, mContext,parent);
        }

        setItemText(convertView, getItem(position));

        return convertView;
    }

    /**
     * Sets the correct text on a view based on an item.
     * @param view the view to place text on.
     * @param item the item to base text off.
     */
    private void setItemText(View view, CollectedItem item) {
        if (item != null) {
            TextView nameText = view.findViewById(R.id.collected_name);
            TextView pointsText = view.findViewById(R.id.collected_points);

            nameText.setText(item.getName());
            pointsText.setText(String.format(Locale.getDefault(), "%d", item.getPoints()));
        }
    }
}
