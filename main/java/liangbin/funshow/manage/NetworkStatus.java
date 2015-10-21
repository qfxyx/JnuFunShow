package liangbin.funshow.manage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import liangbin.funshow.activity.ClassScheduleActivity;
import liangbin.funshow.activity.MainActivity;

/**
 * Created by Administrator on 2015/10/13.
 */
public  class NetworkStatus {

      Context context;
      ConnectivityManager connectivityManager =(ConnectivityManager)MyApplication.getContext().
            getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();

    private final  String noNetworkTips="当前网络链接不可用，是否打开网络设置？";

    public NetworkStatus(Activity activity){
        this.context=activity;
    }

    public boolean isOK(){
        if (networkInfo!=null){
            boolean connect=networkInfo.isAvailable();
            return connect;
        }else {
            return false ;
        }

    }
    public boolean canConntect(){
        if (isOK()){
            return true;
        }else{
           // Toast.makeText(context,noNetworkTips,Toast.LENGTH_SHORT).show();
            AlertDialog builder=new AlertDialog.Builder(context).setTitle("连接失败").
                    setMessage(noNetworkTips).setPositiveButton("设置网络", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent=null;
                    if (Build.VERSION.SDK_INT>10){
                        intent=new Intent(Settings.ACTION_SETTINGS);
                    }else {
                        intent=new Intent();
                        intent.setClassName("com.android.settings","com.android.settings.WirelessSettings");
                    }
                    context.startActivity(intent);

                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create();
            builder.show();
            return false;
        }
    }

    public String getNetType(){
        if (isOK()){
            String type = networkInfo.getTypeName().toLowerCase();
            return type;
        }else {
            return " The Network is not Connected !";

        }

    }



}
