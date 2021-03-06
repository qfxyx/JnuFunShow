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
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import liangbin.funshow.manage.LinksData;
import liangbin.funshow.manage.MyApplication;
import liangbin.funshow.manage.NetworkStatus;
import liangbin.funshow.manage.PreferencesHelper;

/**
 * Created by Administrator on 2015/8/18.
 */
public class CampusNetActivity extends Activity {
    //Uses a textView to show the return results
    TextView textView;
    // post datas Button
    Button button;
    CheckBox checkBox;
   // private SharedPreferences sharedPreferences;
   // private SharedPreferences.Editor editor;
    PreferencesHelper preferencesHelper=new PreferencesHelper(MyApplication.getContext(),PreferencesHelper.netInfo);
    EditText numberEditText;
    EditText nameEditText;
    String beginText="若你是学生，人事编号填写你的学号。" +"\n"
           + "若你是教职工，人事编号请直接填写你的人事编号。"+"\n"
            +"本功能仅供查询个人校园网续费情况和相关信息，请勿用于获取他人隐私。"+"\n"
            +"查询结果采集自学校网络中心。";
    String name;
    String number;
    StringBuilder stringBuilder = new StringBuilder();
    ProgressDialog progressDialog;
    //过滤数据时要用到
    int select=1;
    String response;
    final int SHOW_NET_IMFORMATION=1;
    final int CONNECTED_TIME_OUT=2;
    HttpClient httpClient;
    Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case SHOW_NET_IMFORMATION:
                    //把返回的html字符串存于response待解析
                    response=(String)msg.obj;
                    parseNet();
                    if (select>7){
                        String sendResults=stringBuilder.toString();
                        Intent intent=new Intent(CampusNetActivity.this,ShowResultsActivity.class);
                        intent.putExtra("title","校园网个人信息--"+name);
                        intent.putExtra("result",sendResults);
                        progressDialog.dismiss();
                        startActivity(intent);

                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder(CampusNetActivity.this)
                                .setTitle("查询失败")
                                .setMessage("该用户尚未开通校园网！若你已开通校园网，" +
                                        "请检查输入是否有误。");
                        setPositiveButton(builder).create().show();

                    }
                    select=1;
                    break;

                case CONNECTED_TIME_OUT:
                    progressDialog.dismiss();
                    AlertDialog.Builder builder=new AlertDialog.Builder(CampusNetActivity.this)
                            .setTitle("连接超时").setMessage("连接超时，请检查你的网络状态是否通畅"
                            +"或尝试更换网络环境，稍后再试");
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.campus_net_layout);
        textView=(TextView)findViewById(R.id.net_result_textView);
        button=(Button)findViewById(R.id.net_query_button);
        checkBox=(CheckBox)findViewById(R.id.net_remember_number);
        //sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        nameEditText=(EditText)findViewById(R.id.net_name);
        numberEditText=(EditText)findViewById(R.id.net_number);
        textView.setText(beginText);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("校园网个人信息查询");
        boolean isRemember=preferencesHelper.getBoolean("NetRemember",false);
        if (isRemember){
            String storeName=preferencesHelper.getString("NetStoreName","");
            String storeNum=preferencesHelper.getString("NetStoreNum","");
            numberEditText.setText(storeNum);
            nameEditText.setText(storeName);
            checkBox.setChecked(true);

        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkStatus networkStatus =new NetworkStatus(CampusNetActivity.this);
                if (networkStatus.canConntect()){
                    number=numberEditText.getText().toString();
                    name=nameEditText.getText().toString();
                    if (name.equals("")||number.equals("")){
                        //AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("").
                        // setIcon(R.mipmap.ic_launcher).setMessage("");
                        Toast.makeText(MyApplication.getContext(),"输入不能留空",
                                Toast.LENGTH_LONG).show();
                    }else {
                        if (checkBox.isChecked()){
                            preferencesHelper.setBoolean("NetRemember", true);
                            preferencesHelper.setString("NetStoreName", name);
                            preferencesHelper.setString("NetStoreNum", number);
                        }else {
                            preferencesHelper.clearData();
                        }
                        createProgressDialog();
                        queryNetImfo();
                    }
                }

            }
        });
    }

    private void queryNetImfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                httpClient=new DefaultHttpClient();
                HttpPost httpPost= new HttpPost(LinksData.CampusNet);
                //设置连接超时
                HttpParams httpParams=new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams,3000);
                HttpConnectionParams.setSoTimeout(httpParams,3000);
                httpPost.setParams(httpParams);
                List<NameValuePair> params =new ArrayList<NameValuePair>();
                HttpResponse httpResponse;
                params.add(new BasicNameValuePair("id_num",number));
                params.add(new BasicNameValuePair("u_name",name));
                params.add(new BasicNameValuePair("Submit","提交"));
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params,"gb2312"));
                    httpResponse=httpClient.execute(httpPost);
                    HttpEntity httpEntity=httpResponse.getEntity();
                    String webString= EntityUtils.toString(httpEntity, "gb2312");
                    Message message=new Message();
                    message.obj=webString.toString();
                    message.what=SHOW_NET_IMFORMATION;
                    handler.sendMessage(message);

                }catch (Exception e){
                    Message message=new Message();
                    message.what=CONNECTED_TIME_OUT;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }



            }
        }).start();
    }
    private void parseNet(){
        stringBuilder.delete(0,stringBuilder.length());
        Document document= Jsoup.parse(response);
        Elements elements1=document.getElementsByTag("table");
        Document document1=Jsoup.parse(elements1.toString());
        Elements elements2=document1.getElementsByTag("td");
        Document document2=Jsoup.parse(elements2.toString());
        Elements elements3=document2.getElementsByTag("FONT");
        for (Element element:elements3){
            String string1=element.text();
            if (select==18){
                break;
            }
            if(select<=8) {
                if (select % 2 == 0) {
                    stringBuilder.append(string1 + "\n" + "\n");
                } else {
                    stringBuilder.append(string1);
                }
            }else {
                if(select==11||select==15||select==17){
                    stringBuilder.append(string1 + "\n"+"\n");
                }else {
                    stringBuilder.append(string1);
                }
            }

          select++;
        }
       // Element element2=element1.getE
    }
    private void createProgressDialog(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("查询中");
        progressDialog.setMessage("正在努力查询，请稍候...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder){
        return builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
        public void onClick(DialogInterface dialogInterface,int whitch){
              progressDialog.dismiss();
            }
        });
    }
}
