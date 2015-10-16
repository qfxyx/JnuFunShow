package liangbin.funshow.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/9/17.
 */
public class NotificationCampusActivity extends Activity {

    private HttpClient httpClient;
    private final int RETURN_HTML=1;
    private final int CONNECT_TIMEOUT=2;
    private final String comprehensiveLink="http://www.jnu.edu.cn/jnu2014/" +
            "article_list.asp?channelID=5037";
    ProgressDialog progressDialog;

    Integer select1=0;
    Integer select2=0;
    Integer select3=0;
    Integer select4=0;
    String[] titles= new String[20];
    String[] times=new String[20];
    String[] links=new String[20];
    String[] froms=new String[20];
    TextView textView;
    ListView listView;
    SimpleAdapter simpleAdapter;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case RETURN_HTML:
                    progressDialog.dismiss();
                   // textView.setText(select1.toString()+" "+select2.toString()+" "+select3.toString()+" "+select4.toString());
                   // textView.setText(message.obj.toString());
                    if(select3==20&&select1==select2){
                        List<Map<String,Object>> listItems=new ArrayList<Map<String, Object>>();
                        for (int i=0;i<select3-1;i++){
                            Map<String,Object> listItem=new HashMap<String, Object>();
                            listItem.put("title",titles[i+1]);
                            listItem.put("time",froms[i]+"\n"+times[i].substring(0,10))
                                   ;
                            //listItem.put("link",links[i]);
                            listItems.add(listItem);
                        }
                        listView=(ListView)findViewById(R.id.teaching_listView1);
                        simpleAdapter=new SimpleAdapter(NotificationCampusActivity.this,listItems,
                                R.layout.notification_items_layout,new String[]{"title","time"},
                                new int[]{R.id.item_title,R.id.item_time});
                        listView.setAdapter(simpleAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                String link=links[position];
                                link=link.replace("教务处","%BD%CC%CE%F1%B4%A6").
                                        replace("通知", "%CD%A8%D6%AA");
                                Intent intent=new Intent(NotificationCampusActivity.this,
                                        WebViewActivity.class);
                                intent.putExtra("links","http://www.jnu.edu.cn/" +
                                        "jnu2014/content.asp?newsPath=1/W_5037_"+link);
                                intent.putExtra("what","showToast");
                                intent.putExtra("title","校内通知");
                                intent.putExtra("whatMsg","本页面来自暨大官网，未经优化，" +
                                        "请自行缩放屏幕以适应手机");
                                startActivity(intent);

                            }
                        });


                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder
                                (NotificationCampusActivity.this)
                                .setTitle("连接失败")
                                .setMessage("服务器可能出错了，请尝试更换网络环境或稍后再试...");
                        setPositiveButton(builder).create().show();
                    }

                    break;
                case CONNECT_TIMEOUT:
                    progressDialog.dismiss();
                    AlertDialog.Builder builder=new AlertDialog.Builder
                            (NotificationCampusActivity.this)
                            .setTitle("连接超时")
                            .setMessage("连接服务器出错了，请检查你的网络设置！");
                    setPositiveButton(builder).create().show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceStated){
        super.onCreate(savedInstanceStated);
        setContentView(R.layout.notification_teaching_layout);
        textView=(TextView)findViewById(R.id.teaching_textView);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("校内通知");
        createProgressDialog();
        getHtml();
    }

    private void getHtml(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    httpClient=new DefaultHttpClient();
                    HttpGet httpGet=new HttpGet("http://www.jnu.edu.cn" +
                            "/pub/channel/channel_5037_20_1.xml");
                    HttpParams httpParams =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpGet.setParams(httpParams);
                    HttpResponse httpResponse=httpClient.execute(httpGet);
                    HttpEntity entity=httpResponse.getEntity();
                    String htmlString= EntityUtils.toString(entity, "utf-8");

                    Document document1= Jsoup.parse(htmlString);
                    Elements elements1=document1.getElementsByTag("title");
                    Elements elements2=document1.getElementsByTag("publish:newsID");
                    Elements elements3=document1.getElementsByTag("pubDate");
                    Elements elements4=document1.getElementsByTag("publish:publishDept");
                    select1=0;
                    for(Element element:elements2){
                        String string=element.text();
                        links[select1]=string;
                        select1++;
                        if (select1==20){
                            break;
                        }
                    }
                    select2=0;
                    for(Element element:elements3){
                        String string=element.text();
                        times[select2]=string;
                        select2++;
                        if (select2==20){
                            break;
                        }
                    }
                    select4=0;
                    for(Element element:elements4){
                        String string=element.text();
                        froms[select4]=string;
                        select4++;
                        if (select4==20){
                            break;
                        }
                    }

                    select3=0;
                    for(Element element:elements1){
                        String string=element.text();
                        if(select3!=0){
                           titles[select3]=string;
                            select3++;
                        }else {
                            select3++;
                        }

                        if (select3==20){
                            break;
                        }
                    }

                    Message message=new Message();
                    message.what=RETURN_HTML;
                    message.obj=htmlString;
                    handler.sendMessage(message);

                }catch (Exception e){
                    e.printStackTrace();
                    Message message=new Message();
                    message.what = CONNECT_TIMEOUT;
                    handler.sendMessage(message);
                }

            }
        }).start();
    }
    private void createProgressDialog(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("连接中");
        progressDialog.setMessage("正在努力获取结果，请稍候...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder){
        return builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int whitch){
            }
        });
    }
}

