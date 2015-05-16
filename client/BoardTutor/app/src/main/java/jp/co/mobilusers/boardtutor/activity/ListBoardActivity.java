package jp.co.mobilusers.boardtutor.activity;

import android.app.Activity;
import android.widget.ListView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import jp.co.mobilusers.boardtutor.R;

/**
 * Created by huytran on 5/16/15.
 */
@EActivity(R.layout.list_board_activity)
public class ListBoardActivity extends Activity {

    @ViewById(R.id.boardList)
    ListView boardList;


}
