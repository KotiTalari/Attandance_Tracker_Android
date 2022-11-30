package com.example.myatttracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScanQrCodeActivity extends AppCompatActivity {
    RelativeLayout rl_scan_qr;
    SharedPreferences mPreferences;
    ProgressDialog pBar;
    String token,userId;
    TextView tv_result_message;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);
        mPreferences = getSharedPreferences("Tracker", MODE_PRIVATE);
        pBar = new ProgressDialog(ScanQrCodeActivity.this);
        initViews();
        //hitAttendanceData();
    }

    private void initViews() {
        rl_scan_qr = findViewById(R.id.rl_scan);
        tv_result_message=findViewById(R.id.tv_result_message);
        rl_scan_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(ScanQrCodeActivity.this);
                intentIntegrator.setPrompt("Scan QR Code");
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.initiateScan();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                /*messageText.setText(intentResult.getContents());
                messageFormat.setText(intentResult.getFormatName());*/
                Log.e("Result", intentResult.getContents());
                Log.e("Result-1", intentResult.getFormatName());
                String qrResult = intentResult.getContents();
                if (!qrResult.equalsIgnoreCase("")) {
                    if (qrResult.contains("qrId")) {
                        try {
                            String result1=qrResult.replaceAll("\'","\"");
                            JSONObject object = new JSONObject(result1);
                            if (object.length() > 0) {
                                hitAttendanceData(object);
                            } else {
                                tv_result_message.setText("No QR-CODE data Found, Re scan QR-CODE");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        tv_result_message.setText("Please scan Related QR-CODE");
                    }
                } else {
                    tv_result_message.setText("Please Re-Scan QR-CODE");
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void hitAttendanceData(JSONObject object) {
        if (isNetworkConnected()) {
            token = mPreferences.getString("Token", "");
            userId = mPreferences.getString("UserId", "");
            pBar.setMessage("Please wait....");
            pBar.show();
            JSONObject params = new JSONObject();
            RequestQueue requestQueue = Volley.newRequestQueue(ScanQrCodeActivity.this);
            try {
                params.put("id", "");
                params.put("uid", userId);
                params.put("qid", object.optString("qrId"));
                params.put("scanTime", ""+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                params.put("cid", object.optString("courseId"));
                params.put("createdOn", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                /*params.put("id", "");
                params.put("uid", userId);
                params.put("qid", "1");
                params.put("scanTime", ""+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                params.put("cid", "123");
                params.put("createdOn", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));*/
                String mRequestBody = params.toString();
                Log.e("mRequestBody", mRequestBody);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.student_attendance_details, new Response.Listener<String>() {
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
                                                tv_result_message.setText(""+object.optString("message"));
                                                tv_result_message.setTextColor(getResources().getColor(R.color.color_heading));
                                            } else {
                                                tv_result_message.setText("User Data Not Found");
                                            }
                                        } else {
                                            tv_result_message.setText("User Data Not Found");
                                        }
                                    } else {
                                        tv_result_message.setText(""+object.optString("message"));
                                    }
                                } else {
                                    tv_result_message.setText("please try again");
                                }

                            } else {
                                tv_result_message.setText("please try again");
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
                        Toast.makeText(ScanQrCodeActivity.this, "" + error, Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        return params;
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
        } else {
            Toast.makeText(ScanQrCodeActivity.this, "Please connect to internet and try again...", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}

