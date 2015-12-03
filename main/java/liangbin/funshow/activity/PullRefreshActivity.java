package liangbin.funshow.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import liangbin.funshow.R;
import liangbin.funshow.manage.MyApplication;
import liangbin.funshow.manage.PullToRefleshLayout;

/**
 * Created by Administrator on 2015/11/9.
 */
public class PullRefreshActivity extends Activity {
    PullToRefleshLayout pullToRefleshLayout;
    ListView listView;
    ArrayAdapter<String> adapter;
    String[] items={ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pull_test);
        pullToRefleshLayout=(PullToRefleshLayout)findViewById(R.id.refreshable_view);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        pullToRefleshLayout.setOnRefreshListener(new PullToRefleshLayout.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pullToRefleshLayout.finishRefreshing();

            }
        });
    }
}
