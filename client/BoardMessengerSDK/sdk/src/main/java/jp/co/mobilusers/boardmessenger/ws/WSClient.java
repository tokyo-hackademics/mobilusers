package jp.co.mobilusers.boardmessenger.ws;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.User;

public class WSClient {

    private static final String TAG = WSClient.class.getSimpleName();
    private static final String SEPARATOR = "#";

    public interface Listener {
        public void onConnect();
        public void onDisconnect();
        public void onNewBoard(Board board);
        public void onBoardInfoChanged(Board board);
        public void onNewAction(Action action);
    }

    private Socket          mSocket;
    private Handler         mHandler;
    private Listener        mListener;
    private AtomicInteger   mSeq;
    private String          mUUID;
    private boolean         mConnecting;

    public WSClient(String uri, Handler handler, Listener listener) throws URISyntaxException {
        mSocket     = IO.socket(uri);
        mHandler    = handler;
        mListener   = listener;

        mSeq        = new AtomicInteger();
        mUUID       = UUID.randomUUID().toString();
    }

    public void connect() {

        if (mSocket.connected()) {
            return;
        }

        if (mConnecting) {
            return;
        }

        mConnecting = true;

        // remove all listeners
        mSocket.off();

        // listen for "on_new_board"
        mSocket.on("on_new_board", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Board board = Board.fromJSONObject((JSONObject) args[0]);
                        mListener.onNewBoard(board);
                    }
                });
            }
        });

        // listen for "on_board_info_changed"
        mSocket.on("on_board_info_changed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });

        // listen for "on_new_action"
        mSocket.on("on_new_action", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Action action = Action.fromJSONObject((JSONObject)args[0]);
                        mListener.onNewAction(action);
                    }
                });
            }
        });

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mConnecting = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onConnect();
                    }
                });
            }
        });

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mConnecting = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onDisconnect();
                    }
                });
            }
        });

        // connect
        mSocket.connect();
    }

    public void disconnect() {
        if (!mSocket.connected()) {
            return;
        }
        mSocket.disconnect();
        mSocket.off();
    }

    private void send(String tag, JSONObject data, final Callback callback) throws JSONException {
        int seq = mSeq.incrementAndGet();
        String requestId = mUUID + SEPARATOR + seq;
        JSONObject obj = new JSONObject();
        obj.put("rid", requestId);
        obj.put("data", data);
        mSocket.once(requestId, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = (JSONObject) args[0];
                        int error = obj.optInt("error");
                        if (error == 0) {
                            callback.invoke(obj.opt("result"));
                        } else {
                            callback.onError(error);
                        }
                    }
                });
            }
        });
        mSocket.emit(tag, obj);
    }

    private static abstract class Callback {
        protected abstract void invoke(Object result);
        public abstract void onError(int error);
    }

    private static abstract class CallbackWithId extends Callback {
        public abstract void onSuccess(String id);
    }

    private static abstract class SimpleCallback extends Callback {
        public abstract void onSuccess();
    }


    public void login(
            String userId,
            String userPassword,
            String accessToken,
            LoginCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("user_id", userId);
            if (accessToken != null) {
                data.put("access_token", accessToken);
            }
            if (userPassword != null) {
                data.put("user_password", userPassword);
            }
            send("login", data, callback);
        } catch (Throwable e)  {
            Log.e(TAG, "", e);
        }
    }

    public static abstract class LoginCallback extends Callback {

        public abstract void onSuccess(String accessToken);

        @Override
        protected void invoke(Object result) {
            String accessToken = ((JSONObject)result).optString("access_token");
            onSuccess(accessToken);
        }
    }

    public void createBoard(
            String[] members,
            String name,
            String background,
            int width,
            int height,
            String extra,
            CreateBoardCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("members", TextUtils.join(",", members));
            data.put("name", name);
            data.put("background", background);
            data.put("width", width);
            data.put("height", height);
            data.put("extra", extra);
            send("create_board", data, callback);
        } catch (Throwable e) {
            Log.e(TAG, "", e);
        }
    }

    public static abstract class CreateBoardCallback extends CallbackWithId {
        @Override
        protected void invoke(Object result) {
            String id = ((JSONObject)result).optString("id");
            onSuccess(id);
        }
    }

    public void getBoardInfo(String[] ids, GetBoardInfoCallback callback) {
        try {
            JSONObject data = new JSONObject();
            if (ids != null && ids.length > 0) {
                data.put("ids", TextUtils.join(",", ids));
            }
            send("get_board_info", data, callback);
        } catch (Throwable e) {
            Log.e(TAG, "", e);
        }
    }

    public static abstract class GetBoardInfoCallback extends Callback {
        @Override
        protected void invoke(Object result) {
            JSONArray ja = (JSONArray) result;
            List<Board> boards = new ArrayList<Board>();
            for (int i = 0; i < ja.length(); i++) {
                boards.add(Board.fromJSONObject(ja.optJSONObject(i)));
            }
            onSuccess(boards);
        }

        public abstract void onSuccess(List<Board> boards);
    }

    public void sendAction(
            String boardId,
            String type,
            String data,
            long from,
            long duration,
            CallbackWithId callback) {
        try {
            JSONObject _data = new JSONObject();
            _data.put("board_id", boardId);
            _data.put("type", type);
            if (data != null) {
                _data.put("data", data);
            }
            _data.put("from", from);
            _data.put("duration", duration);
            send("send_action", _data, callback);
        } catch (Throwable e) {
            Log.e(TAG, "", e);
        }
    }

    public void getBoardActions(
            String boardId,
            long from,
            GetBoardActionsCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("board_id", boardId);
            data.put("from", from);
            send("get_board_actions", data, callback);
        } catch (Throwable e) {
            Log.e(TAG, "", e);
        }
    }

    public static abstract class GetBoardActionsCallback extends Callback {

        public abstract void onSuccess(List<Action> actions);

        @Override
        protected void invoke(Object result) {
            JSONArray ja = (JSONArray) result;
            List<Action> ret = new ArrayList<Action>();
            for (int i = 0; i < ja.length(); i++) {
                ret.add(Action.fromJSONObject(ja.optJSONObject(i)));
            }
            onSuccess(ret);
        }
    }
}
