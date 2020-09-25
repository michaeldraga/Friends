package com.mike.newfriends;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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
import org.json.JSONArray;

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
    private LayoutInflater inflater;

    private Button _addFriendsButton;

    private String result;

    /* renamed from: tv */
    TextView tv;

    private static final boolean debug = false;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_main);
        if (savedInstanceState == null) {
            startActivityForResult(new Intent(this, LandingActivity.class), 1);
        }
        sharedPreferences = getPreferences(0);
        editor = sharedPreferences.edit();

        LinearLayout insert = findViewById(R.id.insert_point);

        inflater = this.getLayoutInflater();

        _addFriendsButton = findViewById(R.id.button8);
        this._addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent actIntent = new Intent(getApplicationContext(), AddFriendsActivity.class);
                actIntent.putExtra("token", sharedPreferences.getString("token", ""));
                startActivityForResult(actIntent, 1);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                locStop();
            }
        });

        Button button7 = findViewById(R.id.button7);
        button7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fullRestart();
            }
        });
        /*
        View but = inflater.inflate(R.layout.user_location_template, insert, false);
        TextView name = but.findViewById(R.id.name);
        name.setText("help me please");
        insert.addView(but);
        */
        String str = "android.permission.ACCESS_FINE_LOCATION";
        if (ContextCompat.checkSelfPermission(this, str) != 0) {
            ActivityCompat.requestPermissions(this, new String[]{str}, 3141);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        } catch (SecurityException e) {
            e.printStackTrace();
        }



        //setContentView(R.layout.new_main);

        //ConstraintLayout v = (ConstraintLayout) vi.inflate(R.layout.user_location_template, null);
        //LinearLayout insert = (LinearLayout) findViewById(R.id.insert_point);
        //insert.addView(v, 0, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

        /*
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
        }*/
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
            if (debug)
                Toast.makeText(getApplicationContext(), "help me please", Toast.LENGTH_SHORT).show();
        }
        locStart();
        listFriends();
        sendGet("", url + "/user/me");
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

    public void listFriends() {
        String _url = url + "/friends/list";
        final String data = "";
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(1, _url, new Listener<String>() {
            public void onResponse(String response) {
                try {
                    if (debug)
                        Toast.makeText(MainActivity.this.getApplicationContext(), new JSONObject(response).toString(), Toast.LENGTH_SHORT).show();
                    if (sharedPreferences.contains("token"))
                        editor.remove("token");
                    editor.putString("token", new JSONObject(response).getString("token"));
                    editor.commit();
                    try {
                        JSONObject list = new JSONObject(response);
                        JSONObject friends = list.getJSONObject("friends");
                        JSONArray actual = friends.getJSONArray("actual");
                        for (int i = 0; i < actual.length(); i++) {
                            String username = actual.getJSONObject(i).getString("username");
                            JSONObject location = actual.getJSONObject(i).getJSONObject("location");
                            String lati = location.getString("lat");
                            String longi = location.getString("long");
                            String address = location.getString("address");
                            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
                            View v = inflater.inflate(R.layout.user_location_template, insertPoint, false);
                            TextView name = (TextView) v.findViewById(R.id.name);
                            TextView street = (TextView) v.findViewById(R.id.street);
                            ImageView movement = (ImageView) v.findViewById(R.id.movement);
                            ImageView favorite = (ImageView) v.findViewById(R.id.favorite);
                            name.setText(username);
                            street.setText(address);
                            //insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            insertPoint.addView(v);
                        }
                    } catch (Exception e) {
                        if (debug)
                            Toast.makeText(getApplicationContext(), "oopsie", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    if (debug)
                        Toast.makeText(MainActivity.this.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (debug) Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void locStart() {
        LocationListener r5 = new LocationListener() {
            public void onLocationChanged(Location location) {
                MainActivity mainActivity = MainActivity.this;
                if (mainActivity.isBetterLocation(location, lastKnownLocation)) {
                    lastKnownLocation = location;
                }
                sendLoc(lastKnownLocation);
                listFriends();
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
        StringBuilder sb2 = new StringBuilder();
        sb2.append("{\n\t\"lat\": ");
        sb2.append(loc.getLatitude());
        sb2.append(",\n\t\"long\": ");
        sb2.append(loc.getLongitude());
        sb2.append("\n}");
        String data = sb2.toString();
        if (debug)Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
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
                    if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), objres.toString(), Toast.LENGTH_SHORT).show();
                    if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), "goodyeet", Toast.LENGTH_SHORT).show();
                    String token = new JSONObject(objres.toString()).getString(str);
                    if (MainActivity.this.sharedPreferences.contains(str)) {
                        if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), "fuck you", Toast.LENGTH_SHORT);
                        MainActivity.this.editor.remove(str);
                    }
                    MainActivity.this.editor.putString(str, token);
                    MainActivity.this.editor.commit();
                } catch (JSONException e) {
                    if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                    if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), "badyeet", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), new JSONObject(response).toString(), Toast.LENGTH_SHORT).show();
                    if (sharedPreferences.contains("token"))
                        editor.remove("token");
                    editor.putString("token", new JSONObject(response).getString("token"));
                    editor.commit();
                    result = response;
                } catch (JSONException e) {
                    if (debug)Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void sendGet(String data, String url) {
        final String savedata = data;
        if (this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        StringRequest r0 = new StringRequest(0, url, new Listener<String>() {
            public void onResponse(String response) {
                try {
                    //Toast.makeText(MainActivity.this.getApplicationContext(), new JSONObject(response).toString(), Toast.LENGTH_SHORT).show();
                    TextView tv = findViewById(R.id.textView2);
                    tv.setText(new JSONObject(response).getJSONObject("user").getString("username"));
                } catch (JSONException e) {
                    if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (debug)Toast.makeText(MainActivity.this.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void fullRestart() {
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void printToken(View view) {
        if (debug)Toast.makeText(getApplicationContext(), this.sharedPreferences.getString("token", "help"), Toast.LENGTH_SHORT).show();
    }

    public void getServerLocation(View view) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\t\"reqUserMail\": \"");
        sb.append(this.sharedPreferences.getString(NotificationCompat.CATEGORY_EMAIL, ""));
        sb.append("\",\n\t\"groupId\": \"5e528d6e39a3b01cd79ac053\"\n}");
        sendPost(sb.toString(), url + "/location/get");
    }
}
