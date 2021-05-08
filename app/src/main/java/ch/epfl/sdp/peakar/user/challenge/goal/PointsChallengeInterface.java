package ch.epfl.sdp.peakar.user.challenge.goal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class PointsChallengeInterface implements GoalChallengeInterface {
    private Set<Integer> users;
    private int awardPoints;
    private int goalPoints;
    private boolean over;

    @Override
    public List<Integer> getUsers() {
        return new ArrayList<>(users);
    }

    @Override
    public void addUser(int userID) {
        users.add(userID);
    }

    @Override
    public int getPoints() {
        return awardPoints;
    }

    public int getGoalPoints() {
        return goalPoints;
    }

    @Override
    public boolean isOver() {
        return over;
    }
}
