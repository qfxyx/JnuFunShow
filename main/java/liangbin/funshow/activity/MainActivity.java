package liangbin.funshow.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import liangbin.funshow.R;

import liangbin.funshow.manage.FSAppInstallation;
import liangbin.funshow.manage.FunShowDatabaseHelper;
import liangbin.funshow.manage.LinksData;
import liangbin.funshow.manage.PreferencesHelper;
import liangbin.funshow.manage.ViewPageAdapter;



public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private FunShowDatabaseHelper databaseHelper;
    private ViewPager mViewpager;
    private static ViewPageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //初始化Bmob后台SDK
        Bmob.initialize(this, LinksData.bmobAppKey);
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation(this).save();
        // 启动推送服务
        BmobPush.startWork(this, LinksData.bmobAppKey);

        sendDeviceMessage();


        //开始要先创建viewpager和adapter对象，并且要用viewpager的
        //setadapter初始化，否则会发生空指针异常

        mAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewpager = (ViewPager) findViewById(R.id.viewPager);
        mViewpager.setAdapter(mAdapter);

        final ActionBar actionBar = getActionBar();
        // actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setDisplayUseLogoEnabled(false);
        //actionBar.setDisplayShowHomeEnabled(false);
        // 设置ActionBar的导航方式：Tab导航
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // 依次添加三个Tab页，并为三个Tab标签添加事件监听器
        ActionBar.Tab tabFunshow = actionBar.newTab();
        tabFunshow.setText("FunShow");
        tabFunshow.setTabListener(this);
        actionBar.addTab(tabFunshow);

        ActionBar.Tab tabMessage = actionBar.newTab();
        tabMessage.setText("信息港湾");
        tabMessage.setTabListener(this);
        actionBar.addTab(tabMessage);

        ActionBar.Tab Discovery = actionBar.newTab();
        Discovery.setText("发现");
        Discovery.setTabListener(this);
        actionBar.addTab(Discovery);
        setUpViewPager();

        //初始化数据库
        initDatabase();

    }

    private void setUpViewPager() {

        mViewpager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final ActionBar actionBar = getActionBar();
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        //TODO
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        //TODO
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        //TODO
                        break;
                    default:
                        //TODO
                        break;
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewpager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.back_key) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    private void initDatabase() {
        databaseHelper = new FunShowDatabaseHelper(this, "FunShow.db", null, 2);
        databaseHelper.getWritableDatabase();
    }

    private void sendDeviceMessage() {

        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         final PreferencesHelper sharedPreferences
                = new PreferencesHelper(this,PreferencesHelper.bmobInfo);
        boolean isFirst = sharedPreferences.getBoolean("install_is_first", true);
        String modle = Build.MODEL;
        String version = Build.VERSION.RELEASE;
        String manufactureru = Build.MANUFACTURER;
        TelephonyManager telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);

        String phoneId = telephonyManager.getDeviceId();
        String phoneNum = telephonyManager.getLine1Number();

        //Toast.makeText(this,phoneId,Toast.LENGTH_LONG).show();
        final FSAppInstallation installation = new FSAppInstallation();
        if (isFirst) {
            installation.setDeviceName(modle);
            installation.setDeviceVersion(version);
            installation.setDevicesManu(manufactureru);
            installation.setDeviceId(phoneId);
            installation.setPhoneNum(phoneNum);
            installation.setUseTimes(sharedPreferences.getInt("fs_app_use_time", 1));
            installation.save(this, new SaveListener() {
                @Override
                public void onSuccess() {

                    String id = installation.getObjectId();
                    sharedPreferences.setString("FS_DeviceId", id);
                    sharedPreferences.setBoolean("install_is_first", false);
                    sharedPreferences.setInt("fs_app_use_time", 1);
                    // Toast.makeText(MainActivity.this,"成功",Toast.LENGTH_LONG).show();

                }

                @Override
                public void onFailure(int i, String s) {
                    //Toast.makeText(MainActivity.this,"失败",Toast.LENGTH_LONG).show();

                }
            });

        } else {

            installation.setDeviceVersion(version);
            installation.setDeviceId(phoneId);
            installation.setPhoneNum(phoneNum);
            installation.setUseTimes(sharedPreferences.getInt("fs_app_use_time", 1) + 1);
            installation.update(this, sharedPreferences.getString("FS_DeviceId", ""),
                    new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            sharedPreferences.setInt("fs_app_use_time",
                                    sharedPreferences.getInt("fs_app_use_time", 1) + 1);

                        }

                        @Override
                        public void onFailure(int i, String s) {
                            sharedPreferences.setInt("fs_app_use_time",
                                    sharedPreferences.getInt("fs_app_use_time", 1) + 1);

                        }
                    });

        }


    }
}