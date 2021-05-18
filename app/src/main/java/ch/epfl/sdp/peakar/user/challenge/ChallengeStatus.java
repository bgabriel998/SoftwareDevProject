package ch.epfl.sdp.peakar.user.challenge;

public enum ChallengeStatus {
    PENDING(0),
    ONGOING(1),
    ENDED(2);

    private final int value;

    ChallengeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
