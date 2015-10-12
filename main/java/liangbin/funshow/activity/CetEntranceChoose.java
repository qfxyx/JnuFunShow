package liangbin.funshow.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/8/24.
 */
  public class CetEntranceChoose extends Activity {
    GridView gridView;
    TextView textView;
    String tips="“身份证号+名字”查询入口数据采集自暨南大学官网，" +
            "考试成绩公布时间会稍有延迟，但可查往年四六级成绩。\n"+"查询最新公布的全国大学" +
            "英语四、六级考试、日语四级、日语六级、" +
            "德语四级、德语六级、俄语四级、俄语六级及法语四级" +
            "考试成绩"+"请选择“准考证号+名字”查询入口。";
    int[] imageIds={R.mipmap.cet_entrance_2,R.mipmap.cet_entrance_1};
    String[] entranceName={"准考证号+姓名","身份证号+姓名"};
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cet_query_entrance_layout);
        gridView=(GridView)findViewById(R.id.cet_entrance_gridview);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("查询入口选择");
        textView=(TextView)findViewById(R.id.cet_entrance_text);
        textView.setText(tips);
        List<Map<String,Object>> listItems=new ArrayList<Map<String, Object>>();
        for(int i=0;i<imageIds.length;i++){
            Map<String,Object> listItem=new HashMap<>();
            listItem.put("name",entranceName[i]);
            listItem.put("image",imageIds[i]);
            listItems.add(listItem);
        }
        SimpleAdapter adapter=new SimpleAdapter(this,listItems,R.layout.message_list_item,
               new String[]{"name","image"},new int[]{R.id.message_item_text,
                R.id.message_item_image} );
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent0=new Intent(CetEntranceChoose.this,
                                CetQueryByAdmissionNumActivity.class);
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1=new Intent(CetEntranceChoose.this,CetQueryActivity.class);
                        startActivity(intent1);
                        break;
                    default:
                        break;
                }
            }
        });

    }
}
