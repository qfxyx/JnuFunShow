package liangbin.funshow.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/8/1.
 */
public class WebViewActivity extends Activity {
     private WebView webView;
     private Intent intent;
    private ValueCallback<Uri> myUploadMessage;
    private ProgressBar progressBar;
    private TextView textView;
    private final static int FILECHOOSER_RESULTCODE=1;
    private String links;
    private String what;
    private String whatMsg;
    private String setTitle;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.webview_activity_layoput);
        //显示加载进度条
        progressBar=(ProgressBar)findViewById(R.id.webview_progressBar);
        webView=(WebView)findViewById(R.id.fusnhow_webview);
        textView=(TextView)findViewById(R.id.webView_title_text);

        //根据传进来的数据在该webview中选择相应的显示和操作
        intent=getIntent();
        links = intent.getStringExtra("links");
         what = intent.getStringExtra("what");
         whatMsg=null;
        whatMsg=intent.getStringExtra("whatMsg");
        setTitle=intent.getStringExtra("title");
        textView.setText(setTitle);
        if (what.equals("showToast")){
            if (whatMsg!=null){
                Toast.makeText(this,whatMsg,Toast.LENGTH_LONG).show();

            }
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                Uri uri= Uri.parse(url);
                Intent intent= new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);

            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType,
                                        String capture){
                myUploadMessage=uploadMsg;
                Intent i=new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                WebViewActivity.this.startActivityForResult(Intent.createChooser(i
                        , "File Chooser"),WebViewActivity.FILECHOOSER_RESULTCODE);
            }
            @Override
           public void onProgressChanged(WebView webView,int newProgress){
                if (newProgress==100){
                    progressBar.setVisibility(View.INVISIBLE);//此处设置为GONE或者INVISIBLE时
                                                                //要与”1“处代码一致
                }else {
                    if (View.INVISIBLE==progressBar.getVisibility()){   //"1"处代码
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(webView,newProgress);
            }

            //尚未解决Js弹框中文乱码问题
            /*
            public boolean onJsAlert(WebView webView,String url,String message,JsResult result){
                String hh =message;
               // String messageUTF =new String(hh.getBytes(),"utf-8");
                String hhhh= null;
                try {
                    hhhh = new String(message.getBytes(),"gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Toast.makeText(WebViewActivity.this, hhhh, Toast.LENGTH_SHORT).show();
                result.confirm();
                return true;
            } */

        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(links);
        Button buttonClose=(Button)findViewById(R.id.webView_close_button);
        Button buttonBack=(Button)findViewById(R.id.webView_back_button);
        Button buttonForward=(Button)findViewById(R.id.webView_forward_button);
        Button buttonReflesh=(Button)findViewById(R.id.webView_reflesh_button);
        Button buttonShare=(Button)findViewById(R.id.webView_share_button);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()){
                    webView.goBack();
                }else {
                    finish();
                }

            }
        });
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if (webView.canGoForward()){
                   // webView.reload();
                //}
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,webView.getTitle()+"\n"+webView.getUrl() );
                startActivity(Intent.createChooser(intent, getTitle()));

            }
        });
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (webView.canGoForward()){
                webView.goForward();
                }


            }
        });
        buttonReflesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              webView.reload();


            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        if (requestCode==FILECHOOSER_RESULTCODE){
            if (myUploadMessage==null) return;
            Uri result = intent==null||resultCode!=RESULT_OK ? null:intent.getData();
            myUploadMessage.onReceiveValue(result);
            myUploadMessage=null;

        }
    }
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if ((keyCode==KeyEvent.KEYCODE_BACK)&&webView.canGoBack()){
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

}
