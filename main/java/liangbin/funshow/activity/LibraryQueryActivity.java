package liangbin.funshow.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import liangbin.funshow.R;
import liangbin.funshow.manage.NetworkStatus;

public class LibraryQueryActivity extends Activity {

    Button buttonQuery;
    Button buttonHistroy;
    Button buttonFindBook;
    CheckBox checkBox;
    EditText editTextNum;
    EditText editTextName;
    TextView textView;
    String getResults;
    String getName;
    String getNum;

    StringBuilder stringBuilder=new StringBuilder();
    StringBuilder stringBuilderHis=new StringBuilder();

    private int responseCode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;

    private final int SHOW_RESULTS1=1;
    private final int SHOW_RESULTS2=2;
    private final int CONNECT_TIMEOUT=3;

    //定义四个字符串数组存贮续借返回的数据
    String[] titles=new String[20];
    String[] status=new String[20];
    String[] callNums=new String[20];
    String[] barCodes=new String[20];

    //定义三个字符串数组存贮借阅历史返回的数据
    String[] titlesHistory=new String[200];
    String[] author=new String[200];
    String[] dates=new String[200];

    //筛选中用到的索引
    int select=0;
    int select1=1;
    int select2=1;
    int select3=1;
    int select4=1;
    String Tips="\n校园卡号为你饭卡上的六位卡号，如194200。\n"+"你可以在这里查询你" +
            "已借出书本的到期状态和历史借阅记录。\n"+"已经借出的书逾期不还" +
            "或不续借会产生相应费用，你可以在这里查看是否已经被罚款的金额。\n"+"查询不到结果时请" +
            "检查输入是否有误。\n"+"本页面数据采集自暨大图书馆。";

    private Handler handler=new Handler(){
      public void handleMessage(Message msg){
          switch (msg.what){
              case SHOW_RESULTS1:
                   getResults=(String)msg.obj;
                   parseHtml();
                   progressDialog.dismiss();

                  if (select==0&&select1==1){
                      AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                              .setTitle("没有需要归还的书")
                              .setMessage("该帐号所有图书已归还，没有需要续借的书！" +
                                      "如与你的实际情况不符合，请确认你的六位学生卡号和" +
                                      "姓名输入是否有错");
                      setPositiveButton(builder).create().show();

                  }else {
                      Intent intent=new Intent(LibraryQueryActivity.this,ShowResultsActivity.class);
                      intent.putExtra("title","未归还的书-"+getName);
                      intent.putExtra("result",stringBuilder.toString());
                      startActivity(intent);
                  }
                  initSelect();
                  break;
              case SHOW_RESULTS2:
                  getResults=(String)msg.obj;
                  parseHtmlHistory();
                  progressDialog.dismiss();
                  if (select==0&&select1==1){
                      AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                              .setTitle("你没有借阅记录")
                              .setMessage("该帐号还没有图书馆借阅记录，如与你的实际情况不符合，" +
                                      "请确认你的六位学生卡号和姓名输入是否有错");
                      setPositiveButton(builder).create().show();

                  }else {

                      Intent intent=new Intent(LibraryQueryActivity.this,ShowResultsActivity.class);
                      intent.putExtra("title","图书借阅记录-"+getName);
                      intent.putExtra("result",stringBuilderHis.toString());
                      startActivity(intent);
                  }
                  initSelect();
                  break;
              case CONNECT_TIMEOUT:
                  progressDialog.dismiss();
                  AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                          .setTitle("连接超时")
                          .setMessage("请检查你的网络设置或更换网络环境稍后再试！");
                  setPositiveButton(builder).create().show();
              default:
                  break;
          }

      }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.library_query_layout);
        buttonQuery=(Button)findViewById(R.id.lib_query_button);
        buttonHistroy=(Button)findViewById(R.id.lib_history_button);
        buttonFindBook=(Button)findViewById(R.id.lib_book_search);
        textView=(TextView)findViewById(R.id.lib_results);
        textView.setText(Tips);
        TextView titleText=(TextView)findViewById(R.id.activity_title_text);
        titleText.setText("图书馆服务");
        editTextNum=(EditText)findViewById(R.id.lib_account);
        editTextName=(EditText)findViewById(R.id.lib_name);
        checkBox=(CheckBox)findViewById(R.id.lib_remember_checkBox);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        boolean isRemmember=sharedPreferences.getBoolean("LibRemember",false);

        if(isRemmember){
            String storeName=sharedPreferences.getString("LibName","");
            String storeNum=sharedPreferences.getString("LibNum","");
            editTextName.setText(storeName);
            editTextNum.setText(storeNum);
            checkBox.setChecked(true);
        }
        //查询续借以及欠费
        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus=new NetworkStatus();
                getName=editTextName.getText().toString();
                getNum="0000"+editTextNum.getText().toString();
                if(editTextNum.getText().toString().isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                            .setTitle("输入错误")
                            .setMessage("请输入六位校园卡号！");
                    setPositiveButton(builder).create().show();

                }else if (getName.isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                            .setTitle("输入错误")
                            .setMessage("名字不能留空！请输入名字！");
                    setPositiveButton(builder).create().show();

                }else {
                    if (networkStatus.canConntect()){
                        rememberMe();
                        createProgressDialog();
                        queryLib();
                    }

                }

            }
        });
        buttonHistroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus=new NetworkStatus();
                getName=editTextName.getText().toString();
                getNum="0000"+editTextNum.getText().toString();
                if(editTextNum.getText().toString().isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                            .setTitle("输入错误")
                            .setMessage("请输入六位校园卡号！");
                    setPositiveButton(builder).create().show();

                }else if (getName.isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(LibraryQueryActivity.this)
                            .setTitle("输入错误")
                            .setMessage("名字不能留空！请输入名字！");
                    setPositiveButton(builder).create().show();

                }else {
                    if(networkStatus.canConntect()){
                        rememberMe();
                        createProgressDialog();
                        LibQueryReadHistory();
                    }


                }

            }
        });
        buttonFindBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus=new NetworkStatus();
                if (networkStatus.canConntect()){
                    Intent intent=new Intent(LibraryQueryActivity.this,WebViewActivity.class);
                    intent.putExtra("links","http://202.116.13.3:8080/sms/opac/" +
                            "search/showSearch.action?xc=6");
                    intent.putExtra("what"," ");
                    intent.putExtra("whatMsg","");
                    intent.putExtra("title","馆藏查询");
                    startActivity(intent);
                }

            }
        });

    }

    private void LibQueryReadHistory(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient=new DefaultHttpClient();
                HttpResponse httpResponse;
                try {
                HttpPost httpPost=new HttpPost("http://202.116.13.244/patroninfo*chx");
                HttpParams httpParams=new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                HttpConnectionParams.setSoTimeout(httpParams, 3000);
                httpPost.setParams(httpParams);
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("code",getNum));

                params.add(new BasicNameValuePair("name", getName));

                    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    httpClient.execute(httpPost);
                    HttpGet httpGet=new HttpGet("http://202.116.13.244/patronin" +
                            "fo~S1*chx/1094279/readinghistory");
                    HttpParams httpParamsGet=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParamsGet, 3000);
                    HttpConnectionParams.setSoTimeout(httpParamsGet, 3000);
                    httpGet.setParams(httpParamsGet);
                    httpResponse=httpClient.execute(httpGet);
                    HttpEntity httpEntity=httpResponse.getEntity();
                    String results= EntityUtils.toString(httpEntity);
                    Message message=new Message();
                    message.what=SHOW_RESULTS2;
                    message.obj=results;
                    handler.sendMessage(message);

                }catch (Exception e){
                    e.printStackTrace();
                    Message message=new Message();
                    message.what=CONNECT_TIMEOUT;
                    handler.sendMessage(message);
                }


            }
        }).start();

    }
    public void queryLib(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient=new DefaultHttpClient();
                HttpResponse httpResponse;
                HttpPost httpPost=new HttpPost("http://202.116.13.244/patroninfo*chx");
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("code",getNum));
                params.add(new BasicNameValuePair("name",getName));
                try {
                    HttpParams httpParams=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpPost.setParams(httpParams);
                    httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                    httpClient.execute(httpPost);
                    HttpGet httpGet=new HttpGet("http://202.116.13.244/patron" +
                            "info~S1*chx/1094113/items");
                    HttpParams httpParamsGet=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParamsGet, 3000);
                    HttpConnectionParams.setSoTimeout(httpParamsGet, 3000);
                    httpGet.setParams(httpParamsGet);
                   httpResponse=httpClient.execute(httpGet);
                    HttpEntity httpEntity=httpResponse.getEntity();
                    String results= EntityUtils.toString(httpEntity);
                    Message message=new Message();
                    message.what=SHOW_RESULTS1;
                    message.obj=results;
                    handler.sendMessage(message);


                }catch (Exception e){
                    e.printStackTrace();
                    Message message=new Message();
                    message.what=CONNECT_TIMEOUT;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }
    public void parseHtml(){
        stringBuilder.delete(0,stringBuilder.length());
        stringBuilder.append("\n"+getName+"，你好！你尚未归还的书如下，" +
                "请注意在到期时间前续借或归还，以免产生额外费用！"+"\n\n\n");
        Document document1= Jsoup.parse(getResults);
        Elements elements1=document1.select(".patFunc");
        Document document2=Jsoup.parse(elements1.toString());
        Elements elements3=document2.select(".patFuncStatus");
        Elements elements2=document2.select(".patFuncTitleMain");
        Elements elements4=document2.select(".patFuncCallNo");
        Elements elements5=document2.select(".patFuncBarcode");

        for(Element element:elements2){
            String stringName=element.text();
            titles[select1++-1]="书本名称：\n\n"+stringName;
            select++;
            if(select1==19){
                break;
            }

        }
        for(Element element:elements3){
            String stringName=element.text();
            status[select2++-1]="书本状态："+stringName;
            if (select2==19){
                break;
            }


        }
        for(Element element:elements4){
            String stringName=element.text();
            callNums[select3++-1]="检索号："+stringName;
            if (select3==19){
                break;
            }

        }
        for(Element element:elements5) {
            String stringName = element.text();
            barCodes[select4++-1]="条形码："+stringName;
            if (select4==19){
                break;
            }

        }
        for (int i=0;i<select;i++){
            stringBuilder.append(titles[i]+"\n\n"+status[i]+
                    "\n\n"+callNums[i]+"\n\n"+barCodes[i]+"\n\n\n");
            if (select==19){
                break;
            }
        }
    }
    private void parseHtmlHistory(){
        stringBuilderHis.delete(0,stringBuilderHis.length());
        stringBuilderHis.append("\n"+getName+"，你好！你在图书馆的书本借阅记录如下，" +
                "尚未归还的书本，请注意在到期时间前续借或归还，" +
                "以免产生额外费用！"+"\n\n\n");
        Document document1=Jsoup.parse(getResults);
        Elements elements1=document1.select(".patFunc");
        Document document2=Jsoup.parse(elements1.toString());
        Elements elements2=document2.select(".patFuncTitleMain");
        Elements elements3=document2.select(".patFuncAuthor");
        Elements elements4=document2.select(".patFuncDate");
        for(Element element:elements2){
            String stringName=element.text();
            titlesHistory[select1++-1]="书本名称："+stringName;
            select++;
            if(select1==199){
                break;
            }
        }
        for(Element element:elements3){
            String stringName=element.text();
            author[select2++-1]="书本作者："+stringName;
            if(select2==199){
                break;
            }
        }
        for(Element element:elements4){
            String stringName=element.text();
            dates[select3++-1]="借出时间："+stringName;
            if(select3==199){
                break;
            }
        }
        select4=001;
        for (int i=0;i<select;i++){
            stringBuilderHis.append("时间序号："+select4+++"\n"+titlesHistory[i]
            +"\n"+author[i]+"\n"+dates[i]+"\n\n");
            if (select==199){
                break;
            }
        }
    }
    private void rememberMe(){
        editor=sharedPreferences.edit();
        if(checkBox.isChecked()){
            editor.putBoolean("LibRemember",true);
            editor.putString("LibName",editTextName.getText().toString());
            editor.putString("LibNum",editTextNum.getText().toString());
        }else {
            editor.clear();
        }
        editor.commit();
    }
    public void initSelect(){
        select=0;
        select1=1;
        select2=1;
        select3=1;
        select4=1;

    }
    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder){
        return builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int whitch){
            }
        });
    }
    private void createProgressDialog(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("查询中");
        progressDialog.setMessage("正在努力查询图书中，请稍候...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
}
