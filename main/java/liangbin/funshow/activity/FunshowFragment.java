package liangbin.funshow.activity;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import liangbin.funshow.R;
import liangbin.funshow.manage.TitleListAdapter;
import liangbin.funshow.manage.TitleListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FunshowFragment extends android.support.v4.app.Fragment {

    private List<TitleListView> titleList=new ArrayList<TitleListView>();
    TitleListAdapter adapter;
    private final String funshowHogepage="http://bbs.jnustu.org/";
    private final String CampusTrends1="http://bbs.jnustu.org/forum.php?" +
                                         "mod=forumdisplay&fid=5";
    private final String JobMarket= "http://wsq.discuz.qq.com/?" +
            "c=index&a=index&f=wx&fid=18&siteid=265049098";
    private final String SecondHand= "http://wsq.discuz.qq.com/?" +
            "c=index&a=index&f=wx&fid=77&siteid=265049098";
    private final  String CampusTrends="http://wsq.discuz.qq.com/?" +
            "siteid=265049098&c=index&a=index&mobile=2";

    public FunshowFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleListView();
         adapter= new TitleListAdapter(getActivity(),R.layout.title_list_item,
                titleList);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view=inflater.inflate(R.layout.fragment_funshow_, container, false);
        ListView listView;
        listView=(ListView)view.findViewById(R.id.fusnhow_listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TitleListView titleListView=titleList.get(position);
                switch (position){
                    case 0:
                        Intent intent1=new Intent(getActivity(),WebViewActivity.class);
                        intent1.putExtra("links",funshowHogepage);
                        //定义一个名为what的String给处理此链接的Activity,否则会引起空指针异常。
                        //这是因为在WebViewActivity中会接收该数据,下同
                        intent1.putExtra("what","noNeedThis");
                        intent1.putExtra("title","FunShow");
                        startActivity(intent1);
                        break;
                    case 1:

                        Intent intent2 =new Intent(getActivity(),WebViewActivity.class);
                        intent2.putExtra("links",CampusTrends);
                        intent2.putExtra("what","noNeedThis");
                        intent2.putExtra("title","FunShow");
                        startActivity(intent2);
                        break;
                    case 2:
                        Intent intent3 =new Intent(getActivity(),WebViewActivity.class);
                        intent3.putExtra("links",JobMarket);
                        intent3.putExtra("title","FunShow");
                        intent3.putExtra("what","noNeedThis");
                        startActivity(intent3);
                        break;
                    case 3:
                        Intent intent4 =new Intent(getActivity(),WebViewActivity.class);
                        intent4.putExtra("links",SecondHand);
                        intent4.putExtra("title","FunShow");
                        intent4.putExtra("what","noNeedThis");
                        startActivity(intent4);
                        break;
                    default:
                        break;

                }
            }
        });
        return view;
    }
   private void initTitleListView(){
       TitleListView funshow = new TitleListView("FunShow",R.drawable.funshow);
       titleList.add(funshow);
       TitleListView funshow1= new TitleListView("校园热点",R.drawable.fs_campus_focus);
       titleList.add(funshow1);
       TitleListView funshow2= new TitleListView("就业招聘",R.drawable.fs_jobs);
       titleList.add(funshow2);
       TitleListView secondHand =new TitleListView("二手市场",R.drawable.fs_secondhand_m);
       titleList.add(secondHand);
   }

}
