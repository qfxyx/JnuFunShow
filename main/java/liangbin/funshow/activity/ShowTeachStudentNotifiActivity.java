package liangbin.funshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/10/26.
 */
public class ShowTeachStudentNotifiActivity extends WebViewActivity {
    WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Button button_forward=(Button)findViewById(R.id.webView_forward_button);
        button_forward.setVisibility(View.INVISIBLE);
        Button button_reflash=(Button)findViewById(R.id.webView_reflesh_button);
        button_reflash.setVisibility(View.INVISIBLE);
        Button shareButton=(Button)findViewById(R.id.webView_share_button);
        webView=(WebView)findViewById(R.id.fusnhow_webview);
        //获取父类WebViewActivity的Intent
        Intent intent=super.intent;
        final String title=intent.getStringExtra("notificationTitle");
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,title +"\n"+webView.getUrl() );
                startActivity(Intent.createChooser(intent, getTitle()));

            }
        });
    }

}
