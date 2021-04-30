package ch.epfl.sdp.peakar.database;

public class DatabaseEntry {
    public String email;
    public String username;
    public long score;

    public DatabaseEntry(String email, String username,long score){
        this.email = email;
        this.username = username;
        this.score = score;
    }


}
