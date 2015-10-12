package liangbin.funshow.activity;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liangbin.funshow.R;
import liangbin.funshow.manage.NetworkStatus;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends android.support.v4.app.Fragment {
    private String[] names;
    private int[] images;
    private GridView gridView;
    private SimpleAdapter simpleAdapter;
    List<Map<String,Object>> listItems;

    private final String repairWebsite="http://zhzw.jnu.edu.cn/zhzw/website/repair/" +
            "index.php?openid=of_zzt1lz_r4E71dG4xULBCtt6IY";
    private final String jnuNews="http://news.jnu.edu.cn/";


    public MessageFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        images=new int[]{R.mipmap.jnu_notification,R.mipmap.jnu_news,
                R.mipmap.jnu_cet_query,R.mipmap.jnu_class_exams,R.mipmap.jnu_finish_scores,
                R.mipmap.jnu_network,R.mipmap.jnu_library_service,R.mipmap.jnu_scores_query,
                R.mipmap.ic_launcher};
        names=new String[]{"校内通知","暨南要闻",
                "四六级查询","课程表和考试表","学分查询","校园网助手","图书馆服务","成绩查询",
                "Test"};
        listItems=new ArrayList<Map<String, Object>>();
        for (int i=0;i<names.length;i++){
            Map<String,Object> listItem =new HashMap<String, Object>();
            listItem.put("images",images[i]);
            listItem.put("names",names[i]);
            listItems.add(listItem);
            simpleAdapter=new SimpleAdapter(getActivity(),listItems,R.layout.message_list_item,
                    new String[]{"images","names"},new int[]{R.id.message_item_image,
            R.id.message_item_text});
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_message, container, false);
        gridView=(GridView)view.findViewById(R.id.message_gridView);
        gridView.setAdapter(simpleAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent0=new Intent(getActivity(),NotificationActivity.class);
                        startActivity(intent0);
                        break;
                    case 1:
                        NetworkStatus networkStatus=new NetworkStatus();
                        if (networkStatus.canConntect()){
                            Intent intent1=new Intent(getActivity(),WebViewActivity.class);
                            intent1.putExtra("links",jnuNews);
                            intent1.putExtra("what","showToast");
                            intent1.putExtra("title","暨南要闻");
                            intent1.putExtra("whatMsg","本页面内容来自暨大新闻网，详情请访问其官网");
                            startActivity(intent1);
                        }

                        break;
                    case 2:
                        Intent intent = new Intent(getActivity(),CetEntranceChoose.class);
                        startActivity(intent);
                        break;
                    case 3:
                        Intent intent3=new Intent(getActivity(),ClassScheduleActivity.class);
                        startActivity(intent3);
                        break;
                    case 4:
                        Intent intent4 =new Intent(getActivity(),ScoreQueryActivity.class);
                        startActivity(intent4);
                        break;

                    case 5:
                        Intent intent5=new Intent(getActivity(),CampusNetActivity.class);
                        startActivity(intent5);
                        break;

                    case 6:
                        Intent intent6=new Intent(getActivity(),LibraryQueryActivity.class);
                        startActivity(intent6);
                        break;
                    case 7:
                        Intent intent7=new Intent(getActivity(),GradesActivity.class);
                        startActivity(intent7);
                        break;
                    case 8:
                        Intent intent8=new Intent(getActivity(),TestActivity.class);
                        startActivity(intent8);
                        break;
                    default:
                        break;
                }
            }
        });



        return view;
    }


}
