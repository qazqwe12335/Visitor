package com.example.visitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class MGMTActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, RadioGroup.OnCheckedChangeListener {

    public static String UPLOAD_URL = "http://192.168.0.17:8000/app_face_feature/store";
    public static final String UPLOAD_KEY = "description";

    private static String Errorname = "姓名錯誤",
            Errorphone = "電話長度錯誤",
            Errorusername = "使用者帳號輸入錯誤",
            Erroruserpassword = "使用者密碼輸入錯誤",
            Erroremail = "信箱地址輸入錯誤",
            Errorpassword = "信箱密碼輸入錯誤",
            Errordate = "請選擇時間",
            Errorcompany = "公司名稱錯誤",
            Erroraccount = "Account 輸入錯誤或未達十碼",
            Errorfilepath = "照片錯誤或為選取",
            ErrorGate = "請選擇至少一道門",
            Errorcardid = "card _ID 必須十碼";

    ProgressDialog loading;

    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_IMAGE_CAPTURE = 101;
    private String ImageLocation = "", filelastname, name, userphone, usercompany, usercardid, account,
            username = "eason_wang@orbit.com.tw",
            user_password = "solar2018orbit",
            email, password;

    private int gatetotal, position = 1;

    private TextView tname, tphone, tcompany, tpic, tcardid, etdate;
    private EditText etname, etphone, etcompany, etpic, etcardid, etusername, etuserpassword, etemail, etpassword, etaccount;

    private RadioGroup radioGroup;

    private CheckBox ckb1, ckb2, ckb3, ckb4, ckb5, ckb6, ckb7, ckb8, ckb9;
    ImageView ing;

    LinearLayout companylayout;

    private Uri filePath;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //設定不旋轉畫面
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        //設定直向
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_mgmt);

        init();
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
                ing.setImageBitmap(bitmap);
                etpic.setText(cleansecfilename(getFileName(filePath)));
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
            etpic.setText(String.valueOf(filePath));
            //Toast.makeText(this, ImageLocation, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, getsecfilename(String.valueOf(filePath)), Toast.LENGTH_SHORT).show();
            //imageView.setImageBitmap(bitmap);
            ing.setImageBitmap(bitmap);
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

    //獲取時間
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

        if (view.getId() == R.id.upload_btn) {
            if (etname.getText().length() <= 0) {
                Toast.makeText(this, Errorname, Toast.LENGTH_SHORT).show();
                return;
            }
            /*if (etusername.getText().length() <= 0) {
                Toast.makeText(this, Errorusername, Toast.LENGTH_SHORT).show();
                return;
            }
            if (etuserpassword.getText().length() <= 0) {
                Toast.makeText(this, Erroruserpassword, Toast.LENGTH_SHORT).show();
                return;
            }*/
            if (etemail.getText().length() <= 0) {
                Toast.makeText(this, Erroremail, Toast.LENGTH_SHORT).show();
                return;
            }
            if (etpassword.getText().length() <= 0) {
                Toast.makeText(this, Errorpassword, Toast.LENGTH_SHORT).show();
                return;
            }
            if (position == 1) {
                if (etcompany.getText().length() <= 0) {
                    Toast.makeText(this, Errorcompany, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (etdate.getText().length() < 1) {
                Toast.makeText(this, Errordate, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!etphone.getText().toString().equals("")) {
                if (etphone.getText().length() != 10) {
                    Toast.makeText(this, Errorphone, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!etaccount.getText().toString().equals("")) {
                if (etaccount.getText().length() != 10) {
                    Toast.makeText(this, Erroraccount, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!etcardid.getText().toString().equals("")) {
                if (etcardid.getText().length() != 10) {
                    Toast.makeText(this, Errorcardid, Toast.LENGTH_SHORT).show();
                    return;
                }
            /*} else if (filePath == null) {
                Toast.makeText(this, Errorfilepath, Toast.LENGTH_LONG).show();
            } else if (GetTotal() == 0) {
                Toast.makeText(this, ErrorGate, Toast.LENGTH_SHORT).show();
                */
            }
            uploadImage();
        }
        if (view.getId() == R.id.mgmt_inquire_btn) {
            Intent i = new Intent(this, InquireActivity.class);
            startActivity(i);
            finish();
        }
        if (view.getId() == R.id.pic_camera_btn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Opencamera();
            }
        }
        if (view.getId() == R.id.pic_add_btn) {
            showFileChooser();
        }
        if (view.getId() == R.id.sign_out) {
            Intent signout_intent = new Intent(MGMTActivity.this, MainActivity.class);
            startActivity(signout_intent);
            finish();
        }
        if (view.getId() == R.id.mgmt_et_date) {
            show_datepicker_dialog();
        }
    }

    private void show_datepicker_dialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void init() {
        tname = findViewById(R.id.mgmtname);
        tphone = findViewById(R.id.mgmtphone);
        tcompany = findViewById(R.id.mgmtcompany);
        tpic = findViewById(R.id.mgmtpic);
        tcardid = findViewById(R.id.mgmtcardid);
        etname = findViewById(R.id.mgmt_et_name);
        etphone = findViewById(R.id.mgmt_et_phone);
        etcompany = findViewById(R.id.mgmt_et_company);
        etpic = findViewById(R.id.mgmt_et_pic);
        etcardid = findViewById(R.id.mgmt_et_cardid);
        etdate = findViewById(R.id.mgmt_et_date);

        //etusername = findViewById(R.id.mgmt_et_username);
        //etuserpassword = findViewById(R.id.mgmt_et_user_password);
        etemail = findViewById(R.id.mgmt_et_email);
        etpassword = findViewById(R.id.mgmt_et_password);
        etaccount = findViewById(R.id.mgmt_et_account);

        companylayout = findViewById(R.id.company_layout);

        radioGroup = findViewById(R.id.position_radiog);
        radioGroup.setOnCheckedChangeListener(this);

        ing = findViewById(R.id.table_ing);

        ckb1 = findViewById(R.id.ck1);
        ckb2 = findViewById(R.id.ck2);
        ckb3 = findViewById(R.id.ck3);
        ckb4 = findViewById(R.id.ck4);
        ckb5 = findViewById(R.id.ck5);
        ckb6 = findViewById(R.id.ck6);
        ckb7 = findViewById(R.id.ck7);
        ckb8 = findViewById(R.id.ck8);
        ckb9 = findViewById(R.id.ck9);

        clean_input();
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
        name = etname.getText().toString();
        userphone = etphone.getText().toString();

        if (etcompany.getText().toString() == null) {
            usercompany = null;
        } else {
            usercompany = etcompany.getText().toString();
        }
        //usercompany = etcompany.getText().toString();
        account = etaccount.getText().toString();

        email = etemail.getText().toString();
        password = etpassword.getText().toString();

        if (etcardid.getText().toString() == null) {
            usercardid = null;
        } else {
            usercardid = etcardid.getText().toString();
        }

        class UploadImage extends AsyncTask<Bitmap, Void, String> {
            RequireHandler rh = new RequireHandler();
            String ddate = etdate.getText().toString();
            String ggate_total = String.valueOf(GetTotal());

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show
                        (MGMTActivity.this, "上傳照片", "上傳中 ...", true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (s.equals("\"success\"")) {
                    clean_input();
                }
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
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
                data.put(UPLOAD_KEY, uploadImage);
                data.put("name", name);
                data.put("phone", userphone);
                data.put("company", usercompany);
                data.put("due_date", ddate);
                data.put("account", account);
                data.put("card_id", usercardid);
                data.put("gate", ggate_total);

                data.put("email", email);
                data.put("password", password);
                data.put("username", username);
                data.put("user_password", user_password);
                data.put("type", String.valueOf(position));

                String result = rh.postRequest(UPLOAD_URL, data);
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
        etaccount.setText("");
        etemail.setText("");
        etpassword.setText("");
        etdate.setText("");
        etname.setText("");
        etcompany.setText("");
        etphone.setText("");
        etcardid.setText("");
        etpic.setText("");
        ing.setImageBitmap(null);
        bitmap = null;
        filePath = null;
    }

    boolean[] a;

    private int GetTotal() {

        gatetotal = 0;
        boolean[] arr = new boolean[10];
        arr[0] = ckb1.isChecked();
        arr[1] = ckb2.isChecked();
        arr[2] = ckb3.isChecked();
        arr[3] = ckb4.isChecked();
        arr[4] = ckb5.isChecked();
        arr[5] = ckb6.isChecked();
        arr[6] = ckb7.isChecked();
        arr[7] = ckb8.isChecked();
        arr[8] = ckb9.isChecked();

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

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        String enddate = i + "/" + i1 + "/" + i2;
        etdate.setText(enddate);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (radioGroup.getCheckedRadioButtonId() == R.id.mgmt_radio_visitor) {
            position = 1;
            companylayout.setVisibility(View.VISIBLE);
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.mgmt_radio_work) {
            position = 0;
            etcompany.setText("");
            companylayout.setVisibility(View.GONE);
        }
    }
}
