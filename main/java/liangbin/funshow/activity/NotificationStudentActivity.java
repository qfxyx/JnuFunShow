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
 * Created by Administrator on 2015/9/11.
 */
public class NotificationStudentActivity extends Activity {
    private HttpClient httpClient;
    private final int RETURN_HTML=1;
    private final int CONNECT_TIMEOUT=2;
    private  final String studentLink="http://xsc.jnu.edu.cn/list.aspx?cid=6";
    ProgressDialog progressDialog;
    Integer select1=0;
    Integer select2=0;
    String[] titles= new String[15];
    String[] links=new String[15];
    TextView textView;
    ListView listView;
    SimpleAdapter simpleAdapter;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){

                case RETURN_HTML:
                    progressDialog.dismiss();
                    if(select1>=10&&select1==select2){
                        List<Map<String,Object>> listItems=new ArrayList<Map<String, Object>>();
                        for (int i=0;i<select1;i++){
                            Map<String,Object> listItem=new HashMap<String, Object>();
                            listItem.put("title",titles[i]);
                            // listItem.put("time",times[i]);
                            //listItem.put("link",links[i]);
                            listItems.add(listItem);
                        }
                        listView=(ListView)findViewById(R.id.teaching_listView1);
                        simpleAdapter=new SimpleAdapter(NotificationStudentActivity.this,listItems,
                                R.layout.notification_items_layout,new String[]{"title"},
                                new int[]{R.id.item_title});
                        listView.setAdapter(simpleAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                String link=links[position];
                                Intent intent=new Intent(NotificationStudentActivity.this,
                                        WebViewActivity.class);
                                intent.putExtra("links","http://xsc.jnu.edu.cn/"+link);
                                intent.putExtra("what","showToast");
                                intent.putExtra("title","学生处通知");
                                intent.putExtra("whatMsg","本页面来自暨大学生处，未经优化，" +
                                        "请自行缩放屏幕以适应手机");
                                startActivity(intent);

                            }
                        });

                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder
                                (NotificationStudentActivity.this)
                                .setTitle("连接失败")
                                .setMessage("服务器可能出错了，请尝试更换网络环境或稍后再试...");
                        setPositiveButton(builder).create().show();
                    }
                    break;

                case CONNECT_TIMEOUT:
                    progressDialog.dismiss();
                    AlertDialog.Builder builder=new AlertDialog.Builder
                            (NotificationStudentActivity.this)
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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_teaching_layout);
        textView=(TextView)findViewById(R.id.teaching_textView);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("学生处通知");
        httpClient=new DefaultHttpClient();
        createProgressDialog();
        getHtml();
    }
    private void getHtml(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    HttpGet httpGet=new HttpGet(studentLink);
                    HttpParams httpParams =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpGet.setParams(httpParams);
                    HttpResponse httpResponse=httpClient.execute(httpGet);
                    HttpEntity entity=httpResponse.getEntity();
                    String htmlString= EntityUtils.toString(entity, "gbk");

                    Document document1= Jsoup.parse(htmlString);
                    Elements elements1=document1.select("#middle");

                    Document document2=Jsoup.parse(elements1.toString());
                    Elements elements2=document2.select(".n1");
                    Document document3=Jsoup.parse(elements2.toString());
                    Elements elements3=document2.getElementsByTag("li");
                    Document document4=Jsoup.parse(elements3.toString());
                    Elements elements4=document4.getElementsByTag("a");
                    //Elements elements5=document4.select(".gray");
                    select1=0;
                    for(Element element:elements3){
                        String string=element.text();
                        titles[select1]=string;
                        select1++;
                        if (select1==15){
                            break;
                        }
                    }
                    select2=0;
                    for(Element element:elements4){
                        String link=element.attr("href");
                        links[select2]=link;
                        select2++;
                        if (select2==15){
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

