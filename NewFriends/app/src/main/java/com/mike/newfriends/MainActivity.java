package com.mike.newfriends;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

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
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final int FIVE_SECONDS = 5000;
    private static final int REQUEST_LOGIN = 1;
    public static final String locationProvider = "network";
    /* access modifiers changed from: private */
    public Editor editor;
    Location lastKnownLocation;
    LocationListener locationListener;
    LocationManager locationManager;
    private RequestQueue requestQueue = null;
    /* access modifiers changed from: private */
    public SharedPreferences sharedPreferences;
    private static final String url = "http://82.193.225.50:4000";

    /* renamed from: tv */
    TextView tv;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            startActivityForResult(new Intent(this, LoginActivity.class), 1);
        }
        if (1 != 0) {
            ((Switch) findViewById(R.id.switch1)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        MainActivity.this.locStart();
                    } else {
                        MainActivity.this.locStop();
                    }
                }
            });
            ((Button) findViewById(R.id.button)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    MainActivity.this.login("{\n\t\"email\": \"test@gmail.com\",\n\t\"password\": \"123456\"\n}", url + "/user/login");
                }
            });
            LocationManager locationManager2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            this.locationManager = locationManager2;
            try {
                this.lastKnownLocation = locationManager2.getLastKnownLocation(locationProvider);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            this.tv = (TextView) findViewById(R.id.textView);
            String str = "android.permission.ACCESS_FINE_LOCATION";
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                ActivityCompat.requestPermissions(this, new String[]{str}, 3141);
            }
            SharedPreferences preferences = getPreferences(0);
            this.sharedPreferences = preferences;
            this.editor = preferences.edit();
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1) {
            String str = "token";
            if (this.sharedPreferences.contains(str)) {
                this.editor.remove(str);
            }
            SharedPreferences sharedPreferences2 = this.sharedPreferences;
            String str2 = NotificationCompat.CATEGORY_EMAIL;
            if (sharedPreferences2.contains(str2)) {
                this.editor.remove(str2);
            }
            this.editor.putString(str, data.getExtras().getString(str));
            this.editor.putString(str2, data.getExtras().getString(str2));
            this.editor.commit();
            Toast.makeText(getApplicationContext(), "help me please", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void locStart() {
        LocationListener r5 = new LocationListener() {
            public void onLocationChanged(Location location) {
                MainActivity mainActivity = MainActivity.this;
                if (mainActivity.isBetterLocation(location, mainActivity.lastKnownLocation)) {
                    MainActivity.this.lastKnownLocation = location;
                }
                MainActivity mainActivity2 = MainActivity.this;
                mainActivity2.sendLoc(mainActivity2.lastKnownLocation);
            }

            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            public void onProviderEnabled(String s) {
            }

            public void onProviderDisabled(String s) {
            }
        };
        this.locationListener = r5;
        try {
            this.locationManager.requestLocationUpdates(locationProvider, 5000, 0.0f, r5);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void locStop() {
        this.locationManager.removeUpdates(this.locationListener);
        this.tv.setText("GPS Data");
    }

    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 5000;
        boolean isSignificantlyOlder = timeDelta < -5000;
        boolean isNewer = timeDelta > 0;
        if (isSignificantlyNewer) {
            return true;
        }
        if (isSignificantlyOlder) {
            return false;
        }
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 10;
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return true;
        }
        if (isNewer && !isLessAccurate) {
            return true;
        }
        if (!isNewer || isSignificantlyLessAccurate || !isFromSameProvider) {
            return false;
        }
        return true;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 != null) {
            return provider1.equals(provider2);
        }
        return provider2 == null;
    }

    public void sendLoc(Location loc) {
        TextView textView = this.tv;
        StringBuilder sb = new StringBuilder();
        sb.append("Lat: ");
        sb.append(loc.getLatitude());
        sb.append("; Lon: ");
        sb.append(loc.getLongitude());
        textView.setText(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("{\n\t\"lat\": ");
        sb2.append(loc.getLatitude());
        sb2.append(",\n\t\"long\": ");
        sb2.append(loc.getLongitude());
        sb2.append("\n}");
        String data = sb2.toString();
        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
        sendPost(data, url + "/location/set");
    }

    public void login(String data, String url) {
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(1, url, new Listener<String>() {
            public void onResponse(String response) {
                String str = "token";
                try {
                    JSONObject objres = new JSONObject(response);
                    Toast.makeText(MainActivity.this.getApplicationContext(), objres.toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this.getApplicationContext(), "goodyeet", Toast.LENGTH_SHORT).show();
                    String token = new JSONObject(objres.toString()).getString(str);
                    if (MainActivity.this.sharedPreferences.contains(str)) {
                        Toast.makeText(MainActivity.this.getApplicationContext(), "fuck you", Toast.LENGTH_SHORT);
                        MainActivity.this.editor.remove(str);
                    }
                    MainActivity.this.editor.putString(str, token);
                    MainActivity.this.editor.commit();
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this.getApplicationContext(), "badyeet", Toast.LENGTH_SHORT).show();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void sendPost(String data, String url) {
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(1, url, new Listener<String>() {
            public void onResponse(String response) {
                try {
                    Toast.makeText(MainActivity.this.getApplicationContext(), new JSONObject(response).toString(), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put(str, MainActivity.this.sharedPreferences.getString(str, ""));
                return params;
            }
        };
        this.requestQueue.add(r0);
    }

    public void sendGet(String data, String url) {
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(0, url, new Listener<String>() {
            public void onResponse(String response) {
                try {
                    Toast.makeText(MainActivity.this.getApplicationContext(), new JSONObject(response).toString(), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put(str, MainActivity.this.sharedPreferences.getString(str, ""));
                return params;
            }
        };
        this.requestQueue.add(r0);
    }

    public void printToken(View view) {
        Toast.makeText(getApplicationContext(), this.sharedPreferences.getString("token", "help"), Toast.LENGTH_SHORT).show();
    }

    public void getServerLocation(View view) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\t\"reqUserMail\": \"");
        sb.append(this.sharedPreferences.getString(NotificationCompat.CATEGORY_EMAIL, ""));
        sb.append("\",\n\t\"groupId\": \"5e528d6e39a3b01cd79ac053\"\n}");
        sendPost(sb.toString(), url + "/location/get");
    }
}
