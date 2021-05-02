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
        switch(this) {
            case INVALID:
                return R.string.invalid_username;
            case ALREADY_ADDED:
                return R.string.friend_already_added;
            case ADDED:
                return R.string.friend_added;
            case CURRENT:
                return R.string.add_current_username;
            case NOT_PRESENT:
                return R.string.friend_not_present_db;
            default: return 0;
        }
    }
}
