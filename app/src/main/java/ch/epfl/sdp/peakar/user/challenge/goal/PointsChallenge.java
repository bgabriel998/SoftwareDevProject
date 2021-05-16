package ch.epfl.sdp.peakar.user.challenge.goal;

import java.time.LocalDateTime;
import java.util.List;

import ch.epfl.sdp.peakar.user.challenge.ChallengeOutcome;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * Challenge in which the goal is to reach a specified amount of points.
 */
public abstract class PointsChallenge implements GoalChallenge {
    String id;
    private final List<String> users;
    private long awardPoints;
    private final long goalPoints;
    private final LocalDateTime startDateTime;
    private final LocalDateTime finishDateTime;

    /**
     * Create a new Points Challenge.
     * @param id unique identifier of the challenge.
     * @param users users who joined the challenge.
     * @param awardPoints points that will be awarded to the winner.
     * @param goalPoints score to be reached to win.
     */
    public PointsChallenge(String id, List<String> users, long awardPoints, long goalPoints,
                            LocalDateTime startDateTime, LocalDateTime finishDateTime) {
        this.id = id;
        this.users = users;
        this.awardPoints = awardPoints;
        this.goalPoints = goalPoints;
        this.startDateTime = startDateTime;
        this.finishDateTime = finishDateTime;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ChallengeOutcome join() {
        // If the authenticated user has already joined this challenge.
        if(getUsers().contains(AuthService.getInstance().getID())) return ChallengeOutcome.NOT_POSSIBLE;
        awardPoints += AWARDED_POINTS_PER_USER;
        users.add(AuthService.getInstance().getID());
        return ChallengeOutcome.JOINED;
    }

    @Override
    public List<String> getUsers() {
        return users;
    }

    @Override
    public long getPoints() {
        return awardPoints;
    }

    public long getGoalPoints() {
        return goalPoints;
    }

    @Override
    public LocalDateTime getStartDateTime() {return startDateTime;}
    @Override
    public LocalDateTime getFinishDateTime() {return startDateTime;}

    @Override
    public boolean meetRequirements() {
        AuthAccount authAccount = AuthService.getInstance().getAuthAccount();
        long oldScore = authAccount.getScore();
        return oldScore >= getGoalPoints();
    }

    @Override
    public ChallengeOutcome claimVictory() {
        if(!meetRequirements()) return ChallengeOutcome.MISSING_REQUIREMENTS;
        // Otherwise, remove the challenge from the local account
        AuthService.getInstance().getAuthAccount().getChallenges().remove(this);
        return ChallengeOutcome.AWARDED;
    }
}
