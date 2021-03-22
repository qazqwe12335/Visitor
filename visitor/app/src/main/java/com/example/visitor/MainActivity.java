package com.example.visitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText password,username;
    String name,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.main_username);
        password = findViewById(R.id.main_password);
    }
    public void login(View view){
        name = username.getText().toString();
        pass = password.getText().toString();

        Intent loginintent = new Intent(MainActivity.this,InquireActivity.class);

        if (!name.equals("lhu")){
            Toast.makeText(this, "帳號錯誤", Toast.LENGTH_SHORT).show();
        }else if (!pass.equals("lhu")){
            Toast.makeText(this, "密碼錯誤", Toast.LENGTH_SHORT).show();
        }else{
            startActivity(loginintent);
            finish();
        }
    }
}
