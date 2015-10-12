package liangbin.funshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/8/26.
 */
public class DiscoveryFragment extends Fragment {

    String[] names={"精彩FunShow","JNU表白墙","校车时刻表","各校区地图","游戏天地"};

    int[]images={R.mipmap.discovery_fs_uodate,R.mipmap.discovery_message_board,
            R.mipmap.discovery_school_bus, R.mipmap.discovery_campus_map,
            R.mipmap.discovery_games};
    int ForwardPic=R.mipmap.turn_right;
    String JnuBoard="http://1.funshow.sinaapp.com/messageboard/w/";
    String busTimatable="http://1.funshow.sinaapp.com/bus/";
    String games="http://1.funshow.sinaapp.com/game/index.php";
    String funshowHis=" http://1.funshow.sinaapp.com/history/index.html";
   // SimpleAdapter simpleAdapter;
    List<Map<String,Object>> listItems1;
    List<Map<String,Object>> listItems2;
    List<Map<String,Object>> listItems3;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        listItems1=new ArrayList<Map<String, Object>>();
        for (int i=0;i<2;i++){
            Map<String,Object> listItem1=new HashMap<String, Object>();
            listItem1.put("image1",images[i]);
            listItem1.put("name",names[i]);
            listItem1.put("iamge2",ForwardPic);
            listItems1.add(listItem1);
        }
        listItems2=new ArrayList<Map<String, Object>>();
        for (int i=2;i<4;i++){
            Map<String,Object> listItem2=new HashMap<String, Object>();
            listItem2.put("image1",images[i]);
            listItem2.put("name",names[i]);
            listItem2.put("iamge2",ForwardPic);
            listItems2.add(listItem2);
        }
        listItems3=new ArrayList<Map<String, Object>>();
        Map<String,Object> listItem3=new HashMap<String, Object>();
        listItem3.put("image1",images[4]);
        listItem3.put("name",names[4]);
        listItem3.put("iamge2",ForwardPic);
        listItems3.add(listItem3);
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.discovery_fragment, container, false);

        ListView listView1=(ListView)view.findViewById(R.id.discovery_listView_1);
        listView1.setAdapter(new SimpleAdapter(getActivity(),listItems1,
                R.layout.three_items_listview,new String[]{"image1","name","iamge2"},
                new int[]{R.id.discovery_listView_image,R.id.discovery_listView_title,
                R.id.discovery_listView_forward}));
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:

                        Intent intent0=new Intent(getActivity(),WebViewActivity.class);
                        intent0.putExtra("links",funshowHis);
                        intent0.putExtra("what","");
                        intent0.putExtra("title","精彩Funhow");
                        intent0.putExtra("whatMsg","");
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1=new Intent(getActivity(),WebViewActivity.class);
                        intent1.putExtra("links",JnuBoard);
                        intent1.putExtra("what","");
                        intent1.putExtra("title","请大胆表达吧");
                        intent1.putExtra("whatMsg","");
                        startActivity(intent1);

                        break;
                    default:
                        break;
                }
            }
        });

        ListView listView2=(ListView)view.findViewById(R.id.discovery_listView_2);
        listView2.setAdapter(new SimpleAdapter(getActivity(),listItems2,
                R.layout.three_items_listview,new String[]{"image1","name","iamge2"},
                new int[]{R.id.discovery_listView_image,R.id.discovery_listView_title,
                        R.id.discovery_listView_forward}));
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent0=new Intent(getActivity(),WebViewActivity.class);
                        intent0.putExtra("links",busTimatable);
                        intent0.putExtra("what","");
                        intent0.putExtra("title","校车时刻表");
                        intent0.putExtra("whatMsg","");
                        startActivity(intent0);

                        break;
                    case 1:

                        Toast.makeText(getActivity(),"This page is still on developing..."
                                ,Toast.LENGTH_SHORT).show();

                        break;
                    default:
                        break;
                }
            }
        });


        ListView listView3=(ListView)view.findViewById(R.id.discovery_listView_3);
        listView3.setAdapter(new SimpleAdapter(getActivity(),listItems3,
                R.layout.three_items_listview,new String[]{"image1","name","iamge2"},
                new int[]{R.id.discovery_listView_image,R.id.discovery_listView_title,
                        R.id.discovery_listView_forward}));
        listView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent0=new Intent(getActivity(),WebViewActivity.class);
                        intent0.putExtra("links",games);
                        intent0.putExtra("what","");
                        intent0.putExtra("title","来玩个小游戏吧");
                        intent0.putExtra("whatMsg","");
                        startActivity(intent0);

                        break;
                    default:
                        break;
                }
            }
        });
        return view;

    }
}
