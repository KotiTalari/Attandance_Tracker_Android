package com.example.myatttracker;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {
    EditText mEtEmail, mEtMobile, mEtPassword, mEtConfirmPassword;
    RadioGroup mRgGender;
    String strGender = "", strDOB, strEmail, strMobile, strPassword, strCPassword,profileResponse,strName,strId,strGender1="";
    RelativeLayout mRlUpdate, mRlDob;
    TextView tv_dob;
    int mYear, mMonth, mDay;
    String mobileRegX = "^[6-9]\\d{9}$",token;
    ProgressDialog pBar;
    JSONObject profile;
    TextView tv_name;
    ImageView iv_profile_icon;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        initViews();
    }

    private void initViews() {
        mEtPassword = findViewById(R.id.mEtPassword);
        mEtConfirmPassword = findViewById(R.id.mEtConfirmPassword);
        mRlDob = findViewById(R.id.rl_dob);
        tv_dob = findViewById(R.id.tv_dob);
        mEtEmail = findViewById(R.id.mEtEmail);
        mEtMobile = findViewById(R.id.mEtMobileNumber);
        mRgGender = findViewById(R.id.mRgGender);
        mRlUpdate = findViewById(R.id.mRlUpdate);
        tv_name = findViewById(R.id.tv_name);
        iv_profile_icon = findViewById(R.id.iv_profile_icon);
        pBar = new ProgressDialog(UpdateProfileActivity.this);
        mPreferences=getSharedPreferences("Tracker",MODE_PRIVATE);
        profileResponse=getIntent().getStringExtra("Response");
        Log.e("profile",""+profileResponse);
        try {
            profile=new JSONObject(profileResponse);
            tv_name.setText(""+profile.optString("name"));
            strName=""+profile.optString("name");
            strDOB=""+profile.optString("dob");
            strGender=profile.optString("gender");
            strMobile=""+profile.optString("mobileNo");
            strEmail=""+profile.optString("email");
            strPassword=""+profile.optString("password");
            strId=""+profile.optString("id");

            if(profile.optString("gender").equalsIgnoreCase("m")){
                iv_profile_icon.setImageResource(R.drawable.male_selected);
            }else{
                iv_profile_icon.setImageResource(R.drawable.female_selected);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mRgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.mRbMale) {
                    strGender = "m";
                    strGender1="m";
                } else {
                    strGender = "f";
                    strGender1="f";
                }
            }
        });
        mRlDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String strDayOfMonth = "", strMonthOfYear = "";
                                if (dayOfMonth < 10) {
                                    strDayOfMonth = "0" + dayOfMonth;
                                } else {
                                    strDayOfMonth = "" + dayOfMonth;
                                }
                                if ((monthOfYear + 1) < 10) {
                                    strMonthOfYear = "0" + (monthOfYear + 1);
                                } else {
                                    strMonthOfYear = "" + (monthOfYear + 1);
                                }
                                tv_dob.setText(year + "-" + strMonthOfYear + "-" + strDayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        mRlUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEtPassword.getText().toString().equalsIgnoreCase("") ||
                        !mEtMobile.getText().toString().equalsIgnoreCase("") ||
                        !mEtEmail.getText().toString().equalsIgnoreCase("") ||
                        !strGender1.equalsIgnoreCase("") ||
                        !tv_dob.getText().toString().equalsIgnoreCase("Select D.O.B")) {
                    if (validate()) {
                        if (!mEtPassword.getText().toString().equalsIgnoreCase("")) {
                            strPassword = mEtPassword.getText().toString();
                        }
                        if (!mEtEmail.getText().toString().equalsIgnoreCase("")) {
                            strEmail = mEtEmail.getText().toString();
                        }
                        if (!mEtMobile.getText().toString().equalsIgnoreCase("")) {
                            strMobile = mEtMobile.getText().toString();
                        }
                        token = mPreferences.getString("Token", "");
                        if (isNetworkConnected()) {
                            pBar.setMessage("Please wait....");
                            pBar.show();
                            JSONObject object = new JSONObject();
                            RequestQueue requestQueue = Volley.newRequestQueue(UpdateProfileActivity.this);
                            try {
                                object.put("id", strId);
                                object.put("gender", strGender);
                                object.put("password", strPassword);
                                object.put("name", strName);
                                object.put("email", strEmail);
                                object.put("mobileNo", strMobile);
                                object.put("dob", strDOB);
                                object.put("type", "f");
                                object.put("active", true);
                                String mRequestBody = object.toString();
                                Log.e("mRequestBody", mRequestBody);
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.student_profile_update, new Response.Listener<String>() {
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
                                                                startActivity(new Intent(UpdateProfileActivity.this, DashboardActivity.class)
                                                                        .putExtra("Response", "" + object.optJSONObject("data"))
                                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                            } else {
                                                                Toast.makeText(UpdateProfileActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(UpdateProfileActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(UpdateProfileActivity.this, "" + object.optString("message"), Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(UpdateProfileActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Toast.makeText(UpdateProfileActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(UpdateProfileActivity.this, "" + error, Toast.LENGTH_SHORT).show();
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
                        }else{
                            Toast.makeText(UpdateProfileActivity.this, "Please connect to Internet and try again...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(UpdateProfileActivity.this, "No changes detected...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validate() {
        if (!mEtPassword.getText().toString().equalsIgnoreCase("")) {
            if (!mEtPassword.getText().toString().equalsIgnoreCase(mEtConfirmPassword.getText().toString())) {
                mEtConfirmPassword.setError("Passwords Should be equal");
                return false;
            }
        } else if (!mEtEmail.getText().toString().equalsIgnoreCase("")) {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEtEmail.getText().toString()).matches()) {
                mEtEmail.setError("Please Enter Your valid Email Id");
                return false;
            }
        } else if (!mEtMobile.getText().toString().equalsIgnoreCase("")) {
            if (!mEtMobile.getText().toString().matches(mobileRegX)) {
                mEtMobile.setError("Please Enter Your valid Mobile Number");
                return false;
            }
        }
        return true;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
