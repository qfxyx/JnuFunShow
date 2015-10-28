package liangbin.funshow.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import liangbin.funshow.R;
import liangbin.funshow.manage.LinksData;
import liangbin.funshow.manage.NetworkStatus;

/**
 * Created by Administrator on 2015/8/4.
 */
public class CetQueryActivity extends Activity{
    private Button queryButton;
    private TextView resultTextView;
    private EditText ticketNumber;
    private EditText name;
    HttpClient httpClient;
    //Remember the account and your name
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;
    String VIEWSTATE;
    String VIEWSTATEGENERATOR;
    String EVENTVALIDATION;
    String isInitOk="no";
    //存贮获取到的的html文档
    String response;
    //存贮解析后的结果，即显示的结果
    StringBuilder stringBuilder=new StringBuilder();
    String[] tableTitle={"日期：","级别：","姓名：","总分：",
            "听力：","阅读：","写作：","综合：","准考证号："};
    //定义一个游标循环获取tableTitle数组值得
    private int currentTitle = 0;
    //定义一个查询时的对话框
    ProgressDialog progressDialog;
    String tips="查询期限：\n2005年上半年至最近一次已经公布的CET成绩。" +
            "\n说明：\n这里仅限、只能查询暨南大学考点的考生信息。\n成绩发布当天，暨大有可能" +
            "未在第一时间向系统录入结果，" +
            "若查询不到四六级最新成绩，请选择“准考证+名字”入口试试。";
    private final int SHOW_RESULT=0;
    private final int CONNECT_TIMEOUT=1;
    private Handler handler= new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_RESULT:
                     response=(String)msg.obj;
                    parseHtml();
                   // resultTextView.setText(stringBuilder);
                    progressDialog.dismiss();
                    if(currentTitle<8){
                        String waring="请检查身份证号或姓名是否正确";
                        AlertDialog.Builder builder=new AlertDialog.Builder(CetQueryActivity.this)
                                .setTitle("查询出错")
                                .setMessage(waring);
                        setPositiveButton(builder).create().show();
                        //Toast.makeText(CetQueryActivity.this,waring,Toast.LENGTH_LONG).show();
                        currentTitle=0;
                    }else {
                        currentTitle=0;
                        String sendName=name.getText().toString();
                        String sendResult=stringBuilder.toString();
                        Intent intent=new Intent(CetQueryActivity.this,ShowResultsActivity.class);
                        intent.putExtra("title","四六级成绩--"+sendName);
                        intent.putExtra("result",sendResult);
                        startActivity(intent);
                    }
                    break;

                case CONNECT_TIMEOUT:
                    progressDialog.dismiss();
                    String waring="连接超时，请检查你的网络状况";
                    AlertDialog.Builder builder=new AlertDialog.Builder(CetQueryActivity.this)
                            .setTitle("连接超时")
                            .setMessage(waring);
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
        setContentView(R.layout.cet_query_layout);
        queryButton=(Button)findViewById(R.id.cet_query_button);
        resultTextView=(TextView)findViewById(R.id.cet_result);
        resultTextView.setText(tips);
        getparams();
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("四六级查询");
        ticketNumber=(EditText)findViewById(R.id.cet_ticket_number);
        name=(EditText)findViewById(R.id.cet_name);
        checkBox=(CheckBox)findViewById(R.id.cet_remember_number);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember=sharedPreferences.getBoolean("CetRemember",false);
        if (isRemember){
            String storeNumber = sharedPreferences.getString("CetStoreNumber","");
            String storeName= sharedPreferences.getString("CetStoreName","");
            ticketNumber.setText(storeNumber);
            name.setText(storeName);
            checkBox.setChecked(true);
        }
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus=new NetworkStatus(CetQueryActivity.this);
                if (networkStatus.canConntect()){
                    if (isInitOk.equals("yes")){
                        if(ticketNumber.getText().toString().length()<8){
                            String waring="请正确输入身份证号";
                            AlertDialog.Builder builder=new AlertDialog.Builder(CetQueryActivity.this)
                                    .setTitle("输入错误")
                                    .setMessage(waring);
                            setPositiveButton(builder).create().show();
                        }else if(name.getText().toString().isEmpty()){
                            String waring="请输入姓名";
                            AlertDialog.Builder builder=new AlertDialog.Builder(CetQueryActivity.this)
                                    .setTitle("输入错误")
                                    .setMessage(waring);
                            setPositiveButton(builder).create().show();
                        }else {
                            createProgressDialog();
                            sendQuery();

                        }

                    }else {
                        String waring="亲，你按得太快啦，程序正在初始化，请耐心等待2秒钟...";
                        AlertDialog.Builder builder=new AlertDialog.Builder(CetQueryActivity.this)
                                .setTitle("请稍等2秒哦")
                                .setMessage(waring);
                        setPositiveButton(builder).create().show();
                        getparams();

                    }
                }

            }
        });


    }
    public void getparams(){
        httpClient=new DefaultHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpGet httpGet0=new HttpGet(LinksData.jun_cet);
                try {
                    HttpParams httpParams= new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpGet0.setParams(httpParams);
                    HttpResponse httpResponse0= httpClient.execute(httpGet0);
                    HttpEntity httpEntity0=httpResponse0.getEntity();
                    String string0=EntityUtils.toString(httpEntity0);
                    Document document0=Jsoup.parse(string0);
                    Elements elements0=document0.getElementsByTag("input");
                    Document document1=Jsoup.parse(elements0.toString());
                    Elements elements2=document1.select("#__VIEWSTATE");
                    Elements elements3=document1.select("#__VIEWSTATEGENERATOR");
                    Elements elements4=document1.select("#__EVENTVALIDATION");
                    for(org.jsoup.nodes.Element element:elements2){
                        VIEWSTATE=element.attr("value");
                    }
                    for(org.jsoup.nodes.Element element:elements3){
                        VIEWSTATEGENERATOR=element.attr("value");
                    }
                    for(org.jsoup.nodes.Element element:elements4){
                        EVENTVALIDATION=element.attr("value");
                    }
                    isInitOk="yes";
                }catch (Exception e){
                    e.printStackTrace();
                    isInitOk="";
                }

            isInitOk="yes";
            }
        }).start();


    }
    public void sendQuery(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String Idcard = ticketNumber.getText().toString();
                String names =name.getText().toString();
                editor=sharedPreferences.edit();
                if(checkBox.isChecked()){
                    editor.putBoolean("CetRemember",true);
                    editor.putString("CetStoreName",names);
                    editor.putString("CetStoreNumber",Idcard);
                }else {
                    editor.clear();
                }
                editor.commit();
                try{
                    HttpParams httpParams=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,3000);
                    HttpConnectionParams.setSoTimeout(httpParams,3000);
                    HttpPost httpPost=new HttpPost(LinksData.jun_cet);
                    httpPost.setParams(httpParams);
                    List<NameValuePair> params =new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("TextBox2",Idcard));
                    params.add(new BasicNameValuePair("TextBox1",names));
                    params.add(new BasicNameValuePair("__EVENTVALIDATION",EVENTVALIDATION));
                    params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR",VIEWSTATEGENERATOR));
                    params.add(new BasicNameValuePair("__VIEWSTATE",VIEWSTATE));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,"utf-8");
                   // StringEntity stringEntity= new StringEntity("id=440020142207620&name=梁斌");
                   // stringEntity.setContentType("application/x-www-form-urlencoded");

                   // httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse=httpClient.execute(httpPost);
                    //TestCode
                    //HttpGet httpGet = new HttpGet("http://www.chsi.com.cn/cet/query");
                    //HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode()==200){
                        HttpEntity httpEntity =httpResponse.getEntity();
                        String response = EntityUtils.toString(httpEntity,"utf-8");
                        Message message=new Message();
                        message.what=SHOW_RESULT;
                        message.obj=response.toString();
                        handler.sendMessage(message);
                        //testCode
                        /*Message message1=new Message();
                        message1.what=SHOW_RESULT1;
                        message1.obj=testResponcode;
                        handler.sendMessage(message1);*/
                    }


                }catch (Exception e){
                    e.printStackTrace();
                    Message message=new Message();
                    message.what=CONNECT_TIMEOUT;
                    handler.sendMessage(message);
                }

            }
        }).start();
    }
    private void parseHtml(){
        stringBuilder.delete(0, stringBuilder.length());
        Document document= Jsoup.parse(response);
        Elements table=document.select("#GridView1");
        Document row =Jsoup.parse(table.toString());
        Elements results= row.getElementsByTag("td");
        for (Element result:results){
             String string1=result.text();
            //判断是否需要多隔一行
            if (currentTitle%tableTitle.length==0){
                stringBuilder.append("\n"+tableTitle[currentTitle++%tableTitle.length]
                        +string1+"\n");
            }else {
                stringBuilder.append(tableTitle[currentTitle++%tableTitle.length]
                        +string1+"\n");
            }


        }


    }
    private void createProgressDialog(){
        progressDialog=new ProgressDialog(CetQueryActivity.this);
        progressDialog.setTitle("查询中");
        progressDialog.setMessage("正在努力查询中，请稍候...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder){
        return builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int whitch){
                //progressDialog.dismiss();
            }
        });
    }

}
