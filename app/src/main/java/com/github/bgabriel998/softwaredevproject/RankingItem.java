package com.github.bgabriel998.softwaredevproject;

/**
 * Item holding username and points
 * used to fill rankings list.
 */
public class RankingItem {
    public String username;
    public int points;

    /**
     * Constructor
     * @param username of user.
     * @param points user has.
     */
    public RankingItem(String username, int points) {
        this.username = username;
        this.points = points;
    }

    @Override
    public boolean equals( Object mob2) {
        return this.username.equals( ((RankingItem) mob2).username) &&
                this.points == ((RankingItem) mob2).points;
    }
}
