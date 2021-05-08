package ch.epfl.sdp.peakar.user.challenge.deadline;

import java.util.Date;

import ch.epfl.sdp.peakar.user.challenge.Challenge;

public interface DeadlineChallengeInterface extends Challenge {

    /**
     * Get the deadline of the challenge.
     */
    Date getDeadline();

}
