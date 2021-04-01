package com.github.bgabriel998.softwaredevproject;

/**
 * Item holding username and points
 * used to fill rankings list.
 */
public class RankingItem {
    private String uid;
    private String username;
    private int points;

    /**
     * Constructor
     * @param username of user.
     * @param points user has.
     */
    public RankingItem(String uid, String username, int points) {
        this.uid = uid;
        this.username = username;
        this.points = points;
    }

    @Override
    public boolean equals( Object mob2) {
        return this.username.equals(((RankingItem) mob2).username) &&
                this.points == ((RankingItem) mob2).points &&
                this.uid.equals(((RankingItem) mob2).uid);
    }

    /**
     * Getter of the ranking item user id
     * @return id of the user on the database
     */
    public String getUid() {
        return uid;
    }

    /**
     * Getter of the ranking item username
     * @return username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter of the ranking item user points
     * @return points of the user
     */
    public int getPoints() {
        return points;
    }
}
