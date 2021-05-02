package ch.epfl.sdp.peakar.general.remote;

/**
 * Interface for model classes that communicate with a remote resource. Each class implementing this interface must use a Database provider to control the remote resource.
 */
public interface RemoteResource {
    /**
     * Retrieve data from the remote resource.
     * @return Outcome of the retrieval.
     */
    RemoteOutcome retrieveData();

    /**
     * Load data into the local resource.
     */
    void loadData();
}
