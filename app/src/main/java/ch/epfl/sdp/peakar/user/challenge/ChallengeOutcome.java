package ch.epfl.sdp.peakar.user.challenge;

/**
 * Enum to model results of operations on challenges.
 */
public enum ChallengeOutcome {
    CREATED,
    JOINED,
    ALREADY_OVER,
    AWARDED,
    NOT_POSSIBLE,
    MISSING_REQUIREMENTS
}
