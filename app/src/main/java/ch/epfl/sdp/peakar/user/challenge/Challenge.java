package ch.epfl.sdp.peakar.user.challenge;

import java.util.Date;
import java.util.List;

public interface Challenge {

    /**
     * Get the users who joined the challenge.
     * @return the list of IDs of the users who joined this challenge.
     */
    List<Integer> getUsers();

    /**
     * Add a user to the challenge.
     */
    void addUser(int userID);

    /**
     * Get the points that will be assigned to the winner after the challenge ends.
     */
    int getPoints();

    /**
     * Returns <code>true</code> if the challenge is over or <code>false</code> otherwise.
     */
    boolean isOver();
}
