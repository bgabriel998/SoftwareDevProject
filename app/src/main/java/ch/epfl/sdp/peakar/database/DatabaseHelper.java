package ch.epfl.sdp.peakar.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE = "countryHighPoint";
    private static final String DB_PATH = "/data/data/ch.epfl.sdp.peakar/databases/";
    private static final String DB_NAME = "CountryHighPoints.db";

    private SQLiteDatabase myDataBase;
    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context app context
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        this.createDataBase();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase(){

        //Copy content of the database (from file)
        this.getReadableDatabase();
        copyDataBase();


    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase(){

        try{
            //Open your local db as the input stream
            InputStream myInput = myContext.getAssets().open(DB_NAME);

            // Path to the just created empty db
            String outFileName = DB_PATH + DB_NAME;

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
        catch (Exception e) {
            Log.e("Excpetion",e.toString());
        }
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        return myDataBase;

    }


    public long queryHighestPeakHeight(String country){
        SQLiteDatabase rdb = getReadableDatabase();
        String selection = "Country" + " = ?";

        String[] selectionArgs = { country };

        Cursor cursor = rdb.query(TABLE,null,selection,selectionArgs,null,null, null);
        cursor.moveToNext();
        long height = cursor.getLong(cursor.getColumnIndexOrThrow("Height"));

        cursor.close();
        return height;
    }

    public String queryHighestPeakName(String country){
        String[] selectionArgs = { country };
        String selection = "Country" + " = ?";
        SQLiteDatabase rdb = getReadableDatabase();


        Cursor cursor = rdb.query(TABLE,null,selection,selectionArgs,null,null, null);
        cursor.moveToNext();
        String name = cursor.getString(cursor.getColumnIndexOrThrow("HighPoint"));
        cursor.close();
        return name;
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
        { myDataBase.close();}

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       // File file=new File(DB_PATH+);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion)
            copyDataBase();
    }


}