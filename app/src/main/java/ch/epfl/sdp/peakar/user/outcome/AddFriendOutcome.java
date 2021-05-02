package ch.epfl.sdp.peakar.user.outcome;

import ch.epfl.sdp.peakar.R;

public enum AddFriendOutcome {
    INVALID,
    CURRENT,
    ALREADY_ADDED,
    ADDED,
    NOT_PRESENT,
    FAIL;

    public int getMessage() {
        int message = 0;
        switch(this) {
            case INVALID:
                message = R.string.invalid_username;
                break;
            case ALREADY_ADDED:
                message = R.string.friend_already_added;
                break;
            case ADDED:
                message = R.string.friend_added;
                break;
            case CURRENT:
                message = R.string.add_current_username;
                break;
            case NOT_PRESENT:
                message = R.string.friend_not_present_db;
                break;
        }
        return message;
    }
}
