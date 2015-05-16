package jp.co.mobilusers.boardtutor.model;

import com.datdo.mobilib.util.MblUtils;

import java.util.List;

public class User {

    private static String TAG = MblUtils.getTag(User.class);

    private String mId;
    private String mNickname;
    private String mRole;
    private String mThumbnail;
    private String mEmail;

    public List<String> getAllUserIds() {
        return null;
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
}
