package jp.co.mobilusers.boardmessenger.model;

/**
 * Created by dat on 5/16/15.
 */
public class Board {


    private String      mId;
    private String[]    mMembers = new String[] {};
    private String      mName;
    private String      mBackground;
    private int         mWidth;
    private int         mHeight;
    private String      mExtra;
    private long        mLastActionId; // transparent, not store to DB

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

    public long getLastActionId() {
        return mLastActionId;
    }

    public void setLastActionId(long lastActionId) {
        mLastActionId = lastActionId;
    }

}
