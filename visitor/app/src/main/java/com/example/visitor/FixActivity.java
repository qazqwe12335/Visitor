package com.example.visitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class FixActivity extends AppCompatActivity {

    public static String FIX_URL = "http://192.168.0.17:8000/app_guest_face_feature/update";
    public static String IMG_URL = "http://192.168.0.17:8000/app_guest_face_feature/img";
    public static final String UPLOAD_KEY = "picture";

    private static String Errorname = "姓名錯誤",
            Errorphone = "電話未填寫或長度錯誤",
            Errorcompany = "公司名稱錯誤",
            Errorfilepath = "照片錯誤或為選取",
            ErrorGate = "請選擇至少一道門",
            Errorcardid = "card _ID 必須十碼";
    ;

    ProgressDialog loading;

    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_IMAGE_CAPTURE = 101;
    private String ImageLocation = "", filelastname, username, userphone, usercompany, usercard_id, userid, mid,gate_data,id;
    private int gatetotal;

    TextView fix_name, fix_phone, fix_company, fix_cardid, fix_pic, inid;
    private EditText fix_etname, fix_etphone, fix_etcompany, fix_etpic, fix_etcardid;
    private CheckBox fix_ckb1, fix_ckb2, fix_ckb3, fix_ckb4, fix_ckb5, fix_ckb6, fix_ckb7, fix_ckb8, fix_ckb9;

    private Uri filePath;
    private Bitmap bitmap = null,bbmpb = null;
    private ImageView fix_image;
    String get_adapterjson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //設定不旋轉畫面
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        //設定直向
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_fix);
        init();
        JsonToString(get_adapterjson);

        new img_fix().execute(id);
        //checkboxlister();
    }

    private void Opencamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        } else {
            //filePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            Intent CameraAPPintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (CameraAPPintent.resolveActivity(getPackageManager()) != null) {

                File photoFile = null;
                try {
                    photoFile = createfile();
                    if (!photoFile.getParentFile().exists()) {
                        photoFile.getParentFile().mkdir();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    filePath = FileProvider.getUriForFile(this,
                            "com.example.visitor.fileprovider",
                            photoFile);
                    CameraAPPintent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                    startActivityForResult(CameraAPPintent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File createfile() throws IOException {
        String picturefile = new SimpleDateFormat("yyyyMMddmmss").format(new Date());
        String picturename = "JPEG_" + picturefile + "_";
        //儲存外部空間
        File Dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!Dir.exists()) {
            Dir.mkdirs();
        }
        File imagefile = File.createTempFile(
                picturename,
                ".jpg",
                Dir
        );
        ImageLocation = imagefile.getAbsolutePath();
        return imagefile;
    }

    //照片選擇
    private void showFileChooser() {
        Intent PictureFileintent = new Intent();
        PictureFileintent.setType("image/*");
        PictureFileintent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(PictureFileintent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //startActivityForResult返回值  檔案選擇、拍照
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            int degree = getBitmapDegree(filePath.getPath());
            try {
                ImageDecoder.Source idsource = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    idsource = ImageDecoder.createSource(getContentResolver(), filePath);
                    bitmap = ImageDecoder.decodeBitmap(idsource);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                }
                bitmap = rotateBitmapByDegree(bitmap, degree);
                //bitmap = ImageDecoder.createSource(getContentResolver(),filePath);
                //imageView.setImageBitmap(bitmap);
                fix_image.setImageBitmap(bitmap);
                fix_etpic.setText(cleansecfilename(getFileName(filePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Toast.makeText(this, getsecfilename(String.valueOf(getFileName(filePath))), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, String.valueOf(getFileName(filePath)), Toast.LENGTH_SHORT).show();
            //showimg();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //galleraddphoto();
            try {
                int degree = getBitmapDegree(ImageLocation);

                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(filePath), null, null);
                bitmap = rotateBitmapByDegree(bitmap, degree);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fix_etpic.setText(String.valueOf(filePath));
            //Toast.makeText(this, ImageLocation, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, getsecfilename(String.valueOf(filePath)), Toast.LENGTH_SHORT).show();
            //imageView.setImageBitmap(bitmap);
            fix_image.setImageBitmap(bitmap);
            //Toast.makeText(this, ImageLocation, Toast.LENGTH_SHORT).show();
        }
    }


    //檢查副檔名
    public String getsecfilename(String name) {
        if ((name != null) && (name.length() > 0)) {
            int dot = name.lastIndexOf('.');
            if ((dot > -1) && (dot < name.length() - 1)) {
                return name.substring(dot, Integer.valueOf(name.length()));
            }
        }
        return name;
    }

    //獲取時間+3月
    public String gettime() {
        Calendar mCal = Calendar.getInstance();
        String dataformat = "yy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(dataformat);
        mCal.add(Calendar.MONTH, 3);
        String today = df.format(mCal.getTime());
        return today;
    }

    //移除附檔名
    public String cleansecfilename(String filesecname) {
        if ((filesecname != null) && (filesecname.length() > 0)) {
            int ddot = filesecname.lastIndexOf('.');
            if ((ddot > -1) && (ddot < (filesecname.length()))) {
                return filesecname.substring(0, ddot);
            }
        }
        return filesecname;
    }

    //取得檔名
    String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        /*
        取得最後一次斜線出現的地方，後面的字
         */
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //更新相簿
    public void galleraddphoto() {
        Intent mediaintent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(ImageLocation);
        Uri in = Uri.fromFile(f);
        mediaintent.setData(in);
        this.sendBroadcast(mediaintent);
    }

    //按鈕事件
    public void mgmt_btn(View view) {
        //Intent uploadintent = new Intent(MGMTActivity.this, InquireActivity.class);
        //startActivity(uploadintent);

        if (view.getId() == R.id.update_btn) {
            if (fix_etname.getText().length() <= 0) {
                Toast.makeText(this, Errorname, Toast.LENGTH_SHORT).show();
                return;
            }
            if (fix_etcompany.getText().length() <= 0) {
                Toast.makeText(this, Errorcompany, Toast.LENGTH_SHORT).show();
                return;

                //} else if (fix_etphone.getText().length() != 10) {
                //    Toast.makeText(this, Errorphone, Toast.LENGTH_SHORT).show();
                //} else if (filePath == null) {
                //    Toast.makeText(this, Errorfilepath, Toast.LENGTH_LONG).show();
                //} else if (GetTotal() == 0) {
                //    Toast.makeText(this, ErrorGate, Toast.LENGTH_SHORT).show();
            }
            if (!fix_etphone.getText().toString().equals("")) {
                if (fix_etphone.getText().length() != 10) {
                    Toast.makeText(this, Errorphone, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!fix_etcardid.getText().toString().equals("")) {
                if (fix_etcardid.getText().length() != 10) {
                    Toast.makeText(this, Errorcardid, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            uploadImage();
        }
        if (view.getId() == R.id.pic_camera_btn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Opencamera();
            }
        }
        if (view.getId() == R.id.pic_add_btn) {
            showFileChooser();
        }
    }


    private void init() {

        fix_etname = findViewById(R.id.mgmt_et_name);
        fix_etphone = findViewById(R.id.mgmt_et_phone);
        fix_etcompany = findViewById(R.id.mgmt_et_company);
        fix_etpic = findViewById(R.id.mgmt_et_pic);
        fix_etcardid = findViewById(R.id.mgmt_et_cardid);
        fix_image = findViewById(R.id.table_ing);
        inid = findViewById(R.id.inid);

        fix_ckb1 = findViewById(R.id.ck1);
        fix_ckb2 = findViewById(R.id.ck2);
        fix_ckb3 = findViewById(R.id.ck3);
        fix_ckb4 = findViewById(R.id.ck4);
        fix_ckb5 = findViewById(R.id.ck5);
        fix_ckb6 = findViewById(R.id.ck6);
        fix_ckb7 = findViewById(R.id.ck7);
        fix_ckb8 = findViewById(R.id.ck8);
        fix_ckb9 = findViewById(R.id.ck9);

        Intent get_adapterintent = getIntent();
        get_adapterjson = get_adapterintent.getStringExtra("fix_gid");

        //fix_image.setImageBitmap(bmp);

    }

    //壓縮照片(根據附檔名壓縮)
    public String getStringImage(Bitmap bmp) {
        if (bmp != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            filelastname = getsecfilename(String.valueOf(getFileName(filePath)));
            if (filelastname.equals(".jpg")) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            } else if (filelastname.equals(".png")) {
                bmp.compress(Bitmap.CompressFormat.PNG, 50, baos);
            } else if (filelastname.equals(".webp")) {
                bmp.compress(Bitmap.CompressFormat.WEBP, 50, baos);
            }
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            //Log.e("a",encodedImage);
            return encodedImage;
        }
        return "";
    }

    //上傳照片
    private void uploadImage() {
        username = fix_etname.getText().toString();
        userphone = fix_etphone.getText().toString();
        usercompany = fix_etcompany.getText().toString();

        if (fix_etcardid.getText().toString() == null) {
            usercard_id = null;
        } else {
            usercard_id = fix_etcardid.getText().toString();
        }

        class UploadImage extends AsyncTask<Bitmap, Void, String> {
            RequireHandler rh = new RequireHandler();
            String ggate_total = String.valueOf(GetTotal());

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show
                        (FixActivity.this, "訪客登記", "上傳中 ...", true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (s.equals("\"success\"")) {
                    Toast.makeText(FixActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(FixActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                ///@SuppressLint("WrongThread") String img = etpic.getText().toString();
                String uploadImage = null;
                /*if (img.equals("")){
                    uploadImage = null;
                }else {*/
                Bitmap bitmap = params[0];
                uploadImage = getStringImage(bitmap);
                //}
                //圖片存在image 檔名存在name
                HashMap<String, String> data = new HashMap<>();

                data.put("id", userid);
                data.put(UPLOAD_KEY, uploadImage);
                //data.put("account", Account);
                data.put("name", username);
                data.put("phone", userphone);
                data.put("company", usercompany);
                data.put("card_id", usercard_id);
                data.put("gate", ggate_total);


                String result = rh.postRequest(FIX_URL, data);
                //傳送
                return result;
            }
        }

        UploadImage ui = new UploadImage();
        //調用execute方法 發送GET POST請求，並回傳給HttpResponse
        //通過HttpResponse接口的getEntity方法取得伺服器回應的訊息,並進行相應的處理
        //以下為 AsyncTask.execute方法 ，傳入AsyncTask的參數
        ui.execute(bitmap);
    }

    private void clean_input() {
        fix_etname.setText("");
        fix_etcompany.setText("");
        fix_etphone.setText("");
        fix_etcardid.setText("");
        fix_etpic.setText("");
        fix_image.setImageBitmap(null);
        bitmap = null;
        filePath = null;
    }

    boolean[] a;

    private int GetTotal() {

        gatetotal = 0;
        boolean[] arr = new boolean[10];
        arr[0] = fix_ckb1.isChecked();
        arr[1] = fix_ckb2.isChecked();
        arr[2] = fix_ckb3.isChecked();
        arr[3] = fix_ckb4.isChecked();
        arr[4] = fix_ckb5.isChecked();
        arr[5] = fix_ckb6.isChecked();
        arr[6] = fix_ckb7.isChecked();
        arr[7] = fix_ckb8.isChecked();
        arr[8] = fix_ckb9.isChecked();

        for (int i = 0; i < 9; i++) {
            if (arr[i]) {
                gatetotal += Math.pow(2, i);
            }
        }
        return gatetotal;
    }

    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 從指定路徑下讀取圖片，並獲取其EXIF資訊
            ExifInterface exifInterface = new ExifInterface(path);
            // 獲取圖片的旋轉資訊
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根據旋轉角度，生成旋轉矩陣
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 將原始圖片按照旋轉矩陣進行旋轉，並得到新的圖片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    public void JsonToString(String getjson) {
        try {
            JSONObject jsonobj = new JSONObject(getjson);
            JSONArray jsonArray = jsonobj.getJSONArray("data");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject data = jsonArray.getJSONObject(i);

                id = data.getString("id");
                String name = data.getString("name");
                String phone = data.getString("phone");
                String company = data.getString("company");
                gate_data = data.getString("gate");
                String cardid = data.getString("card_id");

                //inid.setText(gate);

                if (name.equals("null")) {
                    name = "";
                }
                if (phone.equals("null")) {
                    phone = "";
                }
                if (company.equals("null")) {
                    company = "";
                }
                if (cardid.equals("null")) {
                    cardid = "";
                }
                fix_etname.setText(name);
                fix_etphone.setText(phone);
                fix_etcompany.setText(company);
                fix_etcardid.setText(cardid);
                userid = id;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String reverseString(String ori) {
        StringBuilder builder = new StringBuilder();
        builder.append(ori);
        return builder.reverse().toString();
    }

    public void checkboxlister(){
        String gate = null;
        String converttest = Integer.toBinaryString(Integer.valueOf(gate_data));
        gate = reverseString(converttest);

        CheckBox[] jsonarr = new CheckBox[10];
        jsonarr[0] = fix_ckb1;
        jsonarr[1] = fix_ckb2;
        jsonarr[2] = fix_ckb3;
        jsonarr[3] = fix_ckb4;
        jsonarr[4] = fix_ckb5;
        jsonarr[5] = fix_ckb6;
        jsonarr[6] = fix_ckb7;
        jsonarr[7] = fix_ckb8;
        jsonarr[8] = fix_ckb9;

        for (int binaryi = 0; binaryi < gate.length(); binaryi++) {
            try {
                if (Integer.valueOf(gate.substring(binaryi, binaryi + 1)) != 0) {
                    jsonarr[binaryi].setChecked(true);
                }
            }catch (NumberFormatException e){

            }
        }
    }

    public class img_fix extends AsyncTask<String, Void, String> {
        int pos;
        RequireHandler rh = new RequireHandler();

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(FixActivity.this, s, Toast.LENGTH_SHORT).show();
            String imggg = null;

            try {
                JSONObject jsonobjc = new JSONObject(s);
                JSONArray jsonArrayc = jsonobjc.getJSONArray("data");
                for (int i = 0; i < jsonArrayc.length(); i++) {
                    JSONObject data = jsonArrayc.getJSONObject(i);
                    imggg = data.getString("description");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            byte[] bmpString = Base64.decode(imggg.toString(), Base64.NO_WRAP);
            bbmpb = BitmapFactory.decodeByteArray(bmpString, 0, bmpString.length);

            fix_image.setImageBitmap(bbmpb);
        }

        @Override
        protected String doInBackground(String... parmas) {
            String di = parmas[0];
            HashMap<String, String> data = new HashMap<>();
            data.put("id", di);
            String result = rh.postRequest(IMG_URL, data);

            return result;
        }
    }
}
