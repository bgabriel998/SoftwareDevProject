package ch.epfl.sdp.peakar.rankings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.http.auth.AUTH;

import ch.epfl.sdp.peakar.user.auth.Authentication;
import ch.epfl.sdp.peakar.utils.ListAdapterInflater;
import ch.epfl.sdp.peakar.R;

import java.util.List;
import java.util.Locale;

/**
 * List adapter for rankings items to set correct text for each ranking item.
 */
public class RankingListAdapter extends ArrayAdapter<RankingItem> {

    private final int resourceLayout;
    private final Context mContext;

    /**
     * Constructor
     * @param context the context
     * @param resource the resource layout to create list items of
     * @param items list items.
     */
    public RankingListAdapter(Context context, int resource, List<RankingItem> items) {
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
            convertView = ListAdapterInflater.createLayout(resourceLayout, mContext,parent);
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
            TextView[] textViews = new TextView[]{view.findViewById(R.id.ranking_item_position),
                                                  view.findViewById(R.id.ranking_item_username),
                                                  view.findViewById(R.id.ranking_item_points)};

            setItemColor(view, textViews, item);
            setItemText(textViews, item, position+1);
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
        int backgroundColor = R.color.LightGrey;
        int textStyle = R.style.StandardText_DarkGreen;

        if (item.getUid().equals(Authentication.getInstance().getID())) {
            backgroundColor = R.color.DarkGreen;
            textStyle = R.style.StandardText_LightGrey;
        }

        view.findViewById(R.id.ranking_item_container).setBackgroundResource(backgroundColor);
        for (TextView v: textViews) {
            v.setTextAppearance(textStyle);
        }
    }

    /**
     * Sets text on list item
     * @param textViews the text views to set the text on.
     * @param item the actual item
     * @param position the position in ranking.
     */
    private void setItemText(TextView[] textViews, RankingItem item, int position) {
        textViews[0].setText(String.format(Locale.getDefault(), "%d.", position));
        textViews[1].setText(item.getUsername());
        textViews[2].setText(String.format(Locale.getDefault(), "%d", item.getPoints()));
    }
}
