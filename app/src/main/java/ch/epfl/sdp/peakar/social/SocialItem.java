package ch.epfl.sdp.peakar.social;

import android.net.Uri;

/**
 * Item representing a user, used to show all users on the social view.
 */
public class SocialItem {
    private final String uid;
    private String username;
    private long score;
    private Uri profileUrl;

    /**
     * Constructor
     * @param uid used to find user in database.
     * @param username of user.
     * @param score user has.
     * @param profileUrl to the profile picture the user has.
     */
    public SocialItem(String uid, String username, long score, Uri profileUrl) {
        this.uid = uid;
        this.username = username;
        this.score = score;
        this.profileUrl = profileUrl;
    }

    /**
     * Getter of the social item user id
     * @return id of the user on the database
     */
    public String getUid() {
        return uid;
    }

    /**
     * Getter of the social item username
     * @return username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter of the social item user score
     * @return score of the user
     */
    public long getScore() {
        return score;
    }

    /**
     * Getter of the social item user profile image url
     * @return profile image url of the user
     */
    public Uri getProfileUrl() {
        return profileUrl;
    }

    /**
     * Set the social item username
     * @param username given new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the social item score
     * @param score given new score
     */
    public void setScore(Long score) {
        this.score = score;
    }

    /**
     * Set the social item profile image url
     * @param profileUrl given new profile image url
     */
    public void setProfileUrl(Uri profileUrl) {
        this.profileUrl = profileUrl;
    }
}
