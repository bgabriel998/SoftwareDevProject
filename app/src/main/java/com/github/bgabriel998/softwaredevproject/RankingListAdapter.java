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

        setupRankingsItem(convertView, getItem(position), position);

        return convertView;
    }

    /**
     * Sets the text of a ranking list item.
     * @param view the view to text views.
     * @param item the item to take the data from
     * @param position the position the item has in the list.
     */
    private void setupRankingsItem(View view, RankingItem item, int position) {
        if (item != null) {
            TextView positionText = (TextView) view.findViewById(R.id.ranking_item_position);
            TextView usernameText = (TextView) view.findViewById(R.id.ranking_item_username);
            TextView pointsText = (TextView) view.findViewById(R.id.ranking_item_points);

            setItemColor(view, new TextView[]{positionText, usernameText, pointsText}, item);
            setItemText(positionText, usernameText, pointsText, item, position+1);
        }
    }

    /**
     * Sets the colors rankings item
     * based on if the item represents the user.
     * @param view item view
     * @param textViews all text views
     * @param item the item
     */
    private void setItemColor(View view, TextView[] textViews, RankingItem item){
        // TODO Check for actual username
        if (item.username.equals("Username2")) {
            view.findViewById(R.id.ranking_item_container).setBackgroundResource(R.color.DarkGreen);
            for (TextView v: textViews) {
                v.setTextAppearance(R.style.LightGreyText);
            }
        }
        else {
            view.findViewById(R.id.ranking_item_container).setBackgroundResource(R.color.LightGrey);
            for (TextView v: textViews) {
                v.setTextAppearance(R.style.DarkGreenText);
            }
        }
    }

    /**
     * Sets text on list item
     * @param positionText text view for position
     * @param usernameText text view for username
     * @param pointsText text view for points
     * @param item the item
     * @param position the position in ranking.
     */
    private void setItemText(TextView positionText,
                             TextView usernameText,
                             TextView pointsText,
                             RankingItem item, int position) {
        if (positionText != null) {
            positionText.setText(String.format(Locale.getDefault(), "%d.", position));
        }

        if (usernameText != null) {
            usernameText.setText(item.username);
        }

        if (pointsText != null) {
            pointsText.setText(String.format(Locale.getDefault(), "%d", item.points));
        }
    }
}
