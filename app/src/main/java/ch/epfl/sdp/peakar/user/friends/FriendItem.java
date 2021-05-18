package ch.epfl.sdp.peakar.user.friends;

/**
 * Item holding user attributes
 * used to fill friends list.
 */
public class FriendItem {
    private final String uid;
    private String username = "";
    private int points = 0;

    /**
     * Constructor
     * @param uid of user
     */
    public FriendItem(String uid) {
        this.uid = uid;
    }

    /**
     * Getter of the friend item username
     * @return username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter of the friend item username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter of the friend item user points
     * @return points of the user
     */
    public int getPoints() {
        return points;
    }

    /**
     * Setter of the friend item points
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Getter of the friend item UId
     */
    public String getUid() {
        return uid;
    }

    /**
     * Check if a friend item has a specific ID
     * @return true if the user has the target ID, false otherwise
     */
    public boolean hasID(String ID) {
        return this.uid.equals(ID);
    }
}