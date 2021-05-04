package ch.epfl.sdp.peakar.user.services;

/**
 * Enum describing the possible providers of auth for users.
 * It is used in the auth classes to map each provider to the correct credential retrieval operations.
 */
public enum AuthProvider {
    GOOGLE,
    FACEBOOK
}
