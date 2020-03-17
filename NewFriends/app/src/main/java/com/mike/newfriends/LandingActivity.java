package com.mike.newfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mike.loctest.R;

public class LandingActivity extends AppCompatActivity {
    private Button _loginButton;
    private Button _registerButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);
        this._loginButton = (Button) findViewById(R.id.landing_login_button);
        this._registerButton = (Button) findViewById(R.id.landing_register_button);

        this._loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 0);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        this._registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), SignupActivity.class), 1);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("token", data.getStringExtra(("token")));
            setResult(-1, resultIntent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }
}
