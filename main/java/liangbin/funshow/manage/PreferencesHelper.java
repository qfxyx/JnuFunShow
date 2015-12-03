package liangbin.funshow.manage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015/10/29.
 */
public class PreferencesHelper {

    public static final String bmobInfo="FsBmobInfo";
    public static final String class_schedule="FsClassInfo";
    public static final String netInfo="FsNetworkInfo";

    private  Context context=null;
    private  String name="";
    public SharedPreferences sharedPreferences=null;

    public PreferencesHelper(Context context,String name){
        this.context=context;
        this.name=name;
        sharedPreferences=getSharedPreferences();


    }
    private SharedPreferences getSharedPreferences(){
        SharedPreferences preferences=context.getSharedPreferences(name,Context.MODE_PRIVATE);
        return preferences;
    }
     public boolean setString (String key,String val){
         return sharedPreferences.edit().putString(key,val).commit();
     }
    public String getString(String key,String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }
    public boolean setBoolean(String key, boolean val){
        return sharedPreferences.edit().putBoolean(key, val).commit();
    }

    public boolean getBoolean(String key, boolean defaultValue){
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    public boolean setInt(String key, int val){
        return sharedPreferences.edit().putInt(key, val).commit();
    }

    public int getInt(String key, int defaultValue){
        return sharedPreferences.getInt(key, defaultValue);
    }
    public void clearData(){
        //Remember to use the commit() or apply() to commit your data or it will not clear it
        // sharedPreferences.edit().clear().apply();
         sharedPreferences.edit().clear().commit();
    }
}
