package liangbin.funshow.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.URL;

import liangbin.funshow.R;

public class MessageActivity extends Activity  {

    private Button showButton;
    private ImageView checkPic;
    Bitmap bitmap;
    public static final int SHOWPICs =1;

    //import Handler类包的时候，要十分主意引入的是android.os.hand包，否则会引起错误
    //该handler属于非静态内部类，有可能引起内存泄漏
   private  Handler handler = new Handler(){
        @Override
    public void handleMessage(Message message){
            if(message.what==SHOWPICs){
                checkPic.setImageBitmap(bitmap);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity_layout);
        showButton = (Button)findViewById(R.id.button_show_img);
        checkPic =(ImageView)findViewById(R.id.check_image);
        GridView gridView;
        gridView=(GridView)findViewById(R.id.message_gridView1);

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run(){
                        try{
                            URL url = new URL("http://202.116.0.176/ValidateCode.aspx");
                            //打开URL对应的资源流
                            InputStream inputStream=url.openStream();
                            //从InputStream解析出图片
                            bitmap= BitmapFactory.decodeStream(inputStream);
                            //发送消息，通知UI组件显示该图片
                            handler.sendEmptyMessage(SHOWPICs);
                            inputStream.close();
                            //再次打开对应的资源的输入流,下载到本地
                            //inputStream=url.openStream();


                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });
    }


}
