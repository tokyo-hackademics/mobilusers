package jp.co.mobilusers.boardmessenger.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.User;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "BoardMessengerSDK.db";
    private static final int DB_VERSION = 1;
    private static DBHelper sInstance;

    public static SQLiteDatabase getDB(Context context) {
        return getInstance(context).getWritableDatabase();
    }

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context);
        }
        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        createTables(db);
    }

    public static void createTables(SQLiteDatabase db) {
        Board.createTable(db);
        Action.createTable(db);
        User.createTable(db);
    }

    public static void dropTables(SQLiteDatabase db) {
        Board.dropTable(db);
        Action.dropTable(db);
        User.dropTable(db);
    }

    public static void dropAndCreateTable(Context context) {
        SQLiteDatabase db = getDB(context);
        db.beginTransaction();
        dropTables(db);
        createTables(db);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
