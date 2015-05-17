package jp.co.mobilusers.boardmessenger.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Action {

    private String      mId;
    private String      mBoardId;
    private String      mType;
    private String      mData;
    private long        mFrom;
    private long        mDuration;
    private String      mSender;

    private static final String TABLE           = "action";
    private static final String COL_ID          = "id";
    private static final String COL_BOARD_ID    = "board_id";
    private static final String COL_TYPE        = "type";
    private static final String COL_DATA        = "data";
    private static final String COL_FROM        = "_from";
    private static final String COL_DURATION    = "duration";
    private static final String COL_SENDER      = "sender";

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + "("
                + COL_ID        + " TEXT PRIMARY KEY,"
                + COL_BOARD_ID  + " TEXT,"
                + COL_TYPE      + " TEXT,"
                + COL_DATA      + " TEXT,"
                + COL_FROM      + " INTEGER,"
                + COL_DURATION  + " INTEGER,"
                + COL_SENDER    + " TEXT)");
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
    }

    public static Action fromJSONObject(JSONObject json) {
        Action action = new Action();
        action.mId = json.optString("id");
        action.mBoardId = json.optString("board_id");
        action.mType = json.optString("type");
        action.mData = json.optString("data");
        action.mFrom = json.optLong("from");
        action.mDuration = json.optLong("duration");
        action.mSender = json.optString("sender");
        return action;
    }

    public static Action fromCursor(Cursor cur) {
        Action action = new Action();
        int i = 0;
        action.mId = cur.getString(i++);
        action.mBoardId = cur.getString(i++);
        action.mType = cur.getString(i++);
        action.mData = cur.getString(i++);
        action.mFrom = cur.getLong(i++);
        action.mDuration = cur.getLong(i++);
        action.mSender = cur.getString(i++);
        return action;
    }

    public static ContentValues toContentValues(Action action) {
        ContentValues val = new ContentValues();
        val.put(COL_ID, action.mId);
        val.put(COL_BOARD_ID, action.mBoardId);
        val.put(COL_TYPE, action.mType);
        val.put(COL_DATA, action.mData);
        val.put(COL_FROM, action.mFrom);
        val.put(COL_DURATION, action.mDuration);
        val.put(COL_SENDER, action.mSender);
        return val;
    }

    public static void upsert(Context context, Action action) {
        DBHelper.getDB(context).insertWithOnConflict(
                TABLE,
                null,
                toContentValues(action),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void upsert(Context context, List<Action> actions) {
        SQLiteDatabase db = DBHelper.getDB(context);
        db.beginTransaction();
        for (Action a : actions) {
            db.insertWithOnConflict(
                    TABLE,
                    null,
                    toContentValues(a),
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public static List<Action> getAllOfBoard(Context context, String boardId) {
        Cursor cur = DBHelper.getDB(context).query(
                TABLE,
                null,
                COL_BOARD_ID + " = ?",
                new String[] { boardId },
                null,
                null,
                COL_FROM + " ASC");
        List<Action> ret = new ArrayList<Action>();
        while (cur.moveToNext()) {
            ret.add(Action.fromCursor(cur));
        }
        cur.close();
        return ret;
    }

    public static Action getLatestActionOfBoard(Context context, String boardId) {
        Cursor cur = DBHelper.getDB(context).query(
                TABLE,
                null,
                COL_BOARD_ID + " = ?",
                new String[] { boardId },
                null,
                null,
                COL_FROM + " DESC",
                "1");
        Action a = null;
        if (cur.moveToFirst()) {
            a = fromCursor(cur);
        }
        cur.close();
        return a;
    }

    static long getBoardLastActionTime(Context context, String boardId) {
        Cursor cur = DBHelper.getDB(context).query(
                TABLE,
                new String[] { COL_ID },
                COL_BOARD_ID + " = ?",
                new String[] { boardId },
                null, null,
                COL_ID + " DESC",
                "1");
        long time = -1;
        if (cur.moveToFirst()) {
            String id = cur.getString(0);
            time = Long.parseLong(id.substring(0, 8), 16) * 1000;
        }
        cur.close();
        return time;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getBoardId() {
        return mBoardId;
    }

    public void setBoardId(String boardId) {
        mBoardId = boardId;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    public long getFrom() {
        return mFrom;
    }

    public void setFrom(long from) {
        mFrom = from;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        mSender = sender;
    }
}
