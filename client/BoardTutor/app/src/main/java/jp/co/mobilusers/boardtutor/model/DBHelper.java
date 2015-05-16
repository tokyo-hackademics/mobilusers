package jp.co.mobilusers.boardtutor.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.datdo.mobilib.util.MblUtils;


class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "BoardTutor.db";
    private static final int DB_VERSION = 1;
    private static DBHelper sInstance;

    public static SQLiteDatabase getDB() {
        return getInstance(MblUtils.getCurrentContext()).getWritableDatabase();
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
        User.createTable(db);
    }

    public static void dropTables(SQLiteDatabase db) {
        User.dropTable(db);
    }
}
