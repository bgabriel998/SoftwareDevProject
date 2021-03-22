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
 * List adapter for rankings items to set correct text for each ranking item.
 */
public class RankingListAdapter extends ArrayAdapter<RankingItem> {

    private final int resourceLayout;
    private final Context mContext;

    public RankingListAdapter(Context context, int resource, List<RankingItem> items)
    {
        super(context, resource, items);
        resourceLayout = resource;
        mContext = context;
    }

    /**
     * Overridden method to create correct Ranking items.
     * @param position position of item
     * @param convertView item view
     * @param parent parent view
     * @return a view with an added item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            convertView = vi.inflate(resourceLayout, parent, false);
        }

        setRankingsItemTexts(convertView, getItem(position), position +1);

        return convertView;
    }

    /**
     * Sets the text of a ranking list item.
     * @param view the view to text views.
     * @param item the item to take the data from
     * @param place the place the item has in the list.
     */
    private void setRankingsItemTexts(View view, RankingItem item, int place) {
        if (item != null) {
            TextView positionText = (TextView) view.findViewById(R.id.ranking_item_position);
            TextView usernameText = (TextView) view.findViewById(R.id.ranking_item_username);
            TextView pointsText = (TextView) view.findViewById(R.id.ranking_item_points);

            if (positionText != null) {
                positionText.setText(String.format(Locale.getDefault(), "%d.", place));
            }

            if (usernameText != null) {
                usernameText.setText(item.username);
            }

            if (pointsText != null) {
                pointsText.setText(String.format(Locale.getDefault(), "%d", item.points));
            }
        }
    }
}
