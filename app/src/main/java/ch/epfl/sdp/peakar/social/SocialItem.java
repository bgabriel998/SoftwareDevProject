package ch.epfl.sdp.peakar.social;

public class SocialItem {
    private final String uid;
    private String username;
    private long score;

    /**
     * Constructor
     * @param uid used to find user in database.
     * @param username of user.
     * @param score user has.
     * @param profilePictureUrl to find the profile picture of user.
     */
    public SocialItem(String uid, String username, long score, String profilePictureUrl) {
        this.uid = uid;
        this.username = username;
        this.score = score;
        // TODO Create Bitmap?
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
     * Set the social item profile picture
     * @param url to the new profile picture.
     */
    public void setProfilePicture(String url) {

    }
}
