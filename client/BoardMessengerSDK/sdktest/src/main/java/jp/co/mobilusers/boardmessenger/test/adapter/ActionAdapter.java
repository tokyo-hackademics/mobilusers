package jp.co.mobilusers.boardmessenger.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.datdo.mobilib.base.MblBaseAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.mobilusers.boardmessenger.model.Action;
import jp.co.mobilusers.boardmessenger.test.R;

public class ActionAdapter extends MblBaseAdapter<Action> {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view;
        Action action = (Action) getItem(i);

        if (convertView == null) {
            view = getLayoutInflater().inflate(R.layout.cell_action, null);
        } else {
            view = convertView;
        }

        ((TextView)view.findViewById(R.id.type_text)).setText("Type: " + action.getType());
        ((TextView)view.findViewById(R.id.data_text)).setText("Data: " + action.getData());
        ((TextView)view.findViewById(R.id.from_to_text)).setText(
                "From-duration: "
                + FORMATTER.format(new Date(action.getFrom()))
                + " - "
                + action.getDuration());
        ((TextView)view.findViewById(R.id.sender_text)).setText("Sender: " + action.getSender());

        return view;
    }

    public void append(final Action action) {
        changeDataSafely(new Runnable() {
            @Override
            public void run() {
                getData().add(action);
                notifyDataSetChanged();
            }
        });
    }
}
