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
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/8/24.
 */
public class CetQueryByAdmissionNumActivity extends Activity {
    Button button;
    CheckBox checkBox;
    EditText nameEditText;
    EditText numEditText;
    TextView textView;
    final int SHOW_RESULTS=1;
    ProgressDialog progressDialog;
    int resultsLength=0;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    StringBuilder stringBuilder=new StringBuilder();
    String tips="这里可以查询最新公布的大学四六级成绩。\n"+"暨大考点考生往年四六级成绩" +
            "查询，请选择“身份证+名字”查询入口";
    Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){

                case SHOW_RESULTS:
                    progressDialog.dismiss();
                    if (resultsLength!=7){
                        AlertDialog.Builder builder=new AlertDialog.Builder
                                (CetQueryByAdmissionNumActivity.this)
                                .setTitle("查询出错")
                                .setMessage("请检查准考证号和姓名是否有错，若输入正确，则" +
                                        "可能成绩是成绩尚未公布，处于不可查询状态");
                        setPositiveButton(builder).create().show();
                    }else {
                        String sendResults=(String)msg.obj;
                        Intent intent=new Intent(CetQueryByAdmissionNumActivity.this,
                                ShowResultsActivity.class);
                        intent.putExtra("title","查询结果-"+nameEditText.getText().toString());
                        intent.putExtra("result",sendResults);
                        startActivity(intent);

                    }
                    resultsLength=0;
                    stringBuilder.delete(0,stringBuilder.length());

                default:
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cet_by_admission_num_layout);
        button=(Button)findViewById(R.id.cet_by_ad_query_button);
        checkBox=(CheckBox)findViewById(R.id.cet_by_ad_remember_number);
        nameEditText=(EditText)findViewById(R.id.cet_by_ad_name);
        numEditText=(EditText)findViewById(R.id.cet_by_ad_ticket_number);
        TextView textViewTitle=(TextView)findViewById(R.id.activity_title_text);
        textViewTitle.setText("四六级查询");
        textView=(TextView)findViewById(R.id.cet_by_ad_textView);
        textView.setText(tips);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemmember=sharedPreferences.getBoolean("CetByAdRemember",false);

        if(isRemmember){
            String storeName=sharedPreferences.getString("CetByAdName","");
            String storeNum=sharedPreferences.getString("CetByAdNum","");
            nameEditText.setText(storeName);
            numEditText.setText(storeNum);
            checkBox.setChecked(true);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numEditText.getText().toString().length()!=15){
                    AlertDialog.Builder builder=new AlertDialog.Builder
                            (CetQueryByAdmissionNumActivity.this)
                            .setTitle("输入错误")
                            .setMessage("请正确输入15位准考证号！");
                    setPositiveButton(builder).create().show();
                }else if (nameEditText.getText().toString().isEmpty()){
                    AlertDialog.Builder builder=new AlertDialog.Builder
                            (CetQueryByAdmissionNumActivity.this)
                            .setTitle("输入错误")
                            .setMessage("请输入姓名！");
                    setPositiveButton(builder).create().show();
                }else {
                    rememberMe();
                    createProgressDialog();
                    queryCet();
                }
            }
        });


    }
    public void queryCet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter out = null;
                BufferedReader in = null;
                String results = "";

                try {
                    URL url = new URL("http://cet.99sushe.com/find");
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("User-Agent", " Mozilla/5.0 " +
                            "(Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
                    connection.setRequestProperty("Accept", " text/html,application/xhtml+xml," +
                            "application/xml;q=0.9,image/webp,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Language", " en-us,en;q=0.5");
                    connection.setRequestProperty("Referer", " http://cet.99sushe.com");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    out = new PrintWriter(connection.getOutputStream());
                    String name = URLEncoder.encode(nameEditText.getText().toString(), "gb2312");
                    String adminssionNum = numEditText.getText().toString();
                    out.print("id=" + adminssionNum + "&name=" + name);
                    out.flush();
                    in = new BufferedReader(new InputStreamReader
                            (connection.getInputStream(), "GBK"));
                    String line;
                    while ((line = in.readLine()) != null) {
                        results +=line;
                    }
                    //根据准考证号第十位判断四六级
                    String[] admissionNumCheck=numEditText.getText().toString().split("");
                    if (admissionNumCheck[10].equals("1")){
                        stringBuilder.append("\n级别：4级\n");

                    }if (admissionNumCheck[10].equals("2")){
                        stringBuilder.append("\n级别：6级\n");
                    }

                    //把返回的结果拆分为数组
                    String[] responses = results.split(",");
                    resultsLength=responses.length;
                    String[] titles = { "听力：", "阅读：", "写作：",
                            "总分：", "学校：", "姓名："};
                 int select=1;
               if (resultsLength==(titles.length+1)){
                       for (int i = 0; i < titles.length; i++) {
                           stringBuilder.append("\n" + titles[i] + responses[select++] + "\n");

                   if(select==responses.length){
                       break;
                   }
                       }
                   }

                    String result = stringBuilder.toString();
                    Message message = new Message();
                    message.what = SHOW_RESULTS;
                    message.obj = result;
                    handler.sendMessage(message);


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
    private void createProgressDialog(){
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("查询中");
        progressDialog.setMessage("正在努力查询，请稍候...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    private void rememberMe(){
        editor=sharedPreferences.edit();
        if(checkBox.isChecked()){
            editor.putBoolean("CetByAdRemember",true);
            editor.putString("CetByAdName", nameEditText.getText().toString());
            editor.putString("CetByAdNum", numEditText.getText().toString());
        }else {
            editor.clear();
        }
        editor.commit();
    }
    }


