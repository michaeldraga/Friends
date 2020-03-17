package com.mike.newfriends;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mike.loctest.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AddFriendsActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button _submitButton;

    private static final String url = "http://82.193.225.50:4000";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends);

        sharedPreferences = getPreferences(0);
        editor = sharedPreferences.edit();

        Intent data = getIntent();
        editor.putString("token", data.getStringExtra("token"));
        editor.commit();

        _submitButton = findViewById(R.id.register_password2);
        this._submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
    }

    public void sendRequest() {
        TextView email = findViewById(R.id.request_email);
        String _email = email.getText().toString();
        sendPost("{\n" +
                "\t\"reqUserMail\": \"" + _email + "\"\n" +
                "}", url + "/friends/add");
    }

    public void sendPost(String data, String url) {
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(1, url, new Response.Listener<String>() {
            public void onResponse(String response) {
                if (response == "Error in adding") {
                    Toast.makeText(getApplicationContext(), "Please provide a valid email address", Toast.LENGTH_LONG);
                    return;
                }
                try {
                    if (sharedPreferences.contains("token"))
                        editor.remove("token");
                    String token = new JSONObject(response).getString("token");
                    String msg = new JSONObject(response).getString("message");
                    editor.putString("token", token);
                    editor.commit();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("token", token);
                    setResult(-1, resultIntent);
                    finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            public byte[] getBody() throws AuthFailureError {
                byte[] bArr = null;
                try {
                    if (savedata != null) {
                        bArr = savedata.getBytes("utf-8");
                    }
                    return bArr;
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            }

            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                String str = "token";
                params.put(str, sharedPreferences.getString(str, ""));
                return params;
            }
        };
        this.requestQueue.add(r0);
    }
}
