package liangbin.funshow.manage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import liangbin.funshow.activity.DiscoveryFragment;
import liangbin.funshow.activity.FunshowFragment;
import liangbin.funshow.activity.MessageFragment;


/**
 * Created by Administrator on 2015/7/28.
 */
public class ViewPageAdapter extends FragmentPagerAdapter {

     public static final int Funshow_Index = 0;
     public static final int Message_Index = 1;
     public static final int JustFun_Index = 2;
    public ViewPageAdapter(FragmentManager fragmentManager){

        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int Index){
        Fragment mfragment = null;
        switch(Index){
            case Funshow_Index:
                mfragment=new FunshowFragment();
                break;
            case Message_Index:
                mfragment=new MessageFragment();
                break;

            case  JustFun_Index:
                mfragment = new DiscoveryFragment();
                break;
        }
        return mfragment;
    }
    @Override
    public int getCount(){
        return 3;
    }
}
