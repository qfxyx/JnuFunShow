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
import liangbin.funshow.manage.LinksData;


/**
 * Created by Administrator on 2015/9/10.
 */
public class NotificationTeachingActivity extends Activity{

    private HttpClient httpClient;
    private final int RETURN_HTML=1;
    private final int CONNECT_TIMEOUT=2;
    private final String teachingLink= LinksData.notification_teaching;
    ProgressDialog progressDialog;

    Integer select1=0;
    Integer select2=0;
    Integer select3=0;
    String[] titles= new String[20];
    String[] times=new String[20];
    String[] links=new String[20];
    TextView textView;
    ListView listView;
    SimpleAdapter simpleAdapter;
    Handler handler=new Handler(){
        @Override
    public void handleMessage(Message message){
            switch (message.what){
                case RETURN_HTML:
                    progressDialog.dismiss();
                   // textView.setText(links[0]);
                    //textView.setText(message.obj.toString());
                    if(select1>=15&&select1==select2&&select2==select3){
                        List<Map<String,Object>> listItems=new ArrayList<Map<String, Object>>();
                        for (int i=0;i<select1;i++){
                            Map<String,Object> listItem=new HashMap<String, Object>();
                            listItem.put("title",titles[i]);
                            listItem.put("time",times[i]);
                            //listItem.put("link",links[i]);
                            listItems.add(listItem);
                        }
                        listView=(ListView)findViewById(R.id.teaching_listView1);
                        simpleAdapter=new SimpleAdapter(NotificationTeachingActivity.this,listItems,
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
                                Intent intent=new Intent(NotificationTeachingActivity.this,
                                        ShowTeachStudentNotifiActivity.class);
                                intent.putExtra("links","http://jwc.jnu.edu.cn/"+link);
                                intent.putExtra("notificationTitle",titles[position]);
                                intent.putExtra("what","showToast");
                                intent.putExtra("title","教务处通知");
                                intent.putExtra("whatMsg","本页面来自暨大教务处，未经优化，" +
                                        "请自行缩放屏幕以适应手机");
                                startActivity(intent);

                    }
                        });


                        }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder
                                (NotificationTeachingActivity.this)
                                .setTitle("连接失败")
                                .setMessage("服务器可能出错了，请尝试更换网络环境或稍后再试...");
                        setPositiveButton(builder).create().show();
                    }

                    break;
                case CONNECT_TIMEOUT:
                    progressDialog.dismiss();
                    AlertDialog.Builder builder=new AlertDialog.Builder
                            (NotificationTeachingActivity.this)
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
        textViewTitle.setText("教务处通知");
        createProgressDialog();
        getHtml();
    }

    private void getHtml(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    httpClient=new DefaultHttpClient();
                    HttpGet httpGet=new HttpGet(teachingLink);
                    HttpParams httpParams =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpGet.setParams(httpParams);
                    HttpResponse httpResponse=httpClient.execute(httpGet);
                    HttpEntity entity=httpResponse.getEntity();
                    String htmlString= EntityUtils.toString(entity,"gbk");

                    Document document1=Jsoup.parse(htmlString);
                    Elements elements1=document1.getElementsByAttributeValue("width","756");

                    Document document2=Jsoup.parse(elements1.toString());
                    Elements elements2=document2.select(".unnamed3");
                    Document document3=Jsoup.parse(elements2.toString());
                    Elements elements3=document2.getElementsByAttributeValue("width", "80%");
                    Document document4=Jsoup.parse(elements3.toString());
                    Elements elements4=document4.getElementsByTag("a");
                    Elements elements5=document2.getElementsByAttributeValue("width","13%");
                    select1=0;
                    for(Element element:elements3){
                        String string=element.text();
                        titles[select1]=string;
                        select1++;
                        if (select1==20){
                            break;
                        }
                    }
                    select2=0;
                    for(Element element:elements4){
                        String link=element.attr("href");
                        links[select2]=link;
                        select2++;
                        if (select2==20){
                            break;
                        }
                    }
                    select3=0;
                    for(Element element:elements5){
                        String string=element.text();
                        times[select3]=string;
                        select3++;
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
