package liangbin.funshow.manage;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/8/25.
 */
public class ActivityTilte extends LinearLayout {
    Button button;
    public ActivityTilte(Context context,AttributeSet attributeSet){
        super(context,attributeSet);
        LayoutInflater.from(context).inflate(R.layout.activity_title_layout,this);
        button=(Button)findViewById(R.id.activity_title_button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });
    }

    /*@Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.activity_title_button:
                ((Activity)MyApplication.getContext()).finish();
                break;
            default:
                break;
        }

    }*/
}
