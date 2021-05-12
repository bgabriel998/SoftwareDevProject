package ch.epfl.sdp.peakar.collection;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.ListAdapterInflater;
import ch.epfl.sdp.peakar.utils.UIUtils;

/**
 * List adapter for collected items to set correct text for each collected item.
 * TODO Rename to remove New part
 */
public class NewCollectionListAdapter extends ArrayAdapter<NewCollectedItem> {

    private final int resourceLayout;
    private final Context mContext;

    /**
     * Constructor
     * @param context the context
     * @param resource the resource layout to create list items of
     * @param items list items.
     */
    public NewCollectionListAdapter(Context context, int resource, List<NewCollectedItem> items) {
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
            convertView = ListAdapterInflater.createLayout(resourceLayout, mContext, parent);
        }

        setupItemView(convertView, getItem(position));

        return convertView;
    }

    /**
     * Sets the correct text on a view based on an item.
     * @param view the view to place text on.
     * @param item the item to base text off.
     */
    private void setupItemView(View view, NewCollectedItem item) {
        if (item != null) {
            TextView nameText = view.findViewById(R.id.collected_name);
            TextView heightText = view.findViewById(R.id.collected_height);
            TextView pointsText = view.findViewById(R.id.collected_points);
            TextView positionText = view.findViewById(R.id.collected_position);
            TextView dateText = view.findViewById(R.id.collected_date);

            nameText.setText(item.getName());
            heightText.setText(mContext.getResources().getString(R.string.height_display,
                                            UIUtils.IntegerConvert(item.getHeight())));
            positionText.setText(mContext.getResources().getString(R.string.position_display,
                                                    item.getLongitude(), item.getLatitude()));
            pointsText.setText(mContext.getResources().getString(R.string.points_display,
                                                 UIUtils.IntegerConvert(item.getPoints())));
            dateText.setText(mContext.getResources().getString(R.string.date_display,
                                                                    item.getDate()));

            if (item.isTopInCountry()) {
                view.findViewById(R.id.collected_trophy).setVisibility(View.VISIBLE);
            }
            else {
                view.findViewById(R.id.collected_trophy).setVisibility(View.INVISIBLE);
            }

            positionText.setVisibility(View.GONE);
            dateText.setVisibility(View.GONE);

        }
    }
}
