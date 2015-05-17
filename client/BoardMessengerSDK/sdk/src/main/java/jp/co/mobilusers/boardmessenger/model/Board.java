package jp.co.mobilusers.boardmessenger.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private String      mId;
    private String[]    mMembers = new String[] {};
    private String      mName;
    private String      mBackground;
    private int         mWidth;
    private int         mHeight;
    private String      mExtra;
    private long        mLastActionTime; // transparent, not store to DB

    private static final String TABLE           = "board";
    private static final String COL_ID          = "id";
    private static final String COL_MEMBERS     = "members";
    private static final String COL_NAME        = "name";
    private static final String COL_BACKGROUND  = "background";
    private static final String COL_WIDTH       = "width";
    private static final String COL_HEIGHT      = "height";
    private static final String COL_EXTRA       = "mExtra";

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + "("
                + COL_ID            + " TEXT PRIMARY KEY,"
                + COL_MEMBERS       + " TEXT,"
                + COL_NAME          + " TEXT,"
                + COL_BACKGROUND    + " TEXT,"
                + COL_WIDTH         + " INTEGER,"
                + COL_HEIGHT        + " INTEGER,"
                + COL_EXTRA         + " TEXT)");
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
    }

    public static Board fromJSONObject(JSONObject json) {
        Board board = new Board();
        int i = 0;
        board.mId = json.optString("id");
        String members = json.optString("members");
        if (members != null) {
            board.mMembers = members.split(",");
        }
        board.mName = json.optString("name");
        board.mBackground = json.optString("background");
        board.mWidth = json.optInt("width");
        board.mHeight = json.optInt("height");
        board.mExtra = json.optString("extra");
        return board;
    }

    public static Board fromCursor(Cursor cur) {
        Board board = new Board();
        int i = 0;
        board.mId = cur.getString(i++);
        String members = cur.getString(i++);
        if (members != null) {
            board.mMembers = members.split(",");
        }
        board.mName = cur.getString(i++);
        board.mBackground = cur.getString(i++);
        board.mWidth = cur.getInt(i++);
        board.mHeight = cur.getInt(i++);
        board.mExtra = cur.getString(i++);
        return board;
    }

    public static ContentValues toContentValues(Board board) {
        ContentValues val = new ContentValues();
        val.put(COL_ID, board.mId);
        val.put(COL_MEMBERS, TextUtils.join(",", board.mMembers));
        val.put(COL_NAME, board.mName);
        val.put(COL_BACKGROUND, board.mBackground);
        val.put(COL_WIDTH, board.mWidth);
        val.put(COL_HEIGHT, board.mHeight);
        val.put(COL_EXTRA, board.mExtra);
        return val;
    }

    public static void upsert(Context context, Board board) {
        DBHelper.getDB(context).insertWithOnConflict(
                TABLE,
                null,
                toContentValues(board),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void upsert(Context context, List<Board> boards) {
        SQLiteDatabase db = DBHelper.getDB(context);
        db.beginTransaction();
        for (Board board : boards) {
            db.insertWithOnConflict(
                    TABLE,
                    null,
                    toContentValues(board),
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }



    public static Board get(Context context, String id) {
        SQLiteDatabase db = DBHelper.getDB(context);
        db.beginTransaction();
        Cursor cur = db.query(
                TABLE,
                null,
                COL_ID + " = ?",
                new String[] { id },
                null,
                null,
                null,
                "1");
        Board b = null;
        if (cur.moveToFirst()) {
            b = fromCursor(cur);
            b.mLastActionTime = Action.getBoardLastActionTime(context, b.getId());
        }
        cur.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        return b;
    }

    public static List<Board> getAll(Context context) {
        SQLiteDatabase db = DBHelper.getDB(context);
        db.beginTransaction();
        Cursor cur = db.query(TABLE, null, null, null, null, null, null);
        List<Board> ret = new ArrayList<Board>();
        while (cur.moveToNext()) {
            Board b = fromCursor(cur);
            b.mLastActionTime = Action.getBoardLastActionTime(context, b.getId());
            ret.add(b);
        }
        cur.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        return ret;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String[] getMembers() {
        return mMembers;
    }

    public void setMembers(String[] members) {
        mMembers = members;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getBackground() {
        return mBackground;
    }

    public void setBackground(String background) {
        mBackground = background;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    public long getLastActionTime() {
        return mLastActionTime;
    }

    public void setLastActionTime(long lastActionTime) {
        mLastActionTime = lastActionTime;
    }
}
