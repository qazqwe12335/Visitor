package com.example.visitor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataHolder> {

    public static String DELETE_URL = "http://192.168.0.17:8000/app_guest_face_feature/delete";
    public static String EDIT_URL = "http://192.168.0.17:8000/app_guest_face_feature/edit";

    private String edit_json;
    private Bitmap bmp, fix_bit;

    private Context mcontext;
    private ArrayList<DataSet> mdataSetArrayList;

    public DataAdapter(Context context, ArrayList<DataSet> dataSetArrayList) {
        mcontext = context;
        mdataSetArrayList = dataSetArrayList;
    }

    @NonNull
    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.jsonrow, parent, false);
        return new DataHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DataHolder holder, int position) {
        DataSet dataSet = mdataSetArrayList.get(position);

        String name = dataSet.getName();
        String phone = dataSet.getPhone();
        String company = dataSet.getCompany();
        String gate = dataSet.getGate();
        Bitmap bmp = dataSet.getBbmp();
        String cardid = dataSet.getCardId();
        String id = dataSet.getId();

        boolean how = false;
        holder.gate_text.setText("門號 : " + gate);

        for (int i = 0; i < gate.length(); i++) {
            try {
                if (Integer.valueOf(gate.substring(i, i + 1)) != 0) {
                    int k = i + 1;
                    if (how == true) {
                        holder.gate_text.append(", " + k);
                    } else {
                        holder.gate_text.setText("門號 : " + k);
                        how = true;
                    }
                }
            } catch (NumberFormatException e) {

            }
        }

        holder.name_text.setText("姓名 : " + name);
        holder.phone_text.setText("電話 : " + phone);
        holder.company_text.setText("公司 : " + company);
        //holder.gate_text.setText("  門 號：" + gate);
        holder.card_id.setText("Card ID : " + cardid);
        holder.id_text.setText(id);


        if (bmp != null) {
            holder.ivimg_path.setImageBitmap(bmp);
        } else {
            holder.ivimg_path.setImageResource(R.drawable.error);
        }

        //delete deleter = new delete();
        //deleter.execute(id);

        holder.setDataClickListener(new OnDataFixListener() {
            @Override
            public void OnDataFixClick(View v, int position) {
                String gid = mdataSetArrayList.get(position).getId();

                new edit().execute(gid);
/*
                Intent fix_intent = new Intent(mcontext,FixActivity.class);

                fix_intent.putExtra("fix_gid",gid);
                mcontext.startActivity(fix_intent);
                */
               /* String gname = mdataSetArrayList.get(position).getName();
                String gphone = mdataSetArrayList.get(position).getPhone();
                String gcompany = mdataSetArrayList.get(position).getCompany();
                String gcardid = mdataSetArrayList.get(position).getCardId();
                String gid = mdataSetArrayList.get(position).getId();
                BitmapDrawable gbmp = (BitmapDrawable) holder.ivimg_path.getDrawable();

                Bitmap gbitmap = gbmp.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                gbitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();

                Intent fix_intent = new Intent(mcontext, FixActivity.class);
                fix_intent.putExtra("fix_gid", gid);
                fix_intent.putExtra("fix_gname", gname);
                fix_intent.putExtra("fix_gphone", gphone);
                fix_intent.putExtra("fix_gcompany", gcompany);
                fix_intent.putExtra("fix_gcardid", gcardid);
                fix_intent.putExtra("fix_gpic", bytes);
                mcontext.startActivity(fix_intent);

                Log.e("intent:::::::::::::::::", String.valueOf(stream));
                */
            }

            @Override
            public void OnDeleteClick(View v, int position) {
                String deleteid = mdataSetArrayList.get(position).getId();
                new delete().execute(deleteid);
                //找到第幾筆，刪除更新
                mdataSetArrayList.remove(position);
                DataAdapter.this.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mdataSetArrayList.size();
    }

    //holder為表單的元件
    public class DataHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name_text;
        public TextView phone_text;
        public TextView company_text;
        public TextView gate_text;
        public ImageView ivimg_path;
        public TextView card_id;
        public TextView id_text;
        public Button fix_btn;
        public Button delete_btn;
        OnDataFixListener onDataFixListener;
        OnDataDeleteListener onDataDeleteListener;

        DataHolder(@NonNull View itemView) {
            super(itemView);
            id_text = itemView.findViewById(R.id.id_text);
            name_text = itemView.findViewById(R.id.json_name);
            phone_text = itemView.findViewById(R.id.json_phone);
            company_text = itemView.findViewById(R.id.json_company);
            gate_text = itemView.findViewById(R.id.json_gate);
            ivimg_path = itemView.findViewById(R.id.json_pic);
            card_id = itemView.findViewById(R.id.json_cardid);
            fix_btn = itemView.findViewById(R.id.fix_btn);
            delete_btn = itemView.findViewById(R.id.delete_btn);
            fix_btn.setOnClickListener(this);
            delete_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fix_btn:
                    this.onDataFixListener.OnDataFixClick(view, getLayoutPosition());
                    break;
                case R.id.delete_btn:
                    this.onDataFixListener.OnDeleteClick(view, getLayoutPosition());
                    break;
            }
        }

        //onclick
        public void setDataClickListener(OnDataFixListener ic) {
            this.onDataFixListener = ic;
        }

        public void setDataDeleteClickListener(OnDataDeleteListener idd) {
            this.onDataDeleteListener = idd;
        }
    }

    public class delete extends AsyncTask<String, Void, String> {
        int pos;
        RequireHandler rh = new RequireHandler();

        @Override
        protected String doInBackground(String... parmas) {
            String di = parmas[0];
            HashMap<String, String> data = new HashMap<>();
            data.put("id", di);
            String result = rh.postRequest(DELETE_URL, data);

            return result;
        }
    }

    public class edit extends AsyncTask<String, Void, String> {
        int pos;
        RequireHandler rh = new RequireHandler();

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            edit_json = s;

            Intent fix_intent = new Intent(mcontext, FixActivity.class);
            fix_intent.putExtra("fix_gid", edit_json);
            mcontext.startActivity(fix_intent);
        }

        @Override
        protected String doInBackground(String... parmas) {
            String di = parmas[0];
            HashMap<String, String> data = new HashMap<>();
            data.put("id", di);
            String result = rh.postRequest(EDIT_URL, data);

            return result;
        }
    }

    public interface OnDataFixListener {
        void OnDataFixClick(View v, int position);

        void OnDeleteClick(View v, int position);
    }

    public interface OnDataDeleteListener {
        void OnDataDeleteClick(View v, int position);
    }
}