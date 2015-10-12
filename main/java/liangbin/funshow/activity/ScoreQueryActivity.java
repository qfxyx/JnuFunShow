package liangbin.funshow.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.renderscript.Element;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import liangbin.funshow.R;
import liangbin.funshow.manage.MyApplication;

/**
 * Created by Administrator on 2015/8/17.
 */
public class ScoreQueryActivity extends Activity {
    private final int SHOW_VALIDATEPIC=1;
    private final int SHOW_RESULTS=2;
    private final int SHOW_TEST=3;
    HttpClient httpClient;
    HttpClient httpClient1=new DefaultHttpClient();
    HttpPost httpPost;
    Cookie cookie;

    String VIEWSTATE;
    String VIEWSTATEGENERATOR;
    String EVENTVALIDATION;

    String cookieString;
    String response;
    Button queryButton;
    ImageView validateImage;
    Bitmap validateBitmap;
    TextView textView;
    int returnCode;
    private EditText accountNum;
    private EditText password;
    String userName;
    String userPassword;
    ProgressDialog progressDialog;
    StringBuilder stringBuilder=new StringBuilder();
    StringBuilder stringBuilderTest=new StringBuilder();
    private EditText validateCode;
    CheckBox checkBox;
    String[] scoreName1={"要求:","已修:","还差:"};
    String[] scoreName2={"总学分概况：","必修：","艺术素养：","文史哲类","经管法类"
            ,"数理工类","生命类","其他类","通识教育小计","基础教育：","专业教育：",
            "跨专业教育","选修合计："};
    String[] stringsNeeds= new String[15];
    String[] stringsSduty=new String[15];
    String[] stringsLefts=new String[15];
    int select=0;
    int select1=0;
    int select2=0;
    int select3=0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //to judge if it needs input the validatecode
    int NeedValidateCode=0;


    private Handler handler= new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_VALIDATEPIC:
                    validateImage.setImageBitmap(validateBitmap);
                    break;
                case SHOW_RESULTS:
                    response=(String)msg.obj;
                    parseScores();
                    progressDialog.dismiss();
                    if (stringsNeeds[0]!=null&&stringsNeeds[0]!=null&&stringsLefts[0]!=null){
                        textView.setText(stringBuilder);
                        String sendString=stringBuilder.toString();
                        NeedValidateCode=1;
                        Intent intent=new Intent(ScoreQueryActivity.this,ShowResultsActivity.class);
                        intent.putExtra("title","FunShow祝你前程似锦");
                        intent.putExtra("result",sendString);
                        startActivity(intent);
                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder
                                (ScoreQueryActivity.this)
                                .setTitle("查询失败")
                                .setMessage("请检查输入信息是否有错误！");
                        setPositiveButton(builder).create().show();
                        String stringTest=(String)msg.obj;
                        textView.setText(stringTest);

                    }
                    select3=0;
                    select2=0;
                    select1=0;
                    break;

                   // String test11=Integer.toString(returnCode);
                   // textView.setText(test11);
                case SHOW_TEST:{
                    String stringTest=(String)msg.obj;
                    textView.setText(stringTest);

                }

                default:
                    break;

            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_query_layout);
        getQueryValidateCode();
        queryButton=(Button)findViewById(R.id.score_query_button);
        validateImage=(ImageView)findViewById(R.id.score_validate_image);
        textView=(TextView)findViewById(R.id.score_results);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("学分查询");
        accountNum=(EditText)findViewById(R.id.score_account);
        password=(EditText)findViewById(R.id.score_password);
        validateCode=(EditText)findViewById(R.id.score_validate_code);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        checkBox=(CheckBox)findViewById(R.id.score_remember_checkBox);
        textView.setText(stringBuilder);
        boolean isRemember=sharedPreferences.getBoolean("scoreRemember",false);
        if (isRemember){
            String getName=sharedPreferences.getString("scoreName","");
            String getPassword=sharedPreferences.getString("scorePassword","");
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
        //点击查询
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            userName=accountNum.getText().toString();
            userPassword=password.getText().toString();
                if (userName.isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(ScoreQueryActivity.this)
                            .setTitle("输入错误")
                            .setMessage("学号不能留空！");
                    setPositiveButton(builder).create().show();
                }else if (userPassword.isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(ScoreQueryActivity.this)
                            .setTitle("输入错误")
                            .setMessage("密码不能留空！");
                    setPositiveButton(builder).create().show();
                }else if
                    (validateCode.getText().toString().isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(ScoreQueryActivity.this)
                            .setTitle("请输入验证码")
                            .setMessage("验证码不能留空！可以点击验证码图片刷新");
                    setPositiveButton(builder).create().show();
                }else {
                    queryScores();
                    createProgressDialog();
                }


                //testpost();
            }
        });

        //TestButton
        Button button=(Button)findViewById(R.id.score_test_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //testpost();
                textView.setText(stringBuilder);
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
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    byte[] bytes= EntityUtils.toByteArray(httpResponse.getEntity());
                    validateBitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
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

                    // For Test

                    stringBuilder.append(VIEWSTATE+"\n\n"+VIEWSTATEGENERATOR+
                     "\n\n"+EVENTVALIDATION+"\n\n");


                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();


    }
    public void refreshValidateCode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpGet httpGet=new HttpGet("http://202.116.0.176/ValidateCode.aspx");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    byte[] bytes= EntityUtils.toByteArray(httpResponse.getEntity());
                    validateBitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    Message message=new Message();
                    message.what=SHOW_VALIDATEPIC;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }
    private void queryScores(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                editor=sharedPreferences.edit();
                if(checkBox.isChecked()){
                    editor.putString("scoreName",userName);
                    editor.putString("scorePassword",userPassword);
                    editor.putBoolean("scoreRemember",true);

                }else {
                    editor.clear();
                }
                editor.commit();

                String getValidateCode=validateCode.getText().toString();
                HttpPost httpPost=new HttpPost("http://202.116.0.176/Login.aspx");
                HttpResponse httpResponse;
                List<NameValuePair>params=new ArrayList<NameValuePair>();
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
                    httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                    httpResponse=httpClient.execute(httpPost);
                    httpClient.execute(httpPost);
                    HttpGet httpGet=new HttpGet("http://202.116.0.176/default.aspx");
                    httpResponse=httpClient.execute(httpGet);
                    HttpEntity httpEntity=httpResponse.getEntity();
                    String results=EntityUtils.toString(httpEntity);
                    returnCode=httpResponse.getStatusLine().getStatusCode();
                    Message message=new Message();
                    message.what=SHOW_RESULTS;
                    message.obj=results;
                    handler.sendMessage(message);


                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void parseScores(){
        stringBuilder.delete(0,stringBuilder.length());
        Document document1= Jsoup.parse(response);
        Elements elements1=document1.select(".form_table");
        Document document2=Jsoup.parse(elements1.toString());
        Elements elements4=document2.select(".needs");
        Elements elements5=document2.select(".study");
        Elements elements6=document2.select(".lefts");
        for (org.jsoup.nodes.Element element3:elements4){
            String string3= element3.text();
            if (string3.isEmpty()){
                stringsNeeds[select1++]="0";
            }else {
                stringsNeeds[select1++]=string3;
            }
            if (select1==14){
                break;
            }

        }
        for (org.jsoup.nodes.Element element4:elements5){
            String string3= element4.text();
            if (string3.isEmpty()){
                stringsSduty[select2++]="0";
            }else {
                stringsSduty[select2++]=string3;
            }
            if (select2==14){
                break;
            }
        }
        for (org.jsoup.nodes.Element element5:elements6){
            String string3= element5.text();
            if (string3.isEmpty()){
                stringsLefts[select3++]="0";
            }else {
                stringsLefts[select3++]=string3;
            }
            if (select3==14){
                break;
            }
        }
        stringBuilder.append("\n"+"                   你的学分情况如下"+"\n\n");
        for (int i=0;i<=12;i++){
            if (select%scoreName1.length==0){
                stringBuilder.append(scoreName2[i]+"\n"+"\n");
            }
            stringBuilder.append(scoreName1[select++%scoreName1.length]+stringsNeeds[i]+"----"
            +scoreName1[select++%scoreName1.length]+stringsSduty[i]+"----"
                    +scoreName1[select++%scoreName1.length]
            +stringsLefts[i]+"\n"+"\n");


        }

    select=0;

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
        progressDialog.setMessage("正在努力查询，请稍候...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    public void testpost(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter out=null;
                BufferedReader in=null;
                String results="";

                try {
                    URL url=new URL("http://cet.99sushe.com/find");
                    URLConnection connection=url.openConnection();
                    connection.setRequestProperty("User-Agent"," Mozilla/5.0 " +
                            "(Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
                    connection.setRequestProperty("Accept"," text/html,application/xhtml+xml," +
                            "application/xml;q=0.9,image/webp,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Language"," en-us,en;q=0.5");
                    connection.setRequestProperty("Referer"," http://cet.99sushe.com");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    out=new PrintWriter(connection.getOutputStream());
                    String xm= URLEncoder.encode("夏孙志", "gb2312");
                    out.print("id=440020151200317&name="+xm);
                    out.flush();
                    in=new BufferedReader(new InputStreamReader
                            (connection.getInputStream(),"GBK"));
                    String line;
                    while ((line=in.readLine())!=null){
                        results+="\n"+line;
                    }
                   String[] responses=results.split(",");
                    String[] titles={"aa:","bb:","cc:"};
                    stringBuilder.append(titles[0]+responses[0]+"\n"+titles[1]+responses[1]);
                    String result=stringBuilder.toString();
                    Message message=new Message();
                    message.what=SHOW_TEST;
                    message.obj=result;
                    handler.sendMessage(message);


                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    try{
                        if(out!=null) {
                            out.close();
                        }
                        if (in!=null){
                            in.close();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
