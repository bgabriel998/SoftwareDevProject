package ch.epfl.sdp.peakar.user.challenge.goal;

import java.time.LocalDateTime;
import java.util.List;

import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.challenge.ChallengeOutcome;
import ch.epfl.sdp.peakar.user.challenge.ChallengeStatus;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * Challenge in which the goal is to have the maximum amount of points
 * before the end of the challenge
 */
public abstract class PointsChallenge implements Challenge {
    private final String id;
    private final List<String> users;
    private long awardPoints;
    private final int durationInDays;
    private int status;
    private final LocalDateTime creationDateTime;
    private LocalDateTime startDateTime;
    private LocalDateTime finishDateTime;

    /**
     * Create a new Points Challenge.
     * @param id unique identifier of the challenge.
     * @param users users who joined the challenge.
     * @param awardPoints points that will be awarded to the winner.
     */
    public PointsChallenge(String id, List<String> users, long awardPoints, int status,
                           LocalDateTime creationDateTime, int durationInDays,
                            LocalDateTime startDateTime, LocalDateTime finishDateTime) {
        this.id = id;
        this.users = users;
        this.awardPoints = awardPoints;
        this.startDateTime = startDateTime;
        this.finishDateTime = finishDateTime;
        this.status = status;
        this.creationDateTime = creationDateTime;
        this.durationInDays = durationInDays;
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


    @Override
    public LocalDateTime getStartDateTime() {return startDateTime;}

    @Override
    public void setStartDateTime(LocalDateTime startDateTime){
        this.startDateTime = startDateTime;
    }

    @Override
    public LocalDateTime getFinishDateTime() {return finishDateTime;}

    @Override
    public void setFinishDateTime(LocalDateTime finishDateTime){
        this.finishDateTime = finishDateTime;
    }

    @Override
    public LocalDateTime getCreationDateTime() {return creationDateTime;}

    @Override
    public int getDurationInDays() {return durationInDays;}

    @Override
    public int getStatus() {return status;}


    @Override
    public void setStatus(ChallengeStatus challengeStatus){
        status = challengeStatus.getValue();
    }
}
