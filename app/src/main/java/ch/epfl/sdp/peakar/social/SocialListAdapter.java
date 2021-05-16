package ch.epfl.sdp.peakar.social;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.ListAdapterInflater;
import ch.epfl.sdp.peakar.utils.UIUtils;

/**
 * List adapter to display SocialItems.
 */
public class SocialListAdapter extends ArrayAdapter<SocialItem> {

    private final int resourceLayout;
    private final Context mContext;

    /**
     * Constructor
     * @param context the context
     * @param resource the resource layout to create list items of
     * @param items list items.
     */
    public SocialListAdapter(Context context, int resource, List<SocialItem> items) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ListAdapterInflater.createLayout(resourceLayout, mContext,parent);
        }

        setupItem(convertView, getItem(position), position + 1);

        return convertView;
    }

    /**
     * Setup the social item view, with correct info and colors.
     * @param v social item view
     * @param item the social item
     * @param rank the rank the social item has
     */
    private void setupItem(View v, SocialItem item, int rank) {
        if (item != null) {
            TextView usernameText = v.findViewById(R.id.social_item_username);
            TextView scoreText = v.findViewById(R.id.social_item_score);
            TextView rankText = v.findViewById(R.id.social_item_rank);
            ImageView medalView = v.findViewById(R.id.social_item_medal);

            setBackgroundColor(v, item.getUid());
            usernameText.setText(item.getUsername());
            scoreText.setText(mContext.getResources().getString(R.string.score_display,
                                             UIUtils.IntegerConvert(item.getScore())));
            rankText.setText(mContext.getResources().getString(R.string.rank_display,
                    UIUtils.IntegerConvert(rank)));
            setMedal(medalView, rank);
            // TODO Set profile picture, bitmap?
        }
    }

    /**
     * Set the correct background color
     * @param v view to set color to.
     * @param uid user id of social item.
     */
    private void setBackgroundColor(View v, String uid) {
        int backgroundColor = R.color.White;

        if (uid.equals(AuthService.getInstance().getID())) {
            backgroundColor = R.color.LightGrey;
        }

        v.setBackgroundResource(backgroundColor);
    }

    /**
     * Set correct medal or hide it.
     * @param v medal view
     * @param rank the rank of the social item.
     */
    private void setMedal(ImageView v, int rank) {
        switch (rank) {
            case 1 :
                v.setImageResource(R.drawable.social_medal_gold);
                break;
            case 2 :
                v.setImageResource(R.drawable.social_medal_silver);
                break;
            case 3 :
                v.setImageResource(R.drawable.social_medal_bronze);
                break;
            default :
                v.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
