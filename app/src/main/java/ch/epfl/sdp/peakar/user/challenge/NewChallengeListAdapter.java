package ch.epfl.sdp.peakar.user.challenge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.ListAdapterInflater;

import static android.os.Looper.getMainLooper;

public class NewChallengeListAdapter extends ArrayAdapter<NewChallengeItem> {


    private final int resourceLayout;
    private final Context mContext;


    public NewChallengeListAdapter(@NonNull Context context, int resource, @NonNull List<NewChallengeItem> items) {
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
    private void setupItemView(View view, NewChallengeItem item) {
        if (item != null) {
            TextView nameText = view.findViewById(R.id.challenge_name);
            TextView startTimeText = view.findViewById(R.id.challenge_start_time);
            TextView finishTimeText = view.findViewById(R.id.challenge_stop_time);
            TextView enrolledUserSize = view.findViewById(R.id.challenge_enrolled_user_size);
            TextView remainingTime = view.findViewById(R.id.challenge_remaining_time);
            TextView pointsAchieved = view.findViewById(R.id.points_achieved);
            TextView rankingFirstUser = view.findViewById(R.id.challenge_first_user_txt);
            TextView rankingSecondUser = view.findViewById(R.id.challenge_second_user_txt);
            TextView rankingThirdUser = view.findViewById(R.id.challenge_third_user_txt);

            Uri profileImageUrl = AuthService.getInstance().getPhotoUrl();
            if(profileImageUrl == Uri.EMPTY) return;
            Glide.with(mContext)
                    .load(profileImageUrl)
                    .circleCrop()
                    .into((ImageView)view.findViewById(R.id.challenge_owner_picture));

            nameText.setText(item.getName());
            enrolledUserSize.setText(mContext.getResources().getString(R.string.enrolledUser_display, item.getNumberOfParticipants()));
            if(item.getStatus() == ChallengeStatus.PENDING.getValue()){
                view.setBackgroundResource(R.drawable.rounded_rect_grayed);
                nameText.setTextColor(Color.WHITE);
                startTimeText.setText(mContext.getResources().getString(R.string.notStartedStart_display));
                finishTimeText.setText(mContext.getResources().getString(R.string.notStartedFinish_display));
                remainingTime.setVisibility(View.INVISIBLE);
            }
            else{
                startTimeText.setText(mContext.getResources().getString(R.string.startTime_display, formatDateTimeString(item.getStartDateTime().toString())));
                finishTimeText.setText(mContext.getResources().getString(R.string.finishTime_display, formatDateTimeString(item.getEndDateTime().toString())));
                pointsAchieved.setText(mContext.getResources().getString(R.string.achieved_points,item.getChallengeRanking().get(item.getFounderID())));

                //Check if the logged user is the winner of the challenge
                if(item.getChallengeRanking().entrySet().stream()
                        .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                        .get().getKey().equals(item.getFounderID()))
                {
                    view.findViewById(R.id.collected_trophy).setVisibility(View.VISIBLE);
                }

                HashMap<String,Integer> ranking = item.getChallengeRanking();
                HashMap<String,String> enrolledUsers = item.getEnrolledUsers();
                Map.Entry<String, Integer> entry = ranking.entrySet().stream()
                        .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
                rankingFirstUser.setText(enrolledUsers.get(entry.getKey()) + " "+ entry.getValue()+"pts");
                ranking.remove(entry.getKey());

                Map.Entry<String, Integer> entrySec = ranking.entrySet().stream()
                        .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
                rankingSecondUser.setText(enrolledUsers.get(entrySec.getKey())  + " "+ entrySec.getValue()+"pts");
                ranking.remove(entrySec.getKey());
                if(ranking.size() != 0 ) {
                    Map.Entry<String, Integer> entryThird = ranking.entrySet().stream()
                            .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
                    rankingThirdUser.setText(enrolledUsers.get(entryThird.getKey())  + " " + entryThird.getValue() + "pts");
                    ranking.remove(entrySec.getKey());
                }
                addRemainingTimeHandler(view,item);
            }
        }
    }

    /**
     * Add handler for the remaining time display (updated every minutes)
     * @param view the view to place text on.
     * @param item the item to base text off.
     */
    private void addRemainingTimeHandler(View view,NewChallengeItem item){
        TextView remainingTime = view.findViewById(R.id.challenge_remaining_time);
        final Handler remainingTimeHandler = new Handler(getMainLooper());
        remainingTimeHandler.postDelayed(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                String remainingTimeStr = Duration.between(LocalDateTime.now(),item.getEndDateTime()).toString().split("M")[0].substring(2).toLowerCase() + "m";
                remainingTime.setText(remainingTimeStr);
                remainingTimeHandler.postDelayed(this, 1000*60);
            }
        }, 100);
    }

    /**
     * Makes date time string more readable
     * @param input locale date time string
     * @return formatted string
     */
    private String formatDateTimeString(String input) {
        return input.replace("T"," ").substring(0,input.length()-7).replace("-","/").replace(":","h");
    }
}
