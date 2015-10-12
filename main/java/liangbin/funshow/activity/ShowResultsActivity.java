package liangbin.funshow.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/8/19.
 */
public class ShowResultsActivity extends Activity {

    TextView titleTextView;
    TextView resultTextView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.show_result_activity);
        Intent intent=getIntent();
        String title=intent.getStringExtra("title");
        String showText=intent.getStringExtra("result");
        titleTextView=(TextView)findViewById(R.id.activity_title_text);
        resultTextView=(TextView)findViewById(R.id.show_results_text);
        titleTextView.setText(title);
        resultTextView.setText(showText);

    }
}
