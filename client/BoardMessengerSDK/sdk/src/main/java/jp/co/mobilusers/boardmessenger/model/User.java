package jp.co.mobilusers.boardmessenger.model;

/**
 * Created by dat on 5/16/15.
 */
public class User {

    private String mUserId;
    private String mUserPassword;
    private String mAccessToken;

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
