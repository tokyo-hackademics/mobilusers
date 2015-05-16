package jp.co.mobilusers.boardtutor.auth;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.datdo.mobilib.api.MblApi;
import com.datdo.mobilib.api.MblApi.MblApiCallback;
import com.datdo.mobilib.util.MblUtils;

import jp.co.mobilusers.boardtutor.BuildConfig;
import jp.co.mobilusers.boardtutor.R;
import jp.co.mobilusers.boardtutor.model.User;

public class GoogleApi {
    private static final String TAG = MblUtils.getTag(GoogleApi.class);

    private static final String OAUTH_REDIRECT_URI = "http://localhost/oauthcallback";

    private static final String PREF_ACCESS_TOKEN   = "access_token";
    private static final String PREF_REFRESH_TOKEN  = "refresh_token";
    private static final String PREF_EXPIRES_AT     = "expires_at";
    private static final String PREF_TOKEN_TYPE     = "token_type";

    private static final String PREF_RETRIEVED_ACCESS_TOKEN  = "retrieved_access_token";
    private static final String PREF_RETRIEVED_MY_PROFILE     = "retrieved_user_info";
    private static final String PREF_RETRIEVED_ALL_CONTACTS  = "retrieved_all_contacts";

    private static final long _1_MINUTE = 1000l * 60l;

    private static final boolean IGNORE_SSL = true;

    private static String   sAccessToken;
    private static String   sRefreshToken;
    private static long     sExpiresAt;
    private static String   sTokenType;

    private static String   sCurrentUserId;

    public static enum Method {
        GET,
        POST;
    }

    private static interface ApiCallback {
        public void onSuccess(byte[] data);
        public void onError();
    }

    public static void init() {
        SharedPreferences prefs = MblUtils.getPrefs();
        sAccessToken    = prefs.getString   (PREF_ACCESS_TOKEN,     null);
        sRefreshToken   = prefs.getString   (PREF_REFRESH_TOKEN,    null);
        sExpiresAt      = prefs.getLong     (PREF_EXPIRES_AT,       0);
        sTokenType      = prefs.getString   (PREF_TOKEN_TYPE,       null);
    }

    private static Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", sTokenType + " " + sAccessToken);
        return headers;
    }

    private static void runAPI(
            final String url,
            final Map<String, Object> params,
            final Method method,
            final boolean eternalCache,
            final ApiCallback callback) {

        long now = System.currentTimeMillis();

        if (sExpiresAt - now > _1_MINUTE) {

            doRunAPI(url, params, method, eternalCache, callback);

        } else {

            refreshAccessToken(new SimpleCallback() {
                @Override
                public void onSuccess() {
                    doRunAPI(url, params, method, eternalCache, callback);
                }
                @Override
                public void onError() {
                    if (callback != null) {
                        callback.onError();
                    }
                }
            });
        }
    }

    private static void doRunAPI(
            String url,
            Map<String, Object> params,
            Method method,
            boolean eternalCache,
            final ApiCallback callback) {

        MblApiCallback apiCallback = new MblApiCallback() {

            @Override
            public void onSuccess(int statusCode, byte[] data) {
                if (callback != null) {
                    callback.onSuccess(data);
                }
            }

            @Override
            public void onFailure(int error, String errorMessage) {
                if (callback != null) {
                    callback.onError();
                }
            }
        };

        if (method == Method.GET) {
            MblApi.get(
                    url,
                    params,
                    getAuthHeaders(),
                    eternalCache ? Long.MAX_VALUE : -1,
                    IGNORE_SSL,
                    apiCallback,
                    null);
        } else if (method == Method.POST){
            MblApi.post(
                    url,
                    params,
                    getAuthHeaders(),
                    IGNORE_SSL,
                    apiCallback,
                    null);
        }
    }

    private static void refreshAccessToken(final SimpleCallback callback) {

        Log.d(TAG, "refreshAccessToken: sExpiresAt=" + sExpiresAt + ", now=" + System.currentTimeMillis());

        String url = "https://accounts.google.com/o/oauth2/token";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refresh_token", sRefreshToken);
        params.put("client_id",     BuildConfig.GOOGLE_CLIENT_ID);
        params.put("grant_type",    "refresh_token");

        MblApi.post(
                url,
                params,
                null,
                IGNORE_SSL,
                new MblApiCallback() {

                    @Override
                    public void onSuccess(int statusCode, byte[] data) {
                        try {
                            JSONObject json = new JSONObject(new String(data));

                            String  accessToken = json.getString    ("access_token");
                            long    expiresIn   = json.getLong      ("expires_in");
                            String  tokenType   = json.getString    ("token_type");

                            saveAuthInfo(accessToken, sRefreshToken, expiresIn, tokenType);

                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, "", e);
                            if (callback != null) {
                                callback.onError();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int error, String errorMessage) {
                        if (callback != null) {
                            callback.onError();
                        }
                    }
                },
                null);
    }

    public static void startOauth(WebView webView, final SimpleCallback callback) {

        String url = Uri.parse("https://accounts.google.com").buildUpon()
                .path("/o/oauth2/auth")
                .appendQueryParameter("scope", TextUtils.join(" ", new String[] {
                        "https://www.google.com/m8/feeds",
                        "https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile"
                }))
                .appendQueryParameter("state",          "profile")
                .appendQueryParameter("redirect_uri",   OAUTH_REDIRECT_URI)
                .appendQueryParameter("response_type",  "code")
                .appendQueryParameter("client_id",      BuildConfig.GOOGLE_CLIENT_ID)
                .appendQueryParameter("access_type",    "offline")
                .appendQueryParameter("hl", "ja")
                .build().toString();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!url.startsWith(OAUTH_REDIRECT_URI)) {
                    MblUtils.showProgressDialog(R.string.wait, false);
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!url.startsWith(OAUTH_REDIRECT_URI)) {
                    MblUtils.hideProgressDialog();
                }
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(OAUTH_REDIRECT_URI)) {
                    String code = Uri.parse(url).getQueryParameter("code");
                    onRetrieveCode(code, callback);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {

                if (failingUrl.startsWith(OAUTH_REDIRECT_URI)) {

                    webView.loadUrl("about:blank");

                    String code = Uri.parse(failingUrl).getQueryParameter("code");
                    onRetrieveCode(code, callback);
                    return;
                }
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (IGNORE_SSL) {
                    handler.proceed();
                } else {
                    super.onReceivedSslError(view, handler, error);
                }
            }
        });

        webView.loadUrl(url);
    }

    private static void onRetrieveCode(String code, final SimpleCallback callback) {

        String url = "https://accounts.google.com/o/oauth2/token";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", code);
        params.put("client_id",     BuildConfig.GOOGLE_CLIENT_ID);

        params.put("redirect_uri",  OAUTH_REDIRECT_URI);
        params.put("grant_type",    "authorization_code");

        MblApi.post(
                url,
                params,
                null,
                IGNORE_SSL,
                new MblApiCallback() {

                    @Override
                    public void onSuccess(int statusCode, byte[] data) {
                        try {
                            JSONObject json = new JSONObject(new String(data));
                            String  accessToken     = json.getString    ("access_token");
                            String  refreshToken    = json.getString    ("refresh_token");
                            long    expiresIn       = json.getLong      ("expires_in");
                            String  tokenType       = json.getString    ("token_type");

                            saveAuthInfo(accessToken, refreshToken, expiresIn, tokenType);

                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, "", e);
                            if (callback != null) {
                                callback.onError();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int error, String errorMessage) {
                        if (callback != null) {
                            callback.onError();
                        }
                    }
                },
                null);

    }

    private static void getMyProfile(final SimpleCallback callback) {

        String url = "https://www.googleapis.com/oauth2/v1/userinfo";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("access_token",  sAccessToken);
        params.put("alt",           "json");

        runAPI(url, params, Method.GET, false, new ApiCallback() {

            @Override
            public void onSuccess(byte[] data) {
                try {
                    JSONObject json = new JSONObject(new String(data));
                    String email    = json.optString("email");
                    String name     = json.optString("name");
                    String picture  = json.optString("picture");

                    User me = new User();
                    me.setId(email);
                    me.setEmail(email);
                    me.setNickname(name);
                    me.setThumbnail(picture);
                    me.setMe(true);

                    sCurrentUserId = email;
                    User.upsert(me);

                    MblUtils.getPrefs().edit()
                            .putBoolean(PREF_RETRIEVED_MY_PROFILE, true)
                            .commit();

                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "", e);
                    if (callback != null) {
                        callback.onError();
                    }
                }
            }

            @Override
            public void onError() {
                if (callback != null) {
                    callback.onError();
                }
            }
        });
    }

    private static void getContacts(final SimpleCallback callback) {

        String url = "https://www.google.com/m8/feeds/contacts/default/full";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("v",             "3.0");
        params.put("max-results",   Integer.MAX_VALUE);
        params.put("alt",           "json");

        runAPI(
                url,
                params,
                Method.GET,
                false,
                new ApiCallback() {

                    @Override
                    public void onSuccess(final byte[] data) {
                        MblUtils.executeOnAsyncThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject json = new JSONObject(new String(data));
                                    JSONObject jsnFeed = json.optJSONObject("feed");
                                    if (jsnFeed != null) {
                                        saveContactsToDb(jsnFeed.optJSONArray("entry"));
                                    }

                                    if (callback != null) {
                                        MblUtils.executeOnMainThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onSuccess();
                                            }
                                        });
                                    }
                                } catch (Throwable e) {
                                    Log.e(TAG, "", e);
                                    if (callback != null) {
                                        MblUtils.executeOnMainThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onError();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        if (callback != null) {
                            callback.onError();
                        }
                    }
                });
    }

    private static void saveContactsToDb(JSONArray jsnContacts) throws JSONException, ParseException {

        if (MblUtils.isEmpty(jsnContacts)) {
            return;
        }

        List<User> users = new ArrayList<User>();
        for (int i = 0; i < jsnContacts.length(); i++) {
            JSONObject jsnContact = jsnContacts.getJSONObject(i);

            // emails
            List<String> emails = new ArrayList<String>();
            JSONArray jsnEmails = jsnContact.optJSONArray("gd$email");
            if (!MblUtils.isEmpty(jsnEmails)) {
                for (int k = 0; k < jsnEmails.length(); k++) {
                    emails.add(jsnEmails.getJSONObject(k).getString("address"));
                }
            }

            // name
            JSONObject jsnName = jsnContact.optJSONObject("gd$name");
            String name = null;
            if (jsnName != null) {
                JSONObject jsnFullname = jsnName.optJSONObject("gd$fullName");
                if (jsnFullname != null) {
                    name = jsnFullname.optString("$t");
                }
            }

            // avatar
            String avatar = null;
            JSONArray jsnLinks = jsnContact.optJSONArray("link");
            if (!MblUtils.isEmpty(jsnLinks)) {
                for (int k = 0; k < jsnLinks.length(); k++) {
                    JSONObject jsonLink = jsnLinks.getJSONObject(k);
                    String type = jsonLink.optString("type");
                    if (TextUtils.equals(type, "image/*") && jsonLink.has("gd$etag")) {
                        avatar = jsonLink.optString("href");
                        if (!MblUtils.isEmpty(avatar)) break;
                    }
                }
            }

            for (String email : emails) {

                if (TextUtils.equals(email, getCurrentUserId())) {
                    continue;
                }

                User user = new User();
                user.setId(email);
                user.setEmail(email);
                if (name  != null) {
                    user.setNickname(name);
                } else {
                    user.setNickname(email);
                }
                user.setThumbnail(avatar);

                users.add(user);
            }
        }

        User.upsert(users);

        MblUtils.getPrefs().edit()
                .putBoolean(PREF_RETRIEVED_ALL_CONTACTS, true)
                .commit();
    }

    public static void getMyProfileAndContacts(final SimpleCallback callback) {

        MblUtils.showProgressDialog(R.string.wait, false);

        getMyProfile(new SimpleCallback() {

            @Override
            public void onSuccess() {
                getContacts(new SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        MblUtils.hideProgressDialog();
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onError() {
                        MblUtils.hideProgressDialog();
                        if (callback != null) {
                            callback.onError();
                        }
                    }
                });
            }

            @Override
            public void onError() {
                MblUtils.hideProgressDialog();
                if (callback != null) {
                    callback.onError();
                }
            }
        });
    }

    public static void downloadImage(final String url, final DownloadImageCallback callback) {

        if (MblUtils.isEmpty(url)) {
            if (callback != null) {
                MblUtils.executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                    }
                });
            }
            return;
        }

        final String path = MblApi.getCacheFilePath(url, null);

        if (path != null) {

            if (callback != null) {
                MblUtils.executeOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(path);
                    }
                });
            }

        } else {

            runAPI(url, null, Method.GET, true, new ApiCallback() {

                @Override
                public void onSuccess(byte[] data) {
                    if (callback != null) {
                        callback.onSuccess(MblApi.getCacheFilePath(url, null));
                    }
                }

                @Override
                public void onError() {
                    if (callback != null) {
                        callback.onError();
                    }
                }
            });
        }
    }

    public static interface DownloadImageCallback {
        public void onSuccess(String path);
        public void onError();
    }

    private static void saveAuthInfo(
            String accessToken,
            String refreshToken,
            long expiresIn,
            String tokenType) {

        sAccessToken    = accessToken;
        sRefreshToken   = refreshToken;
        sExpiresAt      = System.currentTimeMillis() + 1000l * expiresIn;
        sTokenType      = tokenType;

        MblUtils.getPrefs().edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .putString(PREF_REFRESH_TOKEN, refreshToken)
                .putLong(PREF_EXPIRES_AT, expiresIn)
                .putString(PREF_TOKEN_TYPE, tokenType)
                .putBoolean(PREF_RETRIEVED_ACCESS_TOKEN, true)
                .commit();
    }

    public static void clearAuthInfo() {
        MblUtils.getPrefs().edit()
                .remove(PREF_ACCESS_TOKEN)
                .remove(PREF_REFRESH_TOKEN)
                .remove(PREF_EXPIRES_AT)
                .remove(PREF_TOKEN_TYPE)
                .remove(PREF_RETRIEVED_ACCESS_TOKEN)
                .remove(PREF_RETRIEVED_MY_PROFILE)
                .remove(PREF_RETRIEVED_ALL_CONTACTS)
                .commit();

        sAccessToken    = null;
        sRefreshToken   = null;
        sExpiresAt      = 0;
        sTokenType      = null;

        sCurrentUserId  = null;
    }

    public static boolean isAuthenticated() {
        return
                MblUtils.getPrefs().getBoolean(PREF_RETRIEVED_ACCESS_TOKEN, false) &&
                        MblUtils.getPrefs().getBoolean(PREF_RETRIEVED_MY_PROFILE,   false) &&
                        MblUtils.getPrefs().getBoolean(PREF_RETRIEVED_ALL_CONTACTS, false);
    }

    public static String getCurrentUserId() {
        if (sCurrentUserId == null) {
            User me = User.getMe();
            if (me != null) {
                sCurrentUserId = me.getId();
            }
        }
        return sCurrentUserId;
    }

    public static String getAccessToken() {
        return sAccessToken;
    }
}