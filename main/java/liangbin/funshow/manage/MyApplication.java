package liangbin.funshow.manage;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2015/7/24.
 */
/**
 * 编写自己的Application，管理全局状态信息，比如Context
 * @author yy
 *
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        //获取Context
        super.onCreate();
        context = this;
    }

    //返回
    public static Context getContext(){
        return context;
    }
}

