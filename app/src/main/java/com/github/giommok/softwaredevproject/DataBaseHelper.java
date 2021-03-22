package com.github.giommok.softwaredevproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TABLE = "countryHighPoint";

    private static String DB_PATH = "/data/data/com.github.bgabriel998.softwaredevproject/databases/";

    //replace this with name of your db file which you copied into asset folder
    private static String DB_NAME = "CountryHighPoints.db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context app context
     */
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        this.createDataBase();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase(){
        try {
            boolean dbExist = checkDataBase();

            if(dbExist){
                //do nothing - database already exist
            }else{
                //By calling this method and empty database will be created into the default system path
                //of your application so we are gonna be able to overwrite that database with our database.
                this.getReadableDatabase();


                copyDataBase();

            }
        }
        catch (Exception e) {

        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
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
        SQLiteDatabase rdb = getReadableDatabase();
        String selection = "Country" + " = ?";

        String[] selectionArgs = { country };

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