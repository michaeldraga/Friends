package com.example.maths;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText edit;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit = findViewById(R.id.editText);
        tv = findViewById(R.id.hallo);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = (int) Math.pow(Integer.parseInt(edit.getText().toString()), 2);
                tv.setText(Integer.toString(result));
            }
        });
    }
}
