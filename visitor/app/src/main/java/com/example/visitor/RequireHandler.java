package com.example.visitor;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class RequireHandler {

    public String postRequest(String requestURL, HashMap<String, String> postDataParams) {
        URL url;
        String response = "連接失敗";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //設置連線超時
            conn.setConnectTimeout(1500000);
            //設置從主機讀取超時
            conn.setReadTimeout(1500000);

            conn.setRequestMethod("POST");
            //允許從服務器獲得響應，能夠使用getInputStream
            conn.setDoInput(true);
            //允許可調用getOutPutStream()從服務獲得字節輸出流
            conn.setDoOutput(true);

            //資料的目的與來源，銜接兩者為串流物件
            //若要將資料從來源取出，使用輸入串流
            //若將資料傳入目的地則使用輸出串流

            //連接 向伺服器進行輸出
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            //刷新寫入的outputstream
            writer.flush();
            //關閉
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            //拿到請求結果
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                //獲取伺服器返回並進行讀取
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = br.readLine();
                //br.close();
            } else {
                response =String.valueOf(responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        //與String差別在於內存的消耗
        //他不能+但能使用append 及remove
        //在操作時不需重新實例化，節省內存
        //https://zhidao.baidu.com/question/56752235.html
        StringBuilder result = new StringBuilder();
        boolean first = true;
        //entrySet()取得所有資料
        //每個entry 是每個key與對應value的重新包裝
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                //數據間用&隔開
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        Log.e("a:::::::::::::::", result.toString());
        return result.toString();
    }
}
