package liangbin.funshow.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/9/24.
 */
public class ShowPushTextActivity extends Activity {
    TextView contentTitle;
    TextView contentText;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_push_text_activity);
        contentTitle=(TextView)findViewById(R.id.show_push_text_title);
        contentText=(TextView)findViewById(R.id.show_push_text_content);
        TextView activityTitle=(TextView)findViewById(R.id.activity_title_text);
        Intent intent=getIntent();
        String getActTitle=intent.getStringExtra("title");
        String getContent=intent.getStringExtra("content");
        String getContentText=intent.getStringExtra("contentText");
        activityTitle.setText(getActTitle);
        contentTitle.setText(getContent);
        contentText.setText(getContentText);


    }
}
