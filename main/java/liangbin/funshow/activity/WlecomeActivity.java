package liangbin.funshow.activity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/10/25.
 */
public class WlecomeActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_activity_layout);

    new Thread(){
        @Override
    public void run(){
            try {
                Thread.sleep(3000);
            }catch (Exception e){
                System.out.println("WelcomeActivity has a Bug.");
            }finally {
                startActivity(new Intent(WlecomeActivity.this,MainActivity.class));
                WlecomeActivity.this.finish();
            }

        }
    }.start();
}
}
