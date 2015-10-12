package liangbin.funshow.manage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/10/13.
 */
public  class NetworkStatus {

      Context context=MyApplication.getContext();
      ConnectivityManager connectivityManager =(ConnectivityManager)context.
            getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();

    private final  String noNetworkTips="网络链接不可用，请稍后再试！";

    public NetworkStatus(){
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
            Toast.makeText(context,noNetworkTips,Toast.LENGTH_SHORT).show();
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
