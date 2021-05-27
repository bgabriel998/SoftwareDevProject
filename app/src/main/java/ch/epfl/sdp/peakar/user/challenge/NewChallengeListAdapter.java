package ch.epfl.sdp.peakar.user.challenge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.ListAdapterInflater;

import static android.os.Looper.getMainLooper;

public class NewChallengeListAdapter extends ArrayAdapter<NewChallengeItem> {
    private final int resourceLayout;
    private final Context mContext;

    /**
     * Class constructor, setup challenges items
     * @param context application context
     * @param resource resource
     * @param items list of challenges items
     */
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
            TextView enrolledUserSize = view.findViewById(R.id.challenge_enrolled_user_size);

            //Set founder profile picture
            addFounderProfilePictureToItem(view,item.getFounderURI());
            nameText.setText(item.getName());
            enrolledUserSize.setText(mContext.getResources().getString(R.string.enrolledUser_display, item.getNumberOfParticipants()));

            //Setup item differently if the challenge is PENDING or ONGOING/ENDED
            if(item.getStatus() == ChallengeStatus.PENDING.getValue()){
                setupUnStartedChallenge(view);
            }
            else{
                setupOngoingChallenge(view,item);
            }

            if(item.isAuthAccountFounder() && !item.isAuthAccountEnrolled() && item.isAuthAccount()){
                view.findViewById(R.id.join_button).setVisibility(View.VISIBLE);
                Button joinButton = view.findViewById(R.id.join_button);
                joinButton.setOnClickListener(new JoinOnClickListenerClass(item));
            }
        }
    }

    /**
     * Add handler for the remaining time display (updated every minutes)
     * @param view the view to place text on.
     * @param item the item to base text off.
     */
    @SuppressLint("NewApi")
    private void addRemainingTimeHandler(View view, NewChallengeItem item){
        TextView remainingTime = view.findViewById(R.id.challenge_remaining_time);
        if(Duration.between(LocalDateTime.now(),item.getEndDateTime()).isNegative()){
            remainingTime.setText(mContext.getString(R.string.challenge_finished));
            remainingTime.setTextColor(Color.RED);
            return;
        }

        //Add handler for stopwatch time
        final Handler remainingTimeHandler = new Handler(getMainLooper());
        remainingTimeHandler.postDelayed(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                Duration duration = Duration.between(LocalDateTime.now(),item.getEndDateTime());
                String remainingTimeStr = duration.toString().split("M")[0].substring(2).toLowerCase() + "m";
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

    /**
     * Retrieve founder profile picture and place it in image view of challenge item
     * @param view the view to place image on.
     * @param profileImageUrl uri of the founder to place in the image view
     */
    private void addFounderProfilePictureToItem(View view,Uri profileImageUrl){
        if(profileImageUrl == Uri.EMPTY) return;
        Glide.with(mContext)
                .load(profileImageUrl)
                .circleCrop()
                .into((ImageView)view.findViewById(R.id.challenge_owner_picture));
    }

    /**
     * Add usernames and gained points to the podium section
     * @param view the view to place text on.
     * @param item the item to base text off.
     */
    @SuppressLint("SetTextI18n")
    private void displayPodium(View view, NewChallengeItem item){
        TextView rankingFirstUser = view.findViewById(R.id.challenge_first_user_txt);
        TextView rankingSecondUser = view.findViewById(R.id.challenge_second_user_txt);
        TextView rankingThirdUser = view.findViewById(R.id.challenge_third_user_txt);
        HashMap<String,Integer> ranking = new HashMap<>(item.getChallengeRanking());
        HashMap<String,String> enrolledUsers = item.getEnrolledUsers();

        //Add 1st
        Map.Entry<String, Integer> entry = ranking.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
        rankingFirstUser.setText(enrolledUsers.get(entry.getKey()) + " "+ entry.getValue()+mContext.getString(R.string.challenge_points));
        ranking.remove(entry.getKey());

        //Add 2nd
        Map.Entry<String, Integer> entrySec = ranking.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
        rankingSecondUser.setText(enrolledUsers.get(entrySec.getKey())  + " "+ entrySec.getValue()+mContext.getString(R.string.challenge_points));
        ranking.remove(entrySec.getKey());

        //Add third if more than two users are enrolled
        if(ranking.size() != 0 ) {
            Map.Entry<String, Integer> entryThird = ranking.entrySet().stream()
                    .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
            rankingThirdUser.setText(enrolledUsers.get(entryThird.getKey())  + " " + entryThird.getValue()+mContext.getString(R.string.challenge_points));
            ranking.remove(entrySec.getKey());
        }
    }


    /**
     * Setup item containing unStarted challenge
     * @param view view to setup
     */
    private void setupUnStartedChallenge(View view){
        TextView nameText = view.findViewById(R.id.challenge_name);
        TextView startTimeText = view.findViewById(R.id.challenge_start_time);
        TextView finishTimeText = view.findViewById(R.id.challenge_stop_time);
        TextView remainingTime = view.findViewById(R.id.challenge_remaining_time);

        view.setBackgroundResource(R.drawable.rounded_rect_grayed);
        nameText.setTextColor(Color.WHITE);
        startTimeText.setText(mContext.getResources().getString(R.string.notStartedStart_display));
        finishTimeText.setText(mContext.getResources().getString(R.string.notStartedFinish_display));
        remainingTime.setVisibility(View.INVISIBLE);
    }

    /**
     * Setup fields for ongoing challenges
     * @param view view to setup
     * @param item started challenge
     */
    private void setupOngoingChallenge(View view, NewChallengeItem item){
        TextView startTimeText = view.findViewById(R.id.challenge_start_time);
        TextView finishTimeText = view.findViewById(R.id.challenge_stop_time);
        TextView pointsAchieved = view.findViewById(R.id.points_achieved);

        startTimeText.setText(mContext.getResources().getString(R.string.startTime_display, formatDateTimeString(item.getStartDateTime().toString())));
        finishTimeText.setText(mContext.getResources().getString(R.string.finishTime_display, formatDateTimeString(item.getEndDateTime().toString())));
        if(item.isAuthAccount() && item.isAuthAccountEnrolled())
            pointsAchieved.setText(mContext.getResources().getString(R.string.achieved_points,item.getChallengeRanking().get(AuthService.getInstance().getID())));

        //Check if the logged user is the winner of the challenge
        if(item.getChallengeRanking().entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get().getKey().equals(AuthService.getInstance().getID()))
        {
            view.findViewById(R.id.collected_trophy).setVisibility(View.VISIBLE);
        }

        //Display podium
        displayPodium(view,item);

        //Add stopwatch handler for time that is up
        addRemainingTimeHandler(view,item);

    }

    /**
     * Extended OnClickListener
     * Used to pass correct NewChallengeItem item to the listener
     * This onClickListener is used only for join button on challenges items
     */
    private class JoinOnClickListenerClass implements View.OnClickListener{
        private final NewChallengeItem item;
        public JoinOnClickListenerClass(NewChallengeItem item){
            this.item = item;
        }
        @Override
        public void onClick(View v){
            if(!Database.getInstance().isOnline()) {
                Snackbar snackbar = Snackbar.make(v.findViewById(android.R.id.content), ProfileOutcome.FAIL.getMessage(), Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }
            //Join Challenge and
            item.getRemotePointsChallenge().join();
            v.setVisibility(View.INVISIBLE);

        }
    }
}
