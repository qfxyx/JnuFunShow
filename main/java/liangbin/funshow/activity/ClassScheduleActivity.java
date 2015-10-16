package liangbin.funshow.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import liangbin.funshow.R;
import liangbin.funshow.manage.FunShowDatabaseHelper;
import liangbin.funshow.manage.MyApplication;
import liangbin.funshow.manage.NetworkStatus;

/**
 * Created by Administrator on 2015/8/30.
 */
public class ClassScheduleActivity extends Activity {
    private HttpClient httpClient;
    private final int SHOW_VALIDATEPIC=1;
    private final int SHOW_RESULTS=2;
    private final int SHOW_POST_RESULTS=3;
    private final int GETPIC_TIMEOUT=4;
    private final int CONNECT_TIMEOUT=5;
    private final int CONNECT_TIMEOUT_LOGIN=6;

    //定义八个Stringblider存贮课表信息
    private StringBuilder classMonday=new StringBuilder();
    private StringBuilder classTuesday=new StringBuilder();;
    private StringBuilder classWednesday=new StringBuilder();
    private StringBuilder classThursDay=new StringBuilder();
    private StringBuilder classFriday=new StringBuilder();
    private StringBuilder classSaturday=new StringBuilder();
    private StringBuilder classSunday=new StringBuilder();
    private StringBuilder classWeeks=new StringBuilder();
    String sendResults;

    //创建数据库保存课表信息
    private FunShowDatabaseHelper databaseHelper;

    private final String TAG="activity.ClassScheduleActivity";


    String VIEWSTATE;
    String VIEWSTATEGENERATOR;
    String EVENTVALIDATION;

    TextView textViewQuery;
    TextView textViewWelcome;
    TextView textViewTimes;
    String response;
    Button logInButton;
    ImageView validateImage;
    Bitmap validateBitmap;
    TextView textView;
    int returnCode;
    private EditText accountNum;
    private EditText password;
    String userName;
    String userPassword;
    ProgressDialog progressDialog1;
    ProgressDialog progressDialog2;
    StringBuilder stringBuilder=new StringBuilder();
    private EditText validateCode;
    CheckBox checkBox;
    String resultsPost;
    String test="not change ";
    String isGetResults;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    int select=0;
    String Myname="同学";

    private String mYear;
    private String mMonth;
    private String mDay;
    private String mWay;
    private Calendar calender;

    //Exam query
    private String noTimetableExam;
    String examWeeks;
    StringBuilder examMon=new StringBuilder();
    StringBuilder examTus=new StringBuilder();
    StringBuilder examWed=new StringBuilder();
    StringBuilder examThu=new StringBuilder();
    StringBuilder examFri=new StringBuilder();
    StringBuilder examSat=new StringBuilder();
    StringBuilder examSun=new StringBuilder();
    int selectExam=0;

    String tips1="第一次使用一键查询功能前，需要先进入系统更新课表和考试表。\n"+
            "一键查询的结果来自你最近一次更新的数据，如课表或考试表有改动，" +
            "请进入系统再次更新即可。\n"+"看不到图片验证码，可能与你的网络环境有关，" +
            "请尝试更换网络环境。\n";


    private Handler handler= new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_VALIDATEPIC:
                    validateImage.setImageBitmap(validateBitmap);
                    break;
                case SHOW_RESULTS:
                    response=(String)msg.obj;
                    progressDialog1.dismiss();
                    if (response.equals("整体架构"))
                    {
                    editor=sharedPreferences.edit();
                    if(checkBox.isChecked()){
                        editor.putString("class_scheduleName",userName);
                        editor.putString("class_schedulePassword",userPassword);
                        editor.putBoolean("class_scheduleRemember",true);

                    }else {
                        editor.clear();

                    }
                    editor.commit();
                   setContentView(R.layout.class_schedule_query);
                    Button buttonQueryAll=(Button)findViewById(R.id.
                            class_schedule_query_all_button);
                        textViewQuery=(TextView)findViewById(R.id.
                                class_schedule_query_results);
                        getSchedule();
                        createProgressDialog2();
                    buttonQueryAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (select>100){
                                Intent intent=new Intent(ClassScheduleActivity.this,
                                        ShowResultsActivity.class);
                                intent.putExtra("title","本学期课程表");
                                intent.putExtra("result",sendResults);
                                startActivity(intent);
                            }else {
                                AlertDialog.Builder builder = new
                                        AlertDialog.Builder(ClassScheduleActivity.this)
                                        .setTitle("获取失败")
                                        .setMessage("服务器错误，我们的程序员正在紧急维护中...");
                                setPositiveButton(builder).create().show();
                            }


                        }
                    });
                    Button buttonClassToday=(Button)findViewById(R.id.
                            class_schedule_query_today_button);
                        buttonClassToday.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            String todayResults;
                                if(select>100) {
                                    if (mWay.equals("星期一")) {
                                        todayResults = classMonday.toString().replace("课程：" +
                                                "","\n课目：").replaceAll("\\(.*\\)", "");

                                    } else if (mWay.equals("星期二")) {
                                        todayResults = classTuesday.toString().replace("课程：" +
                                                "","\n课目：").replaceAll("\\(.*\\)", "");

                                    } else if (mWay.equals("星期三")) {
                                        todayResults = classWednesday.toString().replace("课程：" +
                                                "","\n课目：").replaceAll("\\(.*\\)", "");

                                    } else if (mWay.equals("星期四")) {
                                        todayResults = classThursDay.toString().replace("课程：" +
                                                "","\n课目：").replaceAll("\\(.*\\)", "");

                                    } else if (mWay.equals("星期五")) {
                                        todayResults = classFriday.toString().replace("课程：" +
                                                "","\n课目：").replaceAll("\\(.*\\)", "");

                                    } else if (mWay.equals("星期六")) {
                                        todayResults = classSaturday.toString().replace("课程：" +
                                                "","\n课目：").replaceAll("\\(.*\\)","");

                                    } else {
                                        todayResults = classSunday.toString().
                                                replaceAll("\\(.*\\)", "");
                                    }
                                    Intent intent=new Intent(ClassScheduleActivity.this,
                                            ShowResultsActivity.class);
                                    intent.putExtra("title",mMonth+"月"+mDay+"日"+"课表");
                                    intent.putExtra("result",todayResults);
                                    startActivity(intent);
                                }else {
                                    AlertDialog.Builder builder = new
                                            AlertDialog.Builder(ClassScheduleActivity.this)
                                            .setTitle("获取失败")
                                            .setMessage("服务器错误，" +
                                                    "我们的程序员正在紧急维护中...");
                                    setPositiveButton(builder).create().show();

                                }
                            }
                        });
                    Button buttonQueryExam=(Button)findViewById(R.id.
                            class_schedule_query_exam_button);
                    buttonQueryExam.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            textViewQuery=(TextView)findViewById(R.id.
                                    class_schedule_query_results);
                            //textViewQuery.setText(test);
                            Intent intent=new Intent(ClassScheduleActivity.this,
                                    ShowResultsActivity.class);
                            intent.putExtra("title","本学期考试表-"+Myname);
                            intent.putExtra("result",examWeeks);
                            startActivity(intent);

                        }
                    });

                    Button buttonUpdateClass=(Button)findViewById(R.id.
                            class_schedule__storeClassResults_button) ;
                    buttonUpdateClass.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (select>100){
                                storeClassTimeTatle();
                                Toast.makeText(MyApplication.getContext(),"更新成功！",
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MyApplication.getContext(),"获取数据出错，无法更新！",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                        Button buttonUpdateClassTest=(Button)findViewById(R.id.
                                class_schedule__storeExamResults_button) ;
                        buttonUpdateClassTest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (selectExam>30){
                                    storeExamTimeTatle();
                                    Toast.makeText(MyApplication.getContext(),"更新成功！",
                                            Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(MyApplication.getContext(),"获取数据出错，无法更新！",
                                            Toast.LENGTH_SHORT).show();
                                }



                            }
                        });

                        textViewWelcome=(TextView)findViewById(R.id.
                                class_schedule_welcome_results);
                        textViewWelcome.setText("欢迎你，"+Myname+" !");
                        textViewTimes=(TextView)findViewById(R.id.
                                class_schedule_time_results);

                        textViewTimes.setText(mYear+"年 "+mMonth+"月 "+mDay+"日 "+mWay);

                    }else {
                        AlertDialog.Builder builder = new
                                AlertDialog.Builder(ClassScheduleActivity.this)
                                .setTitle("连接超时")
                                .setMessage("请确认学号、密码和验证码输入是否有错...");
                        setPositiveButton(builder).create().show();
                    }
                   // textView.setText(response);


                    stringBuilder.delete(0,stringBuilder.length());
                    Log.i(TAG,"get log in system content view finish");
                    break;
                case SHOW_POST_RESULTS:
                    progressDialog2.dismiss();
                    if (returnCode==500){
                        AlertDialog.Builder builder1 =
                                new AlertDialog.Builder(ClassScheduleActivity.this)
                                .setTitle("服务器错误")
                                .setMessage("这可能与服务器出错或你所处的网络有关，请尝试更换" +
                                        "网络环境或稍后再试");
                        setPositiveButton(builder1).create().show();
                        isGetResults="";

                    }else {
                       sendResults= ((String)msg.obj).replace("课程：","\n课目：").
                                replaceAll("\\(.*\\)","");

                    textViewQuery.setText(sendResults);
                    }

                    break;
                case GETPIC_TIMEOUT:
                    AlertDialog.Builder builder1=new AlertDialog.Builder
                            (ClassScheduleActivity.this)
                            .setTitle("连接超时")
                            .setMessage("获取验证码图片出错了，请检查你的网络设置！");
                    setPositiveButton(builder1).create().show();
                    break;
                case CONNECT_TIMEOUT:
                    progressDialog2.dismiss();
                    AlertDialog.Builder builder=new AlertDialog.Builder
                            (ClassScheduleActivity.this)
                            .setTitle("连接超时")
                            .setMessage("跟服务器连接超时，请检查你的网络设置！");
                    setPositiveButton(builder).create().show();
                    break;
                case CONNECT_TIMEOUT_LOGIN:
                    progressDialog1.dismiss();
                    AlertDialog.Builder builder2=new AlertDialog.Builder
                            (ClassScheduleActivity.this)
                            .setTitle("连接超时")
                            .setMessage("跟服务器连接超时，请检查你的网络设置！");
                    setPositiveButton(builder2).create().show();
                    break;

                default:
                    break;

            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_schedule_log_in);
        Log.i(TAG,"activity created");
        getQueryValidateCode();
        logInButton=(Button)findViewById(R.id.class_schedule_log_in_button);
        validateImage=(ImageView)findViewById(R.id.class_schedule_validate_image);
        textView=(TextView)findViewById(R.id.class_schedule_results);
        textView.setText(tips1);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("课程表和考试表");
        accountNum=(EditText)findViewById(R.id.class_schedule_account);
        password=(EditText)findViewById(R.id.class_schedule_password);
        validateCode=(EditText)findViewById(R.id.class_schedule_validate_code);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        getCurrentTimes();
        databaseHelper=new FunShowDatabaseHelper(this,"FunShow.db",null,2);
        //记住密码
        checkBox=(CheckBox)findViewById(R.id.class_schedule_remember_checkBox);
        boolean isRemember=sharedPreferences.getBoolean("class_scheduleRemember",false);
        if (isRemember){
            String getName=sharedPreferences.getString("class_scheduleName","");
            String getPassword=sharedPreferences.getString("class_schedulePassword","");
            accountNum.setText(getName);
            password.setText(getPassword);
            checkBox.setChecked(true);
        }


        //点击图片刷新验证码
        validateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshValidateCode();

            }
        });
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    userName = accountNum.getText().toString();
                    userPassword = password.getText().toString();
                    if (userName.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ClassScheduleActivity.this)
                                .setTitle("输入错误")
                                .setMessage("学号不能留空！");
                        setPositiveButton(builder).create().show();
                    } else if (userPassword.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ClassScheduleActivity.this)
                                .setTitle("输入错误")
                                .setMessage("密码不能留空！");
                        setPositiveButton(builder).create().show();
                    } else if (validateCode.getText().toString().isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ClassScheduleActivity.this)
                                .setTitle("请输入验证码")
                                .setMessage("验证码不能留空！可以点击验证码图片刷新");
                        setPositiveButton(builder).create().show();
                    } else {
                        if (new NetworkStatus().canConntect()){
                            createProgressDialog1();
                            logInSystem();
                        }
                    }




                //testpost();
            }
        });
        /*
        Button button=(Button)findViewById(R.id.class_schedule_test_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               textView.setText(test);
            }
        });
        */
        Button buttonClassToday=(Button)findViewById(R.id.
                class_schedule_log_in_class_today) ;
        buttonClassToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             getTodayClassWithoutLogIn();
            }
        });
        Button buttonClassTerm=(Button)findViewById(R.id.
                class_schedule_log_in_class_term) ;
        buttonClassTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClassByTermWithoutLogIn();
            }
        });


        Button buttonExam=(Button)findViewById(R.id.
                class_schedule_log_in_exam) ;
        buttonExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExamWithoutLogIn();
            }
        });



    }
    private void getQueryValidateCode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    httpClient=new DefaultHttpClient();

                    HttpGet httpGet=new HttpGet("http://202.116.0.176/ValidateCode.aspx");
                    HttpParams httpParams =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpGet.setParams(httpParams);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    byte[] bytes= EntityUtils.toByteArray(httpResponse.getEntity());
                    validateBitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    //cookieString=((AbstractHttpClient)httpClient).getCookieStore().
                    // getCookies().get(0).getValue();
                    //cookieString1=((AbstractHttpClient)httpClient).getCookieStore().
                    //  getCookies().get(1).getValue();
                    Message message=new Message();
                    message.what=SHOW_VALIDATEPIC;
                    handler.sendMessage(message);
                    HttpGet httpGet0=new HttpGet("http://202.116.0.176/");
                    HttpResponse httpResponse0= httpClient.execute(httpGet0);
                    HttpEntity httpEntity0=httpResponse0.getEntity();
                    String string0=EntityUtils.toString(httpEntity0);
                    Document document0= Jsoup.parse(string0);
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

                    // For Test

                 //   stringBuilder.append(VIEWSTATE+"\n\n"+VIEWSTATEGENERATOR+
                  //          "\n\n"+EVENTVALIDATION+"\n\n");


                }catch (Exception e){
                    e.printStackTrace();
                    Message message=new Message();
                    message.what=GETPIC_TIMEOUT;
                    handler.sendMessage(message);
                }

            }
        }).start();


    }
    public void refreshValidateCode() {
        NetworkStatus networkStatus = new NetworkStatus();
        if (networkStatus.canConntect()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpGet httpGet = new HttpGet("http://202.116.0.176/ValidateCode.aspx");
                        HttpParams httpParams =new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                        HttpConnectionParams.setSoTimeout(httpParams, 3000);
                        httpGet.setParams(httpParams);
                        HttpResponse httpResponse = httpClient.execute(httpGet);
                        byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
                        validateBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Message message = new Message();
                        message.what = SHOW_VALIDATEPIC;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message message=new Message();
                        message.what = GETPIC_TIMEOUT;
                        handler.sendMessage(message);
                    }

                }
            }).start();
        }

    }
    private void logInSystem(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                String getValidateCode=validateCode.getText().toString();
                HttpPost httpPost=new HttpPost("http://202.116.0.176/Login.aspx");
                HttpResponse httpResponse;
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("__VIEWSTATE",VIEWSTATE));
                params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR",VIEWSTATEGENERATOR));
                params.add(new BasicNameValuePair("__EVENTVALIDATION",EVENTVALIDATION));
                params.add(new BasicNameValuePair("txtYHBS",userName));
                params.add(new BasicNameValuePair("txtYHMM",userPassword));
                params.add(new BasicNameValuePair("txtFJM",getValidateCode));
                params.add(new BasicNameValuePair("btnLogin","登    录"));

                //带上cookie去则登陆失败，原因尚未明白
                //httpPost.setHeader("Cookie", "ASP.NET_SessionId=" + cookieString);
                //httpPost.setHeader("Cookie", "_D_SID=" + cookieString1);

                httpPost.addHeader("Content-Type","application/x-www-form-urlencoded");
                httpPost.addHeader("User-Agent","Mozilla/5.0 (Windows " +
                        "NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

                try {
                    HttpParams httpParams =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpPost.setParams(httpParams);
                    Thread.sleep(500);
                    httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                    httpResponse=httpClient.execute(httpPost);
                    HttpEntity httpEntity=httpResponse.getEntity();
                    String results=EntityUtils.toString(httpEntity);
                    Document document=Jsoup.parse(results);
                    String ifLogIn=document.getElementsByTag("title").get(0).text();
                    HttpResponse getName=httpClient.execute(new HttpGet("http://202.116.0.176/" +
                            "areaTopLogo.aspx"));
                    String nams=EntityUtils.toString(getName.getEntity());
                    Document docName=Jsoup.parse(nams);
                    Elements eles=docName.select("#header_lblXM");
                    Myname=eles.get(0).text();
                    returnCode=httpResponse.getStatusLine().getStatusCode();
                    Message message=new Message();
                    message.what=SHOW_RESULTS;
                    message.obj=ifLogIn;
                    handler.sendMessage(message);


                }catch (Exception e){
                    e.printStackTrace();
                    Message message=new Message();
                    message.what = CONNECT_TIMEOUT_LOGIN;
                    handler.sendMessage(message);
                }

            }
        }).start();
    }
    private void getSchedule(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpGet httpGet=new HttpGet("http://202.116.0.176/Secure/" +
                        "PaiKeXuanKe/wfrm_xk_StudentKcb.aspx");

                try{
                    HttpParams httpParams =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpGet.setParams(httpParams);
                    HttpResponse httpResponseGet=httpClient.execute(httpGet);
                    HttpEntity httpEntityGet=httpResponseGet.getEntity();
                    String resultsGet=EntityUtils.toString(httpEntityGet);
                    test="1";
                    Document document1=Jsoup.parse(resultsGet);
                    Elements elements1=document1.getElementsByTag("input");
                    test="2";
                    String EVENTARGUMENT =elements1.select("#__EVENTARGUMENT").attr("value");
                    String EVENTTARGET =elements1.select("#__EVENTTARGET").attr("value");
                    String EVENTVALIDATION1 =elements1.select("#__EVENTVALIDATION").
                            attr("value");
                    String LASTFOCUS =elements1.select("#__LASTFOCUS").attr("value");
                    String VIEWSTATE1 =elements1.select("#__VIEWSTATE").attr("value");
                    String VIEWSTATEGENERATOR1 =elements1.select("#__VIEWSTATEGENERATOR").
                            attr("value");
                    String btnExpKcb =elements1.select("#btnExpKcb").
                            attr("value");
                    test="3";
                    String dlstNdxq="第一学期";
                    String dlstNdxq0="第一学期";
                    String dlstXndZ="2015-2016";
                    String dlstXndZ0="2015-2016";

                    HttpPost httpPost=new HttpPost("http://202.116.0.176/Secure/PaiKeXuanKe/" +
                            "wfrm_xk_StudentKcb.aspx");
                    HttpResponse httpResponse;
                    List<NameValuePair> params=new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("__EVENTARGUMENT",EVENTARGUMENT));
                    params.add(new BasicNameValuePair("__EVENTTARGET",EVENTTARGET));
                    params.add(new BasicNameValuePair("__EVENTVALIDATION",EVENTVALIDATION1));
                    params.add(new BasicNameValuePair("__LASTFOCUS",LASTFOCUS));
                    params.add(new BasicNameValuePair("__VIEWSTATE",VIEWSTATE1));
                    params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR",VIEWSTATEGENERATOR1));
                    params.add(new BasicNameValuePair("btnExpKcb",btnExpKcb));
                    params.add(new BasicNameValuePair("dlstNdxq",dlstNdxq));
                    params.add(new BasicNameValuePair("dlstNdxq0",dlstNdxq0));
                    params.add(new BasicNameValuePair("dlstXndZ", dlstXndZ));
                    params.add(new BasicNameValuePair("dlstXndZ0", dlstXndZ));

                    httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows " +
                            "NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                    httpPost.setHeader("Referer", "http://202.116.0.176/" +
                            "Secure/PaiKeXuanKe/wfrm_xk_StudentKcb.aspx");
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "gbk"));
                    HttpParams httpParamsPOST =new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParamsPOST, 3000);
                    HttpConnectionParams.setSoTimeout(httpParamsPOST, 3000);
                    httpPost.setParams(httpParamsPOST);
                    HttpResponse httpResponse1= httpClient.execute(httpPost);
                    returnCode=httpResponse1.getStatusLine().getStatusCode();
                    Log.i(TAG,"get returncode");
                    if (returnCode==500){
                        isGetResults="false";
                        Log.i(TAG,"Service wrong with  500");
                    }else {
                        Log.i(TAG,"get class schedule start");
                        HttpEntity httpEntityPost=httpResponse1.getEntity();
                        resultsPost=EntityUtils.toString(httpEntityPost);
                        Document document11=Jsoup.parse(resultsPost);
                        String string11=document11.select("#ReportFrameReportViewer1").attr("src");
                        //获取需要提交的RepoetID ControlID参数 这步很关键
                        String id[]=string11.split("%26");
                        String Rid=id[1];
                        String Cid=id[2];
                        String part1="http://202.116.0.176/Reserved.ReportViewerWebControl.axd?" +
                                "Mode=true&";
                        String part2=Rid+"&"+Cid;
                        ;

                        String part3="&Culture=2052&UICulture=2052&ReportStack=" +
                                "1&OpType=ReportArea&Controller=R" +
                                "eportViewer1&PageNumber=1&ZoomMode=Percent&ZoomPc" +
                                "t=100&ReloadDocMap=true&SearchStartPage=0&LinkTarget=_top";

                        HttpGet httpGetResults=new HttpGet(part1+part2.replace("%3d","=")+part3);
                        httpGetResults.setHeader("Referer","http://202.116.0.176/Secure/" +
                                "PaiKeXuanKe/wfrm_xk_StudentKcb.aspx");
                        //test="4";
                        HttpResponse hpptresponse2=httpClient.execute(httpGetResults);
                        test="5";
                        HttpEntity httpEntity11=hpptresponse2.getEntity();
                        resultsPost=EntityUtils.toString(httpEntity11);
                        Document doc1=Jsoup.parse(resultsPost.replace("&nbsp;","replace"));
                        Elements ele1=doc1.select(".a8");
                        Document doc2=Jsoup.parse(ele1.toString());
                        Elements ele2=doc2.getElementsByTag("td");
                        String[] classTimes=new String[16];
                        String[] classNum={"周","第一节","第二节","第三节","第四节",
                                "第五节","第六节","第七节","第八节",
                                "第九节","第十节","第十一节","第十二节","第十三节","第十四节"};
                        String[] weeks={"周一","周二","周三", "周四", "周五", "周六", "周日"};

                        classMonday.delete(0, classMonday.length());
                        classTuesday.delete(0,classTuesday.length());
                        classWednesday.delete(0,classWednesday.length());
                        classThursDay.delete(0, classThursDay.length());
                        classFriday.delete(0, classFriday.length());
                        classSaturday.delete(0, classSaturday.length());
                        classSunday.delete(0,classSunday.length());
                        select=1;
                        for(Element ele:ele2){
                            Log.i(TAG,"parse html"+select);
                            String className=ele.text();
                            if (30<=select&&select<45){
                                classTimes[select%15]=className;
                            }
                            if (select>=45&&select<60&&!className.equals("replace")) {
                                if(select%15==1){
                                    classMonday.append("\n\n"+"    " +
                                            "                         ***** "
                                            +weeks[select/15-3]+" *****\n\n");
                                }else {
                                    classMonday.append("地点：" + className + "\n" + "时间：" +
                                            classNum[select % 15 - 1] + "  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }


                            if (select>=60&&select<75&&!className.equals("replace")) {
                                if(select%15==1){
                                    classTuesday.append("\n\n"+"    " +
                                            "                         ***** "
                                            +weeks[select/15-3]+" *****\n\n");
                                }else {
                                    classTuesday.append("地点：" + className + "\n" + "时间：" +
                                            classNum[select % 15 - 1] + "  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }


                            if (select>=75&&select<90&&!className.equals("replace")) {
                                if(select%15==1){
                                    classWednesday.append("\n\n"+"    " +
                                            "                         ***** "
                                            +weeks[select/15-3]+" *****\n\n");
                                }else {
                                    classWednesday.append("地点：" + className + "\n" + "时间：" +
                                            classNum[select % 15 - 1] + "  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }

                            if (select>=90&&select<105&&!className.equals("replace")) {
                                if(select%15==1){
                                    classThursDay.append("\n\n"+"    " +
                                            "                         ***** "
                                            +weeks[select/15-3]+" *****\n\n");
                                }else {
                                    classThursDay.append("地点：" + className + "\n" + "时间：" +
                                            classNum[select % 15 - 1] + "  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }

                            if (select>=105&&select<120&&!className.equals("replace")) {
                                if(select%15==1){
                                    classFriday.append("\n\n"+"    " +
                                            "                         ***** "
                                            +weeks[select/15-3]+" *****\n\n");
                                }else {
                                    classFriday.append("地点：" + className + "\n" + "时间：" +
                                            classNum[select % 15 - 1] + "  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }


                            if (select>=120&&select<135&&!className.equals("replace")) {
                                if(select%15==1){
                                    classSaturday.append("\n\n" + "    " +
                                            "                         ***** "
                                            + weeks[select / 15 - 3] + " *****\n\n");
                                }else {
                                    classSaturday.append("地点：" + className + "\n" + "时间：" +
                                            classNum[select % 15 - 1] + "  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }


                            if (select>=135&&select<150&&!className.equals("replace")) {
                                if(select%15==1){
                                    classSunday.append("\n\n"+"    " +
                                            "                         ***** "
                                            +weeks[select/15-3]+"*****\n\n");
                                }else {
                                    classSunday.append("地点："+className+"\n"+"时间："+
                                            classNum[select%15-1]+"  "
                                            +classTimes[select%15]+"\n\n");
                                }
                            }



                            select++;

                        }
                        Log.i(TAG,"parse html finish");
                    }

                    HttpPost httpPostExam=new HttpPost("http://202.116.0.176/Secure/PaiKeXuanKe/" +
                            "wfrm_xk_StudentKcb.aspx");
                    List<NameValuePair> paramsExam=new ArrayList<NameValuePair>();
                    paramsExam.add(new BasicNameValuePair("__EVENTARGUMENT",EVENTARGUMENT));
                    paramsExam.add(new BasicNameValuePair("__EVENTTARGET",EVENTTARGET));
                    paramsExam.add(new BasicNameValuePair("__EVENTVALIDATION",EVENTVALIDATION1));
                    paramsExam.add(new BasicNameValuePair("__LASTFOCUS",LASTFOCUS));
                    paramsExam.add(new BasicNameValuePair("__VIEWSTATE",VIEWSTATE1));
                    paramsExam.add(new BasicNameValuePair("__VIEWSTATEGENERATOR",VIEWSTATEGENERATOR1));
                   //与课程表post的参数不一样
                    paramsExam.add(new BasicNameValuePair("btnNewExpKsb","导出或打印考试安排表"));
                    paramsExam.add(new BasicNameValuePair("dlstNdxq",dlstNdxq));
                    paramsExam.add(new BasicNameValuePair("dlstNdxq0",dlstNdxq0));
                    paramsExam.add(new BasicNameValuePair("dlstXndZ",dlstXndZ));
                    paramsExam.add(new BasicNameValuePair("dlstXndZ0",dlstXndZ));

                    httpPostExam.addHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPostExam.addHeader("User-Agent","Mozilla/5.0 (Windows " +
                            "NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                    httpPostExam.setHeader("Referer", "http://202.116.0.176/" +
                            "Secure/PaiKeXuanKe/wfrm_xk_StudentKcb.aspx");
                    httpPostExam.setEntity(new UrlEncodedFormEntity(paramsExam,"gbk"));
                    HttpResponse httpResponseExam= httpClient.execute(httpPostExam);
                    returnCode=httpResponseExam.getStatusLine().getStatusCode();
                    if (returnCode==500){
                        isGetResults="false";
                    }else {
                        HttpEntity httpEntityExam=httpResponseExam.getEntity();
                        resultsPost=EntityUtils.toString(httpEntityExam);
                        Document document11=Jsoup.parse(resultsPost);
                        String string11=document11.select("#ReportFrameReportViewer1").attr("src");
                        //获取需要提交的RepoetID ControlID参数 这步很关键
                        String id[]=string11.split("%26");
                        String Rid=id[1];
                        String Cid=id[2];
                        String part1="http://202.116.0.176/Reserved.ReportViewerWebControl.axd?" +
                                "Mode=true&";
                        String part2=Rid+"&"+Cid;
                        ;

                        String part3="&Culture=2052&UICulture=2052&ReportStack=" +
                                "1&OpType=ReportArea&Controller=R" +
                                "eportViewer1&PageNumber=1&ZoomMode=Percent&ZoomPc" +
                                "t=100&ReloadDocMap=true&SearchStartPage=0&LinkTarget=_top";

                        HttpGet httpGetResults=new HttpGet(part1+part2.replace("%3d","=")+part3);
                        httpGetResults.setHeader("Referer","http://202.116.0.176/Secure/" +
                                "PaiKeXuanKe/wfrm_xk_StudentKcb.aspx");
                        test="6";
                        HttpResponse hpptresponseExam2=httpClient.execute(httpGetResults);
                        test="7";
                        HttpEntity httpEntityExam2=hpptresponseExam2.getEntity();
                        String examHtml=EntityUtils.toString(httpEntityExam2);
                        Document documentExam1=Jsoup.parse(examHtml);
                        Elements elementsExam1=documentExam1.select(".a8");
                        noTimetableExam=elementsExam1.text().replace("未排考课程：",
                                "\n未排考课程：\n").replace(",", "\n");
                        Elements elementsExam2=documentExam1.select(".a9");
                        Document documentExam2=Jsoup.parse(elementsExam2.toString().
                                replace("&nbsp;","replace"));
                        Elements elementsExam3=documentExam2.getElementsByTag("td");
                        selectExam=0;
                        for (Element element:elementsExam3){
                            String exams=element.text();
                            if(selectExam>=21&&selectExam<28&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examMon.append("\n\n\n***** 周一 *****\n");
                                }else {
                                    examMon.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            if(selectExam>=28&&selectExam<35&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examTus.append("\n\n\n***** 周二 *****\n");
                                }else {
                                    examTus.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            if(selectExam>=35&&selectExam<42&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examWed.append("\n\n\n***** 周三 *****\n");
                                }else {
                                    examWed.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            if(selectExam>=42&&selectExam<49&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examThu.append("\n\n\n***** 周四 *****\n");
                                }else {
                                    examThu.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            if(selectExam>=49&&selectExam<56&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examFri.append("\n\n\n***** 周五 *****\n");
                                }else {
                                    examFri.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            if(selectExam>=56&&selectExam<63&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examSat.append("\n\n\n***** 周六 *****\n");
                                }else {
                                    examSat.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            if(selectExam>=63&&selectExam<70&&!exams.equals(" ")&&
                                    !exams.equals("replace")&&!exams.isEmpty()){
                                if (selectExam%7==0){
                                    examSun.append("\n\n\n***** 周日 *****\n");
                                }else {
                                    examSun.append(exams.replace("待定","\n地点：待定").
                                            replace("考试时间：","\n\n时间：").replace("课程",
                                            "\n课程").replaceAll("\\(.*\\)", ""));
                                }
                            }
                            selectExam++;
                        }
                        examWeeks=noTimetableExam+examMon.toString()+examTus.toString()+examWed.toString()
                                +examThu.toString()+examFri.toString()
                                +examSat.toString()+examSun.toString();
                    }
                    String returnString=classMonday.toString()+classTuesday
                            +classWednesday.toString()+classThursDay.toString()+
                              classFriday.toString()
                            +classSaturday.toString() +classSunday.toString();
                    Message message=new Message();
                    message.what=SHOW_POST_RESULTS;
                    message.obj=returnString;
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
    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder){
        return builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int whitch){
            }
        });
    }
    private void createProgressDialog1(){
        progressDialog1=new ProgressDialog(this);
        progressDialog1.setTitle("登录中");
        progressDialog1.setMessage("正在努力登录中，请稍候...");
        progressDialog1.setCancelable(true);
        progressDialog1.show();
    }
    private void createProgressDialog2(){
        progressDialog2=new ProgressDialog(this);
        progressDialog2.setTitle("查询中");
        progressDialog2.setMessage("正在努力获取结果，请稍候...");
        progressDialog2.setCancelable(true);
        progressDialog2.show();
    }
    private void getCurrentTimes(){
        calender=Calendar.getInstance();
        calender.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mYear = String.valueOf(calender.get(Calendar.YEAR)); // 获取当前年份
        mMonth = String.valueOf(calender.get(Calendar.MONTH) + 1);// +1是因为月份是从0开始，特例
        mDay = String.valueOf(calender.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        mWay = String.valueOf(calender.get(Calendar.DAY_OF_WEEK));
        if("1".equals(mWay)){
            mWay ="星期天";
        }else if("2".equals(mWay)){
            mWay ="星期一";
        }else if("3".equals(mWay)){
            mWay ="星期二";
        }else if("4".equals(mWay)){
            mWay ="星期三";
        }else if("5".equals(mWay)){
            mWay ="星期四";
        }else if("6".equals(mWay)){
            mWay ="星期五";
        }else if("7".equals(mWay)){
            mWay ="星期六";
        }
    }
    private void storeClassTimeTatle(){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("stu_num",userName);
        values.put("stu_name",Myname);
        values.put("class_mon",classMonday.toString().replace("课程：" +
                "","\n课目：").replaceAll("\\(.*\\)", ""));
        values.put("class_tus", classTuesday.toString().replace("课程：" +
                "", "\n课目：").replaceAll("\\(.*\\)", ""));
        values.put("class_wed",classWednesday.toString().replace("课程：" +
                "", "\n课目：").replaceAll("\\(.*\\)", ""));
        values.put("class_thu",classThursDay.toString().replace("课程：" +
                "", "\n课目：").replaceAll("\\(.*\\)", ""));
        values.put("class_fri",classFriday.toString().replace("课程：" +
                "", "\n课目：").replaceAll("\\(.*\\)", ""));
        values.put("class_sat",classSaturday.toString().replace("课程：" +
                "", "\n课目：").replaceAll("\\(.*\\)", ""));
        values.put("class_sun",classSunday.toString().replace("课程：" +
                "","\n课目：").replaceAll("\\(.*\\)", ""));
        //claas将错就错
        values.put("claas_week",sendResults);
        Cursor cursor = database.query("class_timetable",null, "stu_num=?",
                new String[]{userName},null,null,null);

            database.delete("class_timetable", null, null);
            database.insert("class_timetable", null, values);

            //database.update("class_timetable",values,"stu_num=?",new String[]{userName});


        values.clear();
        cursor.close();
    }

    private void storeExamTimeTatle(){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put("stu_num",userName);
        values.put("stu_name",Myname);
        values.put("exam_mon",examMon.toString());
        values.put("exam_tus", examTus.toString());
        values.put("exam_wed",examWed.toString());
        values.put("exam_thu",examThu.toString());
        values.put("exam_fri",examFri.toString());
        values.put("exam_sat",examSat.toString());
        values.put("exam_sun",examSun.toString());
        values.put("exam_weeks",examWeeks);

        database.delete("exam_timetable", null, null);
        database.insert("exam_timetable", null, values);

        //database.update("class_timetable",values,"stu_num=?",new String[]{userName});
        values.clear();
    }

    private void getTodayClassWithoutLogIn(){

        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        Cursor cursor=database.query("class_timetable",null, null,
                null,null,null,null);
        int count=cursor.getCount();
        if(count==0){
        AlertDialog.Builder builder = new
                AlertDialog.Builder(ClassScheduleActivity.this)
                .setTitle("结果为空")
                .setMessage("请先登录并进入系统，选择更新课程表后再使用该功能...");
        setPositiveButton(builder).create().show();

    }else {
            String todayResults;
            if (mWay.equals("星期一")) {
                todayResults = "class_mon";
            } else if (mWay.equals("星期二")) {
                todayResults = "class_tus";

            } else if (mWay.equals("星期三")) {
                todayResults = "class_wed";
            } else if (mWay.equals("星期四")) {
                todayResults = "class_thu";

            } else if (mWay.equals("星期五")) {
                todayResults ="class_fri";

            } else if (mWay.equals("星期六")) {
                todayResults = "class_sat";

            } else {
                todayResults = "class_sun";
            }
        String results="";
        String myname="";
        if(cursor.moveToFirst()){
            do {
                results=cursor.getString(cursor.getColumnIndex
                        (todayResults));
                myname=cursor.getString((cursor.getColumnIndex("stu_name")));
            }while (cursor.moveToNext());
            Intent intent=new Intent(ClassScheduleActivity.this,
                    ShowResultsActivity.class);
            intent.putExtra("title",mMonth+"月"+mDay+""+"日课表-"+myname);
            intent.putExtra("result",results);
            startActivity(intent);

        }

    }
    cursor.close();

    }



    private void getClassByTermWithoutLogIn() {
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        Cursor cursor=database.query("class_timetable",null, null,
                null,null,null,null);
        int count=cursor.getCount();
        if(count==0){
            AlertDialog.Builder builder = new
                    AlertDialog.Builder(ClassScheduleActivity.this)
                    .setTitle("结果为空")
                    .setMessage("请先登录并进入系统，选择更新课程表后再使用该功能...");
            setPositiveButton(builder).create().show();

        }else {
            String results="";
            String names="";
            if(cursor.moveToFirst()){
                do {
                    results=cursor.getString(cursor.getColumnIndex
                            ("claas_week"));
                    names=cursor.getString(cursor.getColumnIndex("stu_name"));
                }while (cursor.moveToNext());
                Intent intent=new Intent(ClassScheduleActivity.this,
                        ShowResultsActivity.class);
                intent.putExtra("title","本学期课程表-"+names);
                intent.putExtra("result",results);
                startActivity(intent);


            }

        }
    cursor.close();
    }


    private void getExamWithoutLogIn(){
        SQLiteDatabase database=databaseHelper.getWritableDatabase();
        Cursor cursor=database.query("exam_timetable",null, null,
                null,null,null,null);
        int count=cursor.getCount();
        if(count==0){
            AlertDialog.Builder builder = new
                    AlertDialog.Builder(ClassScheduleActivity.this)
                    .setTitle("结果为空")
                    .setMessage("请先登录并进入系统，选择更新考试表后再使用该功能...");
            setPositiveButton(builder).create().show();

        }else {
            String results="";
            String names="";
            if(cursor.moveToFirst()){
                do {
                    results=cursor.getString(cursor.getColumnIndex
                            ("exam_weeks"));
                   names=cursor.getString(cursor.getColumnIndex("stu_name"));
                }while (cursor.moveToNext());
                Intent intent=new Intent(ClassScheduleActivity.this,
                        ShowResultsActivity.class);
                intent.putExtra("title","本学期考试表-"+names);
                intent.putExtra("result",results);
                startActivity(intent);


            }

        }
        cursor.close();


    }

}
