package jp.co.mobilusers.boardmessenger;

import android.content.Context;
import android.os.Handler;

import java.util.List;

import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.model.User;

/**
 * Created by dat on 5/16/15.
 */
public class BoardMessenger {

    public static class Listener {
        public void onLoginSuccess() {}
        public void onLoginError() {}
        public void onDisconnect() {}
        public void onNewBoard(Board board) {}
        public void onNewAction(Action action) {}
    }

    private static abstract class Callback {
        public abstract void onError();
    }

    public static abstract class SimpleCallback extends Callback {
        public abstract void onSuccess();
    }

    public static BoardMessenger getInstance() {
        return null;
    }

    public static void init(Context context, String uri, Handler handler) {
    }

    public void addListener(Listener l) {
    }

    public void removeListener(Listener l) {
    }

    public void setAccount(String userId, String userPassword) {
    }

    public User getAccount() {
        return null;
    }

    public boolean needAccount() {
        return false;
    }

    public void connect() {
    }

    public void disconnect() {
    }

    public void createBoard(
            String[] members,
            String name,
            String background,
            int width,
            int height,
            String extra,
            final SimpleCallback callback) {
    }

    public List<Board> getAllBoards() {
        return null;
    }

    public Board getBoard(String id) {
        return null;
    }

    public void sendAction(
            String boardId,
            String type,
            String data,
            long from,
            long duration,
            final SimpleCallback callback) {
    }

    public void getBoardActions(final String boardId, final GetBoardActionsCallback callback) {
    }

    public static abstract class GetBoardActionsCallback extends Callback {
        public abstract void onSuccess(List<Action> actions);
    }
}
