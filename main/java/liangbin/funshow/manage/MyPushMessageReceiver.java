package liangbin.funshow.manage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;
import liangbin.funshow.R;
import liangbin.funshow.activity.ShowPushTextActivity;
import liangbin.funshow.activity.WebViewActivity;

/**
 * Created by Administrator on 2015/9/23.
 */
public class MyPushMessageReceiver extends BroadcastReceiver{
    //notification
    NotificationManager nm;
    @Override
    public void onReceive(Context context,Intent intent){

         nm=(NotificationManager)context.//要先获取Context
                getSystemService(Context.NOTIFICATION_SERVICE);
        //获取服务器推送的Json格式消息
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            String pushData=intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            //Toast.makeText(MyApplication.getContext(),pushData,Toast.LENGTH_LONG).show();
            try{

                    JSONObject jsonObject=new JSONObject(pushData);
                    String title =jsonObject.getString("title");
                    String content=jsonObject.getString("content");
                    String link=jsonObject.getString("link");
                    String contentText=jsonObject.getString("contentText");
               // Intent intentSend=new Intent(context, ShowPushTextActivity.class);
                //intentSend.putExtra("content",content);
                //intentSend.putExtra("title",title);
                //intentSend.putExtra("contentText",contentText);
                Intent intentSend;
                if (link.equals("no")){
                    intentSend=new Intent(context, ShowPushTextActivity.class);
                    intentSend.putExtra("content",content);
                    intentSend.putExtra("title",title);
                    intentSend.putExtra("contentText",contentText);

                }else {
                    intentSend=new Intent(context, WebViewActivity.class);
                    intentSend.putExtra("title",title);
                    intentSend.putExtra("links",link);
                    intentSend.putExtra("what","");
                    intentSend.putExtra("whatMsg","");
                }





                PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intentSend,
                        PendingIntent.FLAG_UPDATE_CURRENT);//最后一个参数表示更新intent的数据到
                        // pendingIntent中

                    //Toast.makeText(MyApplication.getContext(),title+content+link,Toast.LENGTH_LONG).show();
                Notification  notification=new Notification.Builder(context).setAutoCancel(true)
                        .setTicker("Funshow有新消息啦！").setSmallIcon(R.mipmap.funshow_logo)
                        .setContentTitle(title).setContentText(content).setContentIntent
                                (pendingIntent).build();
                nm.notify(123,notification);


            }catch (JSONException e){
                e.printStackTrace();
            }


        }

    }

}
