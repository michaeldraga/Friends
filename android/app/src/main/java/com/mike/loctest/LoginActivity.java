package com.mike.loctest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_SIGNUP = 0;
    private static final String TAG = "LoginActivity";
    EditText _emailText;
    Button _loginButton;
    EditText _passwordText;
    TextView _signupLink;
    /* access modifiers changed from: private */
    public Editor editor;
    private RequestQueue requestQueue;
    /* access modifiers changed from: private */
    public SharedPreferences sharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0485R.layout.activity_login);
        ButterKnife.bind((Activity) this);
        this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        this._emailText = (EditText) findViewById(C0485R.C0487id.input_email);
        this._passwordText = (EditText) findViewById(C0485R.C0487id.input_password);
        this._loginButton = (Button) findViewById(C0485R.C0487id.btn_login);
        this._signupLink = (TextView) findViewById(C0485R.C0487id.link_signup);
        this._loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LoginActivity.this.login();
            }
        });
        this._signupLink.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LoginActivity.this.startActivityForResult(new Intent(LoginActivity.this.getApplicationContext(), SignupActivity.class), 0);
                LoginActivity.this.finish();
                LoginActivity.this.overridePendingTransition(C0485R.anim.push_left_in, C0485R.anim.push_left_out);
            }
        });
        SharedPreferences preferences = getPreferences(0);
        this.sharedPreferences = preferences;
        this.editor = preferences.edit();
    }

    public void login() {
        Log.d(TAG, "Login");
        if (!validate()) {
            onLoginFailed();
            return;
        }
        this._loginButton.setEnabled(false);
        ProgressDialog progressDialog = new ProgressDialog(this, C0485R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        sendLogin(this._emailText.getText().toString(), this._passwordText.getText().toString(), progressDialog);
    }

    public void sendLogin(final String email, String password, final ProgressDialog pd) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\t\"email\": \"");
        sb.append(email);
        sb.append("\",\n\t\"password\": \"");
        sb.append(password);
        sb.append("\"\n}");
        final String savedata = sb.toString();
        String str = "http://192.168.178.76:4000/user/login";
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r2 = new StringRequest(1, "http://192.168.178.76:4000/user/login", new Listener<String>() {
            public void onResponse(String response) {
                String str = "token";
                try {
                    JSONObject objres = new JSONObject(response);
                    Toast.makeText(LoginActivity.this.getApplicationContext(), objres.toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this.getApplicationContext(), "goodyeet", Toast.LENGTH_SHORT).show();
                    String token = new JSONObject(objres.toString()).getString(str);
                    if (LoginActivity.this.sharedPreferences.contains(str)) {
                        Toast.makeText(LoginActivity.this.getApplicationContext(), "fuck you", Toast.LENGTH_SHORT);
                        LoginActivity.this.editor.remove(str);
                    }
                    LoginActivity.this.editor.putString(NotificationCompat.CATEGORY_EMAIL, email);
                    LoginActivity.this.editor.putString(str, token);
                    LoginActivity.this.editor.commit();
                    LoginActivity.this.onLoginSuccess(token, email);
                    pd.dismiss();
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this.getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this.getApplicationContext(), "badyeet", Toast.LENGTH_SHORT).show();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                LoginActivity.this.onLoginFailed();
                pd.dismiss();
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
        };
        this.requestQueue.add(r2);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == -1) {
            finish();
        }
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String token, String email) {
        String str = "token";
        if (this.sharedPreferences.contains(str)) {
            this.editor.remove(str);
        }
        SharedPreferences sharedPreferences2 = this.sharedPreferences;
        String str2 = NotificationCompat.CATEGORY_EMAIL;
        if (sharedPreferences2.contains(str2)) {
            this.editor.remove(str2);
        }
        this.editor.putString(str, token);
        this.editor.putString(str2, email);
        this.editor.commit();
        this._loginButton.setEnabled(true);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(str, token);
        resultIntent.putExtra(str2, email);
        setResult(-1, resultIntent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_SHORT).show();
        this._loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String email = this._emailText.getText().toString();
        String password = this._passwordText.getText().toString();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this._emailText.setError("enter a valid email address");
            valid = false;
        } else {
            this._emailText.setError(null);
        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            this._passwordText.setError("between 4 and 10 alphanumeric characters");
            return false;
        }
        this._passwordText.setError(null);
        return valid;
    }
}
