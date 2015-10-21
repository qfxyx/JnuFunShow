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

/**
 * Created by Administrator on 2015/8/27.
 */
public class GradesActivity extends Activity {
    private HttpClient httpClient;
    private final int SHOW_VALIDATEPIC=1;
    private final int SHOW_RESULTS=2;
    private final int GETPIC_TIMEOUT=3;
    private final int CONNECT_TIMEOUT=4;

    String VIEWSTATE;
    String VIEWSTATEGENERATOR;
    String EVENTVALIDATION;
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
    ProgressDialog progressDialog1;
    ProgressDialog progressDialog2;
    StringBuilder stringBuilder=new StringBuilder();
    private EditText validateCode;
    CheckBox checkBox;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    int select=1;
    String Myname;

    private Handler handler= new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_VALIDATEPIC:
                    validateImage.setImageBitmap(validateBitmap);
                    break;
                case SHOW_RESULTS:
                    response=(String)msg.obj;
                    progressDialog1.dismiss();
                    //createProgressDialog2();
                    parseResults();
                   // progressDialog2.dismiss();
                    if (select>7){
                        Intent intent=new Intent(GradesActivity.this,ShowResultsActivity.class);
                        intent.putExtra("title","最好成绩-"+Myname);
                        intent.putExtra("result",stringBuilder.toString());
                        startActivity(intent);
                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder(GradesActivity.this)
                                .setTitle("查询失败")
                                .setMessage("请检查输入是否错误！");
                        setPositiveButton(builder).create().show();
                    }
                    stringBuilder.delete(0,stringBuilder.length());
                    select=1;
                    break;
                case GETPIC_TIMEOUT:
                    AlertDialog.Builder builder=new AlertDialog.Builder(GradesActivity.this)
                            .setTitle("连接超时")
                            .setMessage("获取图片验证码失败，请检查网络设置！");
                    setPositiveButton(builder).create().show();
                    break;
                case CONNECT_TIMEOUT:
                    progressDialog1.dismiss();
                    AlertDialog.Builder builder1=new AlertDialog.Builder(GradesActivity.this)
                            .setTitle("连接超时")
                            .setMessage("连接服务器失败，请检查网络设置或稍后再试！");
                    setPositiveButton(builder1).create().show();

                default:
                    break;

            }

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grades_query_activity_layuot);
        getQueryValidateCode();
        queryButton=(Button)findViewById(R.id.grades_query_button);
        validateImage=(ImageView)findViewById(R.id.grades_validate_image);
        textView=(TextView)findViewById(R.id.grades_results);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("成绩查询");
        accountNum=(EditText)findViewById(R.id.grades_account);
        password=(EditText)findViewById(R.id.grades_password);
        validateCode=(EditText)findViewById(R.id.grades_validate_code);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        checkBox=(CheckBox)findViewById(R.id.grades_remember_checkBox);
        boolean isRemember=sharedPreferences.getBoolean("gradesRemember",false);
        if (isRemember){
            String getName=sharedPreferences.getString("gradesName","");
            String getPassword=sharedPreferences.getString("gradesPassword","");
            accountNum.setText(getName);
            password.setText(getPassword);
            checkBox.setChecked(true);
        }


        //点击图片刷新验证码
        validateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus=new NetworkStatus(GradesActivity.this);
                if(networkStatus.canConntect()){
                    refreshValidateCode();
                }


            }
        });
        //点击查询
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus=new NetworkStatus(GradesActivity.this);
                userName=accountNum.getText().toString();
                userPassword=password.getText().toString();
                if (userName.isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(GradesActivity.this)
                            .setTitle("输入错误")
                            .setMessage("学号不能留空！");
                    setPositiveButton(builder).create().show();
                }else if (userPassword.isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(GradesActivity.this)
                            .setTitle("输入错误")
                            .setMessage("密码不能留空！");
                    setPositiveButton(builder).create().show();
                }else if (validateCode.getText().toString().isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(GradesActivity.this)
                            .setTitle("请输入验证码")
                            .setMessage("验证码不能留空！可以点击验证码图片刷新");
                    setPositiveButton(builder).create().show();
                }else {
                    if (networkStatus.canConntect()){
                        queryGrades();
                        createProgressDialog1();
                    }

                }


                //testpost();
            }
        });
        Button button=(Button)findViewById(R.id.grades_test_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //testpost();
                //textView.setText(stringBuilder);
               // setContentView(R.layout.cet_by_admission_num_layout);
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
                    HttpParams httpParams=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,3000);
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

                    stringBuilder.append(VIEWSTATE+"\n\n"+VIEWSTATEGENERATOR+
                            "\n\n"+EVENTVALIDATION+"\n\n");


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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpGet httpGet = new HttpGet("http://202.116.0.176/ValidateCode.aspx");
                    HttpParams httpParams=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,3000);
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
                    message.what=GETPIC_TIMEOUT;
                    handler.sendMessage(message);
                }

            }
        }).start();
    }
    private void queryGrades(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                editor=sharedPreferences.edit();
                if(checkBox.isChecked()){
                    editor.putString("gradesName",userName);
                    editor.putString("gradesPassword",userPassword);
                    editor.putBoolean("gradesRemember",true);

                }else {
                    editor.clear();
                }
                editor.commit();

                String getValidateCode=validateCode.getText().toString().trim();
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
                    HttpParams httpParams=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,3000);
                    HttpConnectionParams.setSoTimeout(httpParams, 3000);
                    httpPost.setParams(httpParams);
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    //httpResponse=httpClient.execute(httpPost);
                    httpClient.execute(httpPost);
                    HttpGet httpGet=new HttpGet("http://202.116.0.176/Secure/" +
                            "Cjgl/Cjgl_Cjcx_WdCj.aspx");
                    httpResponse=httpClient.execute(httpGet);
                    HttpEntity httpEntity=httpResponse.getEntity();
                    String results=EntityUtils.toString(httpEntity);
                    Document document0= Jsoup.parse(results);
                    Elements elements0=document0.getElementsByTag("input");
                    Document document1=Jsoup.parse(elements0.toString());
                    Elements elements2=document1.select("#__VIEWSTATE");
                    Elements elements3=document1.select("#__VIEWSTATEGENERATOR");
                    Elements elements4=document1.select("#__EVENTVALIDATION");
                    Elements elements5=document1.select("#__EVENTARGUMENT");
                    Elements elements6=document1.select("#txtXH");
                    Elements elements7=document1.select("#txtXM");
                    Elements elements8=document1.select("#txtYXZY");
                    String VIEWSTATE1="";
                    String VIEWSTATEGENERATOR1="";
                    String EVENTVALIDATION1="";
                    String EVENTARGUMENT="";
                    String txtXH="";
                    String txtYXZY="";

                    // the stringBuilder below is just for test
                    //stringBuilder.delete(0,stringBuilder.length());
                    for(Element element:elements2){
                        VIEWSTATE1=element.attr("value");
                       // stringBuilder.append(VIEWSTATE1+"\n\n");
                    }
                    for(Element element:elements3){
                        VIEWSTATEGENERATOR1=element.attr("value");
                        //stringBuilder.append(VIEWSTATEGENERATOR1+"\n\n");
                    }
                    for(Element element:elements4){
                        EVENTVALIDATION1=element.attr("value");
                        //stringBuilder.append(EVENTVALIDATION1+"\n\n");
                    }
                    for(Element element:elements5){
                        EVENTARGUMENT=element.attr("value");
                       // stringBuilder.append(EVENTARGUMENT+"\n\n");
                    }
                    for(Element element:elements6){
                        txtXH=element.attr("value");
                        //stringBuilder.append(txtXH+"\n\n");
                    }
                    for(Element element:elements7){
                        Myname=element.attr("value");
                       // stringBuilder.append(Myname+"\n\n");
                    }
                    for(Element element:elements8){
                        txtYXZY=element.attr("value");
                        //stringBuilder.append(txtYXZY+"\n\n");
                    }

                    httpPost=new HttpPost("http://202.116.0.176/Secure/" +
                            "Cjgl/Cjgl_Cjcx_WdCj.aspx");
                    List<NameValuePair> params1=new ArrayList<NameValuePair>();
                    params1.add(new BasicNameValuePair("__EVENTARGUMENT",""));
                    params1.add(new BasicNameValuePair("__EVENTTARGET","lbtnQuery"));
                    params1.add(new BasicNameValuePair("__EVENTVALIDATION",EVENTVALIDATION1));
                    params1.add(new BasicNameValuePair("__VIEWSTATE",VIEWSTATE1));
                    params1.add(new BasicNameValuePair("__VIEWSTATEGENERATOR",VIEWSTATEGENERATOR1));
                    params1.add(new BasicNameValuePair("ddlXXLB","主修"));
                    params1.add(new BasicNameValuePair("rbtnListLBXX","最好成绩列表"));
                    params1.add(new BasicNameValuePair("txtXH",txtXH));
                    params1.add(new BasicNameValuePair("txtXM",Myname));
                    params1.add(new BasicNameValuePair("txtYXZY",txtYXZY));

                    //格式很重要，否则会获取不到返回结果

                    httpPost.setEntity(new UrlEncodedFormEntity(params1,"gbk"));
                    HttpResponse httpResponse1=httpClient.execute(httpPost);
                    HttpEntity httpEntity1=httpResponse1.getEntity();
                    String results1=EntityUtils.toString(httpEntity1);
                    returnCode=httpResponse1.getStatusLine().getStatusCode();
                    Message message=new Message();
                    message.what=SHOW_RESULTS;
                    message.obj=results1;
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
        progressDialog1.setTitle("查询中");
        progressDialog1.setMessage("正在努力获取结果，请稍候...");
        progressDialog1.setCancelable(true);
        progressDialog1.show();
    }
    private void createProgressDialog2(){
        progressDialog2=new ProgressDialog(this);
        progressDialog2.setTitle("解析中");
        progressDialog2.setMessage("正在努力解析结果，请稍候...");
        progressDialog2.setCancelable(true);
        progressDialog2.show();
    }
    public void parseResults(){
        stringBuilder.delete(0,stringBuilder.length());
        Document document1=Jsoup.parse(response.replace("&nbsp;","replace"));
        Elements elements1=document1.select("#GVZHCJ");
       // String sttt=elements1.toString();
        Document document2=Jsoup.parse(elements1.toString());
        Elements elements2=document2.getElementsByTag("td");

        for (Element element:elements2){
            String strings=element.text();
            if (strings.equals("replace")&&select%7!=0){
            }else {
                if(select%7==2){
                    stringBuilder.append("课程："+strings+"\n");
                }else if (select%7==3){

                    stringBuilder.append("类别："+strings+"\n");
                }else if (select%7==4){

                    stringBuilder.append("成绩："+strings+"\n");
                }else if (select%7==5){
                    stringBuilder.append("学分："+strings+"\n");

                }else if (select%7==6){

                    stringBuilder.append("绩点："+strings+"\n");

                }else if(select%7==0){
                    stringBuilder.append("\n");
                }
                else {

                }

            }


            ++select;

        }

    }

}
