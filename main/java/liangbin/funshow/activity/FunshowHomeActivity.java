package liangbin.funshow.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/7/30.
 */
public class FunshowHomeActivity extends Activity{
    private WebView funshow;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.funshow_home);
        funshow=(WebView)findViewById(R.id.fusnhow_home_webView);
        funshow.getSettings().getJavaScriptEnabled();
        funshow.getSettings().setUseWideViewPort(true);
        funshow.getSettings().setLoadWithOverviewMode(true);
        funshow.getSettings().setSupportZoom(true);
        funshow.getSettings().setBuiltInZoomControls(true);
        funshow.getSettings().setDisplayZoomControls(false);
        funshow.getSettings().setDomStorageEnabled(true);
        funshow.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        funshow.loadUrl("http://bbs.jnustu.org/");
        /*
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }); */

    }
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if ((keyCode==KeyEvent.KEYCODE_BACK)&&funshow.canGoBack()){
            funshow.goBack();
            return  true;
        }
        return super.onKeyDown(keyCode,event);
    }
}
