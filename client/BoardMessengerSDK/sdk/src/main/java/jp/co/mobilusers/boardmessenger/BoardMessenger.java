package jp.co.mobilusers.boardmessenger;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jp.co.mobilusers.boardmessenger.model.Board;
import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.model.User;
import jp.co.mobilusers.boardmessenger.ws.WSClient;


public class BoardMessenger {

    private static final String TAG = BoardMessenger.class.getSimpleName();

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

    private static BoardMessenger sInstance;
    private WSClient mWSClient;
    private Context mApplicationContext;
    private Handler mHandler;
    private Set<Listener> mListeners = Collections.synchronizedSet(new HashSet<Listener>());

    private BoardMessenger(Context context, String uri, Handler handler) {

        mApplicationContext = context.getApplicationContext();
        if (handler != null) {
            mHandler = handler;
        } else {
            mHandler = new Handler(Looper.getMainLooper());
        }

        HandlerThread wsClienThread = new HandlerThread(TAG);
        wsClienThread.start();
        Handler wsClientHandler = new Handler(wsClienThread.getLooper());

        try {
            mWSClient = new WSClient(uri, wsClientHandler, new WSClient.Listener() {
                @Override
                public void onConnect() {
                    final User user = User.get(mApplicationContext);
                    if (user == null) {
                        disconnect();
                        return;
                    }
                    mWSClient.login(
                            user.getUserId(),
                            user.getUserPassword(),
                            user.getAccessToken(),
                            new WSClient.LoginCallback() {
                                @Override
                                public void onSuccess(String accessToken) {
                                    user.setAccessToken(accessToken);
                                    User.upsert(mApplicationContext, user);

                                    // get all rooms
                                    mWSClient.getBoardInfo(null, new WSClient.GetBoardInfoCallback() {
                                        @Override
                                        public void onSuccess(List<Board> boards) {
                                            Board.upsert(mApplicationContext, boards);
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (Listener l : mListeners) {
                                                        l.onLoginSuccess();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(int error) {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (Listener l : mListeners) {
                                                        l.onLoginError();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }

                                @Override
                                public void onError(int error) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (Listener l : mListeners) {
                                                l.onLoginError();
                                            }
                                        }
                                    });
                                }
                            });
                }

                @Override
                public void onDisconnect() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (Listener l : mListeners) {
                                l.onDisconnect();
                            }
                        }
                    });
                }

                @Override
                public void onNewBoard(final Board board) {
                    Board.upsert(mApplicationContext, board);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (Listener l : mListeners) {
                                l.onNewBoard(board);
                            }
                        }
                    });
                }

                @Override
                public void onBoardInfoChanged(Board board) {

                }

                @Override
                public void onNewAction(final Action action) {
                    Action.upsert(mApplicationContext, action);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (Listener l : mListeners) {
                                l.onNewAction(action);
                            }
                        }
                    });
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "", e);
        }
    }

    public static BoardMessenger getInstance() {
        return sInstance;
    }

    public static void init(Context context, String uri, Handler handler) {
        sInstance = new BoardMessenger(context, uri, handler);
    }

    public void addListener(Listener l) {
        mListeners.add(l);
    }

    public void removeListener(Listener l) {
        mListeners.remove(l);
    }

    public void setAccount(String userId, String userPassword) {
        User user = User.get(mApplicationContext);
        if (user == null) {
            user = new User();
        }
        user.setUserId(userId);
        user.setUserPassword(userPassword);
        User.upsert(mApplicationContext, user);
    }

    public User getAccount() {
        return User.get(mApplicationContext);
    }

    public boolean needAccount() {
        User user = User.get(mApplicationContext);
        return user == null || user.getUserId() == null || user.getAccessToken() == null;
    }

    public void connect() {
        mWSClient.connect();
    }

    public void disconnect() {
        mWSClient.disconnect();
    }

    public void createBoard(
            String[] members,
            String name,
            String background,
            int width,
            int height,
            String extra,
            final SimpleCallback callback) {

        mWSClient.createBoard(members, name, background, width, height, extra, new WSClient.CreateBoardCallback() {
            @Override
            public void onSuccess(String id) {
                if (callback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                }
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError();
                        }
                    });
                }
            }
        });
    }

    public List<Board> getAllBoards() {
        return Board.getAll(mApplicationContext);
    }

    public Board getBoard(String id) {
        return Board.get(mApplicationContext, id);
    }

    public void sendAction(
            String boardId,
            String type,
            String data,
            long from,
            long duration,
            final SimpleCallback callback) {

        mWSClient.sendAction(boardId, type, data, from, duration, new WSClient.CreateBoardCallback() {
            @Override
            public void onSuccess(String id) {
                if (callback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                }
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError();
                        }
                    });
                }
            }
        });
    }

    public void getBoardActions(final String boardId, final GetBoardActionsCallback callback) {

        Action latestAction = Action.getLatestActionOfBoard(mApplicationContext, boardId);
        long from = latestAction != null ? latestAction.getFrom() : -1;
        mWSClient.getBoardActions(boardId, from, new WSClient.GetBoardActionsCallback() {
            @Override
            public void onSuccess(final List<Action> actions) {
                Action.upsert(mApplicationContext, actions);
                if (callback != null) {
                    final List<Action> allActions = Action.getAllOfBoard(mApplicationContext, boardId);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(allActions);
                        }
                    });
                }
            }

            @Override
            public void onError(int error) {
                if (callback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError();
                        }
                    });
                }
            }
        });
    }

    public static abstract class GetBoardActionsCallback extends Callback {
        public abstract void onSuccess(List<Action> actions);
    }
}
