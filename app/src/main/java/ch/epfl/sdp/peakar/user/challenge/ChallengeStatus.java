package ch.epfl.sdp.peakar.user.challenge;

/**
 * Enum used to determine the challenge status
 */
public enum ChallengeStatus {
    PENDING(0), // Challenge has not started yet (waiting for a user to join)
    ONGOING(1), // Challenge is ongoing
    ENDED(2);   // Challenge is finished, waiting for all users to connect before deletion

    private final int value;

    /**
     * Constructor
     * @param value int value for enum
     */
    ChallengeStatus(int value) {
        this.value = value;
    }

    /**
     * Get integer value out of enum member
     * @return int value
     */
    public int getValue() {
        return value;
    }
}
