package com.example.visitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class InquireActivity extends AppCompatActivity {

    public static String DELETE_URL = "http://192.168.0.17:8000/app_guest_face_feature/store";

    private RecyclerView recycler;
    private RequestQueue mqueue;
    private DataAdapter mDataAdapter;
    private ArrayList<DataSet> mDataSetArrayList;

    private TextView Inquire_tatal;
    private Button updatebtn;
    private Drawable icon;
    final ColorDrawable background = new ColorDrawable(Color.RED);

    public static String BASE_URL = "http://192.168.0.17:8000/app_guest_face_feature/show";

    private TextView jsonrow_id;

    //按鈕判斷是可再次更新
    boolean runOK = false;

    public InquireActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquire);
        init();
        initgetdata();
    }

    private void init() {
        jsonrow_id = findViewById(R.id.id_text);
        updatebtn = findViewById(R.id.update_btn);
        Inquire_tatal = findViewById(R.id.total_text);
        recycler = findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);

        mDataSetArrayList = new ArrayList<>();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        once();

        //new ItemTouchHelper(itemtouchhelper).attachToRecyclerView(recycler);
    }

    private void initgetdata() {
        mqueue = volleySingleton.getInstance(this).getrequestqueue();
        jsonParse();
    }

    public void jsonparse_btn(View view) {

        Long currentTime = Calendar.getInstance().getTimeInMillis();
        long lasttime = 0;
        int fasttime = 5000;

        if (currentTime - lasttime <= fasttime) {
            Toast.makeText(this, "too fast", Toast.LENGTH_SHORT).show();
        }

        updatebtn.setEnabled(false);
        try {
            if (runOK == true) {
                runOK = false;

                once();
                //mDataSetArrayList.clear();
                //mDataAdapter.notifyDataSetChanged();
                jsonParse();
            } else {
                Toast.makeText(this, "操作頻繁", Toast.LENGTH_SHORT).show();
            }
        } catch (AndroidRuntimeException e) {
            Log.e("TAG", e.toString());
        }
    }

    private void jsonParse() {
        mDataSetArrayList.clear();
        mDataAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BASE_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObj = new JSONObject(String.valueOf(response));
                            int Total = jsonObj.getInt("recordsTotal");

                            JSONArray jsonArray = response.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject data = jsonArray.getJSONObject(i);

                                String id = data.getString("id");
                                String name = data.getString("name");
                                String phone = data.getString("phone");
                                String company = data.getString("company");
                                String gate_data = data.getString("gate");
                                String img_path = data.getString("description");
                                String cardid = data.getString("card_id");

                                String gate = null;
                                if (gate_data == "null" || gate_data == "0" || gate_data == null) {
                                    gate = "NO DATA";
                                } else {
                                    String converttest = Integer.toBinaryString(Integer.valueOf(gate_data));
                                    gate = reverseString(converttest);
                                }

                                Bitmap bmp = null;
                                try {
                                    byte[] bmpString = Base64.decode(img_path.toString(), Base64.NO_WRAP);
                                    bmp = BitmapFactory.decodeByteArray(bmpString, 0, bmpString.length);
                                } catch (IllegalArgumentException e) {
                                    bmp = null;
                                    Log.e("AAAAAA", e.toString());
                                }

                                mDataSetArrayList.add(new DataSet(name, phone, company, gate, bmp, cardid, id));
                            }
                            Inquire_tatal.setText("共有 " + Total + "筆資料");
                            mDataAdapter = new DataAdapter(InquireActivity.this, mDataSetArrayList);
                            recycler.setAdapter(mDataAdapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(InquireActivity.this, "JSON 資料錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                mqueue.add(request);

                runOK = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updatebtn.setEnabled(true);
                    }
                });
            }
        }).start();
    }

    /*@Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b == true) {
            Toast.makeText(this, "aaaaaaaaaa", Toast.LENGTH_SHORT).show();
        }
    }*/

   /*//滑動移動監聽
    ItemTouchHelper.SimpleCallback itemtouchhelper =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                    new AlertDialog.Builder(viewHolder.itemView.getContext())
                            .setTitle("Confirm to delete?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    //delete ui = new delete();
                                    //ui.execute();
                                    mDataSetArrayList.remove(viewHolder.getAdapterPosition());
                                    mDataAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mDataAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                }
                            })
                            .create()
                            .show();
                }
            };
*/
    private void once() {
        String name = "", phone = "", company = "", gate = "", cardid = "", id = "";
        Bitmap bmp = null;
        mDataSetArrayList.add(new DataSet(name, phone, company, gate, bmp, cardid, id));
        mDataAdapter = new DataAdapter(InquireActivity.this, mDataSetArrayList);
        recycler.setAdapter(mDataAdapter);
    }

    private String reverseString(String ori) {
        StringBuilder builder = new StringBuilder();
        builder.append(ori);
        return builder.reverse().toString();
    }

    public void finish_btn(View view) {
        Intent mgmt_intent = new Intent(this,MGMTActivity.class);
        startActivity(mgmt_intent);
    }

    /*public class delete extends AsyncTask<Void, Void, String> {
        int pos;
        RequireHandler rh = new RequireHandler();

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> data = new HashMap<>();
            data.put("id", /id);
            String result = rh.postRequest(DELETE_URL, data);

            return result;
        }
    }*/
/*
    @Override
    protected void onRestart() {
        super.onRestart();
        jsonParse();
    }
    */
}