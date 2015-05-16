package jp.co.mobilusers.boardmessenger.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class User {

    private String mUserId;
    private String mUserPassword;
    private String mAccessToken;

    private static final String TABLE               = "user";
    private static final String COL_USER_ID         = "user_id";
    private static final String COL_USER_PASSWORD   = "user_password";
    private static final String COL_ACCESS_TOKEN    = "access_token";

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + "("
                + COL_USER_ID       + " TEXT PRIMARY KEY,"
                + COL_USER_PASSWORD + " TEXT,"
                + COL_ACCESS_TOKEN  + " TEXT)");
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
    }

    public static User fromCursor(Cursor cur) {
        User user = new User();
        int i = 0;
        user.mUserId = cur.getString(i++);
        user.mUserPassword = cur.getString(i++);
        user.mAccessToken = cur.getString(i++);
        return user;
    }

    public static ContentValues toContentValues(User user) {
        ContentValues val = new ContentValues();
        val.put(COL_USER_ID, user.mUserId);
        val.put(COL_USER_PASSWORD, user.mUserPassword);
        val.put(COL_ACCESS_TOKEN, user.mAccessToken);
        return val;
    }

    public static void upsert(Context context, User user) {
        DBHelper.getDB(context).insertWithOnConflict(
                TABLE,
                null,
                toContentValues(user),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static User get(Context context) {
        Cursor cur = DBHelper.getDB(context).query(TABLE, null, null, null, null, null, "1");
        User user = null;
        if (cur.moveToFirst()) {
            user = fromCursor(cur);
        }
        cur.close();
        return user;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserPassword() {
        return mUserPassword;
    }

    public void setUserPassword(String userPassword) {
        mUserPassword = userPassword;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }
}
