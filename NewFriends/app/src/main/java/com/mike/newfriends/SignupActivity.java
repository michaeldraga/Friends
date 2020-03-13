package com.mike.newfriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mike.loctest.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    EditText _emailText;
    TextView _loginLink;
    EditText _nameText;
    EditText _passwordText;
    EditText _reEnterPasswordText;
    Button _signupButton;
    /* access modifiers changed from: private */
    public Editor editor;
    private RequestQueue requestQueue;
    /* access modifiers changed from: private */
    public SharedPreferences sharedPreferences;

    private static final String url = "http://82.193.225.50:4000";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_signup);
        this._nameText = (EditText) findViewById(R.id.input_name);
        this._emailText = (EditText) findViewById(R.id.input_email);
        this._passwordText = (EditText) findViewById(R.id.input_password);
        this._reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        this._signupButton = (Button) findViewById(R.id.btn_signup);
        this._loginLink = (TextView) findViewById(R.id.link_login);
        this._signupButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignupActivity.this.signup();
            }
        });
        this._loginLink.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignupActivity.this.startActivity(new Intent(SignupActivity.this.getApplicationContext(), LoginActivity.class));
                SignupActivity.this.finish();
                SignupActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        SharedPreferences preferences = getPreferences(0);
        this.sharedPreferences = preferences;
        this.editor = preferences.edit();
    }

    public void sendSignup(String data, final ProgressDialog pd) {
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(1, url + "/user/signup", new Listener<String>() {
            public void onResponse(String response) {
                String str = "token";
                try {
                    String token = new JSONObject(new JSONObject(response).toString()).getString(str);
                    if (SignupActivity.this.sharedPreferences.contains(str)) {
                        Toast.makeText(SignupActivity.this.getApplicationContext(), "fuck you", Toast.LENGTH_SHORT);
                        SignupActivity.this.editor.remove(str);
                    }
                    SignupActivity.this.editor.putString(str, token);
                    SignupActivity.this.editor.commit();
                    SignupActivity.this.onSignupSuccess();
                    pd.dismiss();
                } catch (JSONException e) {
                    Toast.makeText(SignupActivity.this.getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                    Toast.makeText(SignupActivity.this.getApplicationContext(), "badyeet", Toast.LENGTH_SHORT).show();
                    SignupActivity.this.onSignupFailed();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SignupActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
        this.requestQueue.add(r0);
    }

    public void signup() {
        Log.d("TAG", "Signup");
        if (!validate()) {
            onSignupFailed();
            return;
        }
        this._signupButton.setEnabled(false);
        ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        String name = this._nameText.getText().toString();
        String email = this._emailText.getText().toString();
        String password = this._passwordText.getText().toString();
        String obj = this._reEnterPasswordText.getText().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\t\"username\": \"");
        sb.append(name);
        sb.append("\",\n\t\"email\": \"");
        sb.append(email);
        sb.append("\",\n\t\"password\": \"");
        sb.append(password);
        sb.append("\"\n}");
        sendSignup(sb.toString(), progressDialog);
    }

    public void onSignupSuccess() {
        this._signupButton.setEnabled(true);
        setResult(-1, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failes", Toast.LENGTH_SHORT).show();
        this._signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String name = this._nameText.getText().toString();
        String email = this._emailText.getText().toString();
        String password = this._passwordText.getText().toString();
        String reEnterPassword = this._reEnterPasswordText.getText().toString();
        if (name.isEmpty() || name.length() < 3) {
            this._nameText.setError("at least 3 characters");
            valid = false;
        } else {
            this._nameText.setError(null);
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this._emailText.setError("enter a valid email address");
            valid = false;
        } else {
            this._emailText.setError(null);
        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            this._passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            this._passwordText.setError(null);
        }
        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !reEnterPassword.equals(password)) {
            this._reEnterPasswordText.setError("Password Do not match");
            return false;
        }
        this._reEnterPasswordText.setError(null);
        return valid;
    }
}
