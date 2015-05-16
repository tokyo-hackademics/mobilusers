package jp.co.mobilusers.boardtutor.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.datdo.mobilib.util.MblUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {

    private static String TAG = MblUtils.getTag(User.class);

    private String mId;
    private String mNickname;
    private String mRole;
    private String mThumbnail;
    private String mEmail;
    private boolean isMe;

    private static final String TABLE = "user";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE + "("
                        + "id           TEXT NOT NULL,"
                        + "nickname     TEXT,"
                        + "role         TEXT,"
                        + "thumbnail    TEXT,"
                        + "email        TEXT,"
                        + "is_me        INTEGER)");
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
    }

    public static User get(String id) {
        List<User> ret = get(Arrays.asList(new String[]{id}));
        if (!MblUtils.isEmpty(ret)) {
            return ret.get(0);
        } else {
            return null;
        }
    }

    public static User getMe() {
        Cursor cur = DBHelper.getDB().query(TABLE, null, "is_me = 1", null, null, null, null);
        User user = null;
        if (cur.moveToFirst()) {
            user = fromCursor(cur);
        }
        cur.close();
        return user;
    }

    public static List<User> get(List<String> ids) {
        String[] q = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            q[i] = "?";
        }
        Cursor cur = DBHelper.getDB().query(
                TABLE,
                null,
                "id in (" + TextUtils.join(",", q) + ")",
                ids.toArray(new String[ids.size()]),
                null, null, null);
        List<User> ret = new ArrayList<User>();
        List<String> notFoundIds = new ArrayList<String>(ids);
        while (cur.moveToNext()) {
            User u = fromCursor(cur);
            ret.add(u);
            notFoundIds.remove(u.getId());
        }
        cur.close();

        for (String userId : notFoundIds) {
            User u = new User();
            u.setId(userId);
            u.setEmail(userId);
            u.setNickname("???");
            ret.add(u);
        }

        return ret;
    }

    public static List<String> getIdsByEmails(List<String> emails) {
        if (MblUtils.isEmpty(emails)) {
            return new ArrayList<String>();
        }

        List<String> placeholders = new ArrayList<String>();
        for (int i = 0; i < emails.size(); i++) {
            placeholders.add("?");
        }
        Cursor cur = DBHelper.getDB().query(
                TABLE,
                new String[] { "id" },
                "email in (" + TextUtils.join(",", placeholders)+ ")",
                emails.toArray(new String[emails.size()]),
                null, null, null);
        List<String> ret = new ArrayList<String>();
        while (cur.moveToNext()) {
            ret.add(cur.getString(0));
        }
        cur.close();
        return ret;
    }

    public static List<User> getAll() {
        Cursor cur = DBHelper.getDB().query(TABLE, null, "is_me = 0", null, null, null, null);
        List<User> ret = new ArrayList<User>();
        while (cur.moveToNext()) {
            ret.add(fromCursor(cur));
        }
        cur.close();
        return ret;
    }

    public static void upsert(User user) {
        upsert(Arrays.asList(new User[]{user}));
    }

    public static void upsert(List<User> users) {
        DBHelper.getDB().beginTransaction();
        for (User u : users) {
            Cursor cur = DBHelper.getDB().rawQuery(
                    "select 1 from " + TABLE + " where id = ?",
                    new String[] { u.getId() });
            if (cur.moveToFirst()) {
                DBHelper.getDB().update(TABLE, toContentValues(u), "id = ?", new String[] { u.getId() });
            } else {
                DBHelper.getDB().insert(TABLE, null, toContentValues(u));
            }
            cur.close();
        }
        DBHelper.getDB().setTransactionSuccessful();
        DBHelper.getDB().endTransaction();
    }

    private static User fromCursor(Cursor cur) {
        User u = new User();
        int i = 0;
        u.setId(cur.getString(i++));
        u.setNickname(cur.getString(i++));
        u.setRole(cur.getString(i++));
        u.setThumbnail(cur.getString(i++));
        u.setEmail(cur.getString(i++));
        u.setMe(cur.getInt(i++) > 0);
        return u;
    }

    private static ContentValues toContentValues(User user) {
        ContentValues val = new ContentValues();
        val.put("id", user.getId());
        val.put("nickname", user.getNickname());
        val.put("role", user.getRole());
        val.put("thumbnail", user.getThumbnail());
        val.put("email", user.getEmail());
        val.put("is_me", user.isMe());
        return val;
    }

    public static User fromJSON(JSONObject jo) {
        User u = new User();
        u.setId(jo.optString("id"));
        u.setNickname(jo.optString("nickname"));
        u.setRole(jo.optString("role"));
        u.setThumbnail(jo.optString("thumbnailUrl"));
        u.setEmail(jo.optString("email"));
        u.setMe(jo.optBoolean("is_me"));
        return u;
    }

    public List<String> getAllUserIds() {
        Cursor cur = DBHelper.getDB().query(
                TABLE,
                new String[] { "id" },
                "is_me = 0",
                null, null, null, null);
        List<String> ids = new ArrayList<String>();
        while (cur.moveToNext()) {
            ids.add(cur.getString(0));
        }
        cur.close();
        return ids;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public String getRole() {
        return mRole;
    }

    public void setRole(String role) {
        mRole = role;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }
}
