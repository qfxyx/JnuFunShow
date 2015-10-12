package liangbin.funshow.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import liangbin.funshow.R;
import liangbin.funshow.manage.NetworkStatus;
import liangbin.funshow.manage.TitleListAdapter;
import liangbin.funshow.manage.TitleListView;

/**
 * Created by Administrator on 2015/8/11.
 */
public class NotificationActivity extends Activity {
    private List<TitleListView> notificationList=new ArrayList<TitleListView>();
    private TitleListAdapter adapter;
    private ListView listView;
    private final String teachingLink="http://jwc.jnu.edu.cn/SmallClass_index.asp?" +
            "SmallClassName=%CD%A8%D6%AA&BigClassName=%BD%CC%CE%F1%B4%A6";
    private final String studentsLink="http://xsc.jnu.edu.cn/list.aspx?cid=6";
    private final String comprehensiveLink="http://www.jnu.edu.cn/jnu2014/" +
            "article_list.asp?channelID=5037";
    private String Test="http://www.tudou.com/";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);
        initNotificationList();
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("校内通知");
        listView=(ListView)findViewById(R.id.notification_listView);
        adapter=new TitleListAdapter(this,R.layout.title_list_item,notificationList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NetworkStatus networkStatus=new NetworkStatus();
                if (networkStatus.canConntect()){
                    switch (position){

                        case 0:
                            Intent intent0=new Intent(NotificationActivity.this,
                                    NotificationTeachingActivity.class);
                            startActivity(intent0);
                            break;
                        case 1:
                            Intent intent1=new Intent(NotificationActivity.this,
                                    NotificationStudentActivity.class);
                            startActivity(intent1);
                            break;
                        case 2:
                            Intent intent2=new Intent(NotificationActivity.this,
                                    NotificationCampusActivity.class);;
                            startActivity(intent2);
                            break;
                        default:
                            break;
                    }

                }

            }
        });

    }
    public void initNotificationList(){
        TitleListView teachingAffair =new TitleListView("教务处",R.mipmap.notification_teaching);
        notificationList.add(teachingAffair);
        TitleListView studentsAffair = new TitleListView("学生处",R.mipmap.notification_students);
        notificationList.add(studentsAffair);
        TitleListView comprehensiveNotification =new TitleListView
                ("校内综合(请用校内ip访问)",R.mipmap.notification_comprehensive);
        notificationList.add(comprehensiveNotification);
    }
}
