package com.example.kevinrose.handle_json;

import android.app.Activity;
import android.app.ProgressDialog;
import android.icu.text.SymbolTable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MainActivity extends Activity implements View.OnClickListener {
    String base_url="http://t.weather.sojson.com/api/weather/city/";//http访问基础页面
    Spinner city_spinner;
    Button check_button;
    private List<String> city_list=new ArrayList<String>();//城市列表
    private Map<String,String> city_id_dict=new HashMap<String,String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_list();
    }
    Runnable network_request=new Runnable() {
        @Override
        public void run() {
            String request_url = base_url + city_id_dict.get(city_list.get(city_spinner.getSelectedItemPosition()));
            try {
                URL url=new URL(request_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                // 设置连接超时时长
                httpURLConnection.setConnectTimeout(5000);
                // 设置请求方式Get
                httpURLConnection.setRequestMethod("GET");
                // 注意：是执行httpURLConnection.getResponseCode()的时候，才开始向服务器发送请求
                String response="";
                int code = httpURLConnection.getResponseCode();
                if(code==200)//请求成功
                {
                    InputStream is=httpURLConnection.getInputStream();
                    //System.out.println(httpURLConnection.getContent());//测试出来这就是一个GZIPinputStream
                    GZIPInputStream in = (GZIPInputStream) httpURLConnection.getContent();//获得一个GZIP输入流
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in, "utf-8"));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }
                    net_request_Handler.obtainMessage(1,response).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void init_list()//用于初始化可查询城市List及用List初始化和下拉列表
    {
        this.city_spinner=(Spinner) findViewById(R.id.city_spinner);
        this.check_button=findViewById(R.id.check_button);
        this.city_list.add("北京");
        this.city_list.add("西安");
        this.city_list.add("上海");
        this.city_list.add("台北");
        this.city_list.add("香港");
        this.city_list.add("澳门");
        this.city_id_dict.put("北京","101010100");
        this.city_id_dict.put("西安","101110101");
        this.city_id_dict.put("上海","101020100");
        this.city_id_dict.put("台北","101340101");
        this.city_id_dict.put("香港","101320101");
        this.city_id_dict.put("澳门","101330101");
        /* support_simple_spinner_dropdown_item 为自定义下拉菜单样式定义在res/layout 目录下 */
        ArrayAdapter arr_adpater=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,this.city_list);
        //设置下拉适配器
        arr_adpater.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        //将adapter加入到spinner中，初始化完成
        this.city_spinner.setAdapter(arr_adpater);
        this.city_spinner.setClickable(true);
        this.check_button.setOnClickListener(this);//让当前环境监听onclick，不设置则Onclick方法没有效果
    }
    Handler net_request_Handler=new Handler(){
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 1://请求成功情况，对JSON数据进行处理
                    String data= (String) msg.obj;
                    Handle_JSON(data);//对JSON数据进行解析
                    break;
            }
            Bundle data = msg.getData();
            String val = data.getString("value");
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_button:
                new Thread(this.network_request).start();
                break;
            default:
        }
    }
    private void Handle_JSON(String result)
    {
        String toastinfo="";
        try {
            JSONObject jsonObjectALL = new JSONObject(result);
            String data = jsonObjectALL.optString("data");
            JSONObject dataJSONObject = new JSONObject(data);
            JSONArray json_arr=dataJSONObject.getJSONArray("forecast");//forecast对应于一个json数组，把他拿到
            toastinfo+="空气质量:"+json_arr.getJSONObject(0).get("aqi")+"\n";
            toastinfo+="最高气温:"+json_arr.getJSONObject(0).get("high")+"\n";
            toastinfo+="最低气温:"+json_arr.getJSONObject(0).get("low");
            Toast.makeText(this,toastinfo,Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
