package liangbin.funshow.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import liangbin.funshow.R;
import liangbin.funshow.manage.NetworkStatus;

/**
 * Created by Administrator on 2015/9/8.
 */
public class TestActivity extends Activity {
    RequestQueue requestQueue;
    String cookie="";
    @Override
   public void onCreate(Bundle savedInstanceStated){
        super.onCreate(savedInstanceStated);
        setContentView(R.layout.test_activity_layout);
        final TextView textView=(TextView)findViewById(R.id.test_activity_textView);
        Button button=(Button)findViewById(R.id.test_activity_button);
        requestQueue= Volley.newRequestQueue(TestActivity.this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringRequest stringRequest=new StringRequest(Request.Method.POST,
                        "http://icas.jnu.edu.cn/cas/login",
                        new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                 textView.setText(s);
                    }
                },new Response.ErrorListener(){
                    @Override
                public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("encodedService", "http%3a%2f%2fi.jnu.edu.cn%2fdcp%2Findex.jsp");
                        map.put("loginErrCnt", "0");
                        map.put("lt", "LT-AlwaysValidTicket");
                        map.put("password", "c2b400fbf068bdb372b3a4c338aa85bb");
                        map.put("password1", "051815");
                        map.put("service", "http://i.jnu.edu.cn/dcp/index.jsp");
                        map.put("Submit", "  ");
                        map.put("username", "194295");
                        map.put("userNameType", "cardID");
                        return map;
                    }
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        for (String s : response.headers.keySet()) {
                            if (s.contains("CAS-Ticket")) {
                                cookie = response.headers.get(s);
                                break;
                            }
                        }
                        return super.parseNetworkResponse(response);
                    }



                };
                requestQueue.add(stringRequest);


            }

        });
        Button button1=(Button)findViewById(R.id.test_activity_button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest stringRequest1=new StringRequest(
                        "http://i.jnu.edu.cn/dcp/index.jsp?ticket="+cookie,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                textView.setText(s);
                            }
                        },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        for (String s : response.headers.keySet()) {
                            if (s.contains("Set-Cookie")) {
                                cookie = response.headers.get(s);
                                String[] strings=cookie.split(";");
                                cookie=strings[0];
                                break;
                            }
                        }
                        return super.parseNetworkResponse(response);
                    }


                };
                requestQueue.add(stringRequest1);

            }
        });
        Button button2=(Button)findViewById(R.id.test_activity_button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(TestActivity.this,PullRefreshActivity.class));

            }
        });

    }
}
