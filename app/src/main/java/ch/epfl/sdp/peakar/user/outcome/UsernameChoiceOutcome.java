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
        int message = 0;
        switch(this) {
            case INVALID:
                message =  R.string.invalid_username;
                break;
            case USED:
                message = R.string.already_existing_username;
                break;
            case CHANGED:
                message = R.string.available_username;
                break;
            case CURRENT:
                message = R.string.current_username;
                break;
            case REGISTERED:
                message = R.string.registered_username;
                break;
        }
        return message;
    }
}
