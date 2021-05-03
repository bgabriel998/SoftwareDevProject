package ch.epfl.sdp.peakar.user.outcome;

import ch.epfl.sdp.peakar.R;

/**
 * Enum representing possible outcomes of operations on profiles.
 * It's used to display the proper message after an operation on the model requested by the profile activity.
 */
public enum ProfileOutcome {
    INVALID(R.string.invalid_username),
    FRIEND_CURRENT(R.string.add_current_username),
    FRIEND_ALREADY_ADDED(R.string.friend_already_added),
    FRIEND_ADDED(R.string.friend_added),
    FRIEND_NOT_PRESENT(R.string.friend_not_present_db),
    USERNAME_CURRENT(R.string.current_username),
    USERNAME_USED(R.string.already_existing_username),
    USERNAME_CHANGED(R.string.available_username),
    USERNAME_REGISTERED(R.string.registered_username),
    FAIL(0);

    private final int message;

    ProfileOutcome(int message) {
        this.message = message;
    }

    /**
     * Get an integer representing a resource string to display.
     */
    public int getMessage() {
        return message;
    }
}
