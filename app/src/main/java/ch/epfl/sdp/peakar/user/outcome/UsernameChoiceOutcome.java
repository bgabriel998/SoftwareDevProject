package ch.epfl.sdp.peakar.user.outcome;

import ch.epfl.sdp.peakar.R;

public enum UsernameChoiceOutcome {
    INVALID,
    CURRENT,
    USED,
    CHANGED,
    REGISTERED,
    FAIL;

    public int getMessage() {
        switch(this) {
            case INVALID:
                return R.string.invalid_username;
            case USED:
                return R.string.already_existing_username;
            case CHANGED:
                return R.string.available_username;
            case CURRENT:
                return R.string.current_username;
            case REGISTERED:
                return R.string.registered_username;
            default: return 0;
        }
    }
}
