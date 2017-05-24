package com.william.www.stock;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import com.baidu.aip.ocr.AipOcr;
import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        //设置APPID/AK/SK
        String APP_ID = "7ec56e7ad4084e3d98cc958cd9e7a9e2";
        String API_KEY = "15a68b1243224cd9b6587c242f1efa28";
        String SECRET_KEY = "face9c631aa046269f72c99a250bd4df";

        // 初始化一个OcrClient
        //AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        //client.setConnectionTimeoutInMillis(2000);
        //client.setSocketTimeoutInMillis(60000);

        // 调用通用识别接口
        String genFilePath = Environment.getExternalStorageDirectory().getPath();
        System.out.println(genFilePath);
        //JSONObject genRes = client.general(genFilePath, new HashMap<String, String>());
        /*File f = new File(genFilePath + "/4.jpg");
        if (f.exists()){

            FileOutputStream out = null;
            try {

                String FileContent=genRes.toString(2);
                out = new FileOutputStream(new File(genFilePath + "/4.txt"));
                out.write(FileContent.getBytes());

                out.close();
                System.out.println(genRes.toString(2));
                }
            catch(Exception e){
                try {
                    throw e;
                } catch (Exception e1) {
                    // TODO 自动生成的 catch 块
                    e1.printStackTrace();
                }
            }
        }*/


    }
}
