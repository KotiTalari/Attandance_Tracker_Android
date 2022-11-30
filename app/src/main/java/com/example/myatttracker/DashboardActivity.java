package com.example.myatttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nambimobile.widgets.efab.ExpandableFabLayout;
import com.nambimobile.widgets.efab.FabOption;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    String profileResponse;
    JSONObject profile;
    ImageView iv_profile_icon;
    TextView tv_name, tv_email, tv_mobile, tv_gender, tv_dob;
    FabOption update_profile, scan_qr_code;
    SharedPreferences mPreferences;
    String token, userId;
    ProgressDialog pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = getSharedPreferences("Tracker", MODE_PRIVATE);
        pBar = new ProgressDialog(DashboardActivity.this);
        initViews();

    }

    private void initViews() {
        iv_profile_icon = findViewById(R.id.iv_profile_icon);
        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        tv_mobile = findViewById(R.id.tv_mobileno);
        tv_gender = findViewById(R.id.tv_gender);
        tv_dob = findViewById(R.id.tv_dob);
        update_profile = findViewById(R.id.update_profile);
        scan_qr_code = findViewById(R.id.scan_qr_code);
        profileResponse = getIntent().getStringExtra("Response");
        if (!profileResponse.equalsIgnoreCase("NA")) {
            try {
                profile = new JSONObject(profileResponse);
                tv_name.setText("" + profile.optString("name"));
                tv_dob.setText("" + profile.optString("dob"));
                if (profile.optString("gender").equalsIgnoreCase("m")) {
                    tv_gender.setText("Male");
                } else {
                    tv_gender.setText("Fe-Male");
                }
                tv_mobile.setText("" + profile.optString("mobileNo"));
                tv_email.setText("" + profile.optString("email"));
                if (profile.optString("gender").equalsIgnoreCase("m")) {
                    iv_profile_icon.setImageResource(R.drawable.male_selected);
                } else {
                    iv_profile_icon.setImageResource(R.drawable.female_selected);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            token = mPreferences.getString("Token", "");
            userId = mPreferences.getString("UserId", "");
            if (isNetworkConnected()) {
                pBar.setMessage("Please wait....");
                pBar.show();
                RequestQueue requestQueue = Volley.newRequestQueue(DashboardActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, APIClient.student_profile + userId, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pBar.dismiss();
                        Log.i("VOLLEY", response);
                        try {
                            if (response != null) {
                                JSONObject object = new JSONObject(response);
                                if (object.length() > 0) {
                                    if (object.optString("code").equalsIgnoreCase("200")) {
                                        if (object.has("data")) {
                                            if (object.optJSONObject("data").length() > 0) {
                                                profile = object.optJSONObject("data");
                                                profileResponse = "" + object.optString("data");
                                                tv_name.setText("" + profile.optString("name"));
                                                tv_dob.setText("" + profile.optString("dob"));
                                                if (profile.optString("gender").equalsIgnoreCase("m")) {
                                                    tv_gender.setText("Male");
                                                } else {
                                                    tv_gender.setText("Fe-Male");
                                                }
                                                tv_mobile.setText("" + profile.optString("mobileNo"));
                                                tv_email.setText("" + profile.optString("email"));
                                                if (profile.optString("gender").equalsIgnoreCase("m")) {
                                                    iv_profile_icon.setImageResource(R.drawable.male_selected);
                                                } else {
                                                    iv_profile_icon.setImageResource(R.drawable.female_selected);
                                                }
                                            } else {
                                                Toast.makeText(DashboardActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(DashboardActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(DashboardActivity.this, "" + object.optString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(DashboardActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(DashboardActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pBar.dismiss();
                        Log.e("VOLLEYError", error.toString());
                        Toast.makeText(DashboardActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("token", "" + token);
                        return params;
                    }
                };

                requestQueue.add(stringRequest);
            }else{
                Toast.makeText(this, "Please connect to Internet and try again...", Toast.LENGTH_SHORT).show();
            }
        }

        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, UpdateProfileActivity.class)
                        .putExtra("Response", profileResponse));
            }
        });
        scan_qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, ScanQrCodeActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            hitLogoutService();
                        }
                    }).create().show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        hitLogoutService();
                    }
                }).create().show();
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public void hitLogoutService(){
        if (isNetworkConnected()) {
            pBar.setMessage("Please wait....");
            pBar.show();
            JSONObject object = new JSONObject();
            RequestQueue requestQueue = Volley.newRequestQueue(DashboardActivity.this);
            try {
                token = mPreferences.getString("Token", "");
                object.put("uid", token);
                String mRequestBody = object.toString();
                Log.e("mRequestBody", mRequestBody);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.student_logout, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pBar.dismiss();
                        Log.i("VOLLEY", response);
                        try {
                            if (response != null) {
                                JSONObject object = new JSONObject(response);
                                if (object.length() > 0) {
                                    if (object.optString("code").equalsIgnoreCase("200")) {
                                        if (object.has("data")) {
                                            if (object.optString("data").length() > 0) {
                                                startActivity(new Intent(DashboardActivity.this, LoginActivity.class)
                                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            } else {
                                                Toast.makeText(DashboardActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(DashboardActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(DashboardActivity.this, "" + object.optString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(DashboardActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(DashboardActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pBar.dismiss();
                        Log.e("VOLLEYError", error.toString());
                        Toast.makeText(DashboardActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                            return null;
                        }
                    }
                };
                requestQueue.add(stringRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(DashboardActivity.this, "Please connect to internet and try again...", Toast.LENGTH_SHORT).show();
        }
    }
}