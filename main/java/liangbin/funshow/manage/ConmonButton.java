package liangbin.funshow.manage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import liangbin.funshow.R;
import liangbin.funshow.activity.MessageActivity;

/**
 * Created by Administrator on 2015/7/21.
 */
public class ConmonButton extends LinearLayout implements View.OnClickListener{
    private Button closeButton;
    public ConmonButton (Context context,AttributeSet attrs){
        super(context,  attrs);
        LayoutInflater.from(context).inflate(R.layout.common_button_layout,this);
        closeButton=(Button)findViewById(R.id.close_Button);
        closeButton.setOnClickListener(this);






    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.close_Button:

          /*  Context中有一个startActivity方法，Activity继承自Context，
          重载了startActivity方法。如果使用 Activity的startActivity方法，
          不会有任何限制，而如果使用Context的startActivity方法的话，
          就需要开启一个新的task，解决办法是，加一个flag。
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Intent intent =new Intent(MyApplication.getContext(),MessageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApplication.getContext().startActivity(intent);  */
                ((Activity)getContext()).finish();

                break;
            default:
                break;
        }

    }


}
