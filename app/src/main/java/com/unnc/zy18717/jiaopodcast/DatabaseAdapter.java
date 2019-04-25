package com.unnc.zy18717.jiaopodcast;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String SONG_NAME = "song";
    public static final String COMMENT = "comment";

    private static final String SQLITE_TABLE = "myList";

    private static final String SQLITE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    SONG_NAME + " VARCHAR(256)," +
                    COMMENT + " VARCHAR(256)" +
                    ");";

    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    private Context context;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, "martinDB", null, 7);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("ae3mdp", "onCreate");
            db.execSQL(SQLITE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // do translation between database versions here
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }
    }

    public DatabaseAdapter(Context context) {
        this.context = context;
    }

    public DatabaseAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public void addComment(String song, String comment) {
        db.execSQL("INSERT INTO myList (song, comment) " + "VALUES " + "('" + song + "','" + comment + "');");
    }

    public Cursor fetchAll() {
        Cursor c = db.query("myList", new String[] { "_id", "song", "comment" }, null, null, null, null, null);
        return c;
    }

}
