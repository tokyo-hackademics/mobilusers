package jp.co.mobilusers.boardmessenger.model;

/**
 * Created by dat on 5/16/15.
 */
public class Action {

    private String      mId;
    private String      mBoardId;
    private String      mType;
    private String      mData;
    private long        mFrom;
    private long        mDuration;
    private String      mSender;

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
