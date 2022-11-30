package com.example.myatttracker;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
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

public class SignupActivity extends AppCompatActivity {
    EditText mEtName, mEtId, mEtPassword, mEtConfirmPassword, mEtEmail, mEtMobile;
    RadioGroup mRgGender;
    String strName, strId, strPassword, strGender = "", strDOB, strCPassword, strEmail, strMobile;
    RelativeLayout mRlSignUp,mRlDob;
    TextView tv_dob;
    int mYear, mMonth, mDay;
    String mobileRegX = "^[6-9]\\d{9}$";
    ProgressDialog pBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();
    }

    private void initViews() {
        mEtName = findViewById(R.id.mEtUserName);
        mEtPassword = findViewById(R.id.mEtPassword);
        mEtConfirmPassword = findViewById(R.id.mEtConfPassword);
        mRlDob = findViewById(R.id.rl_dob);
        tv_dob = findViewById(R.id.tv_dob);
        mEtEmail = findViewById(R.id.mEtEmail);
        mEtMobile = findViewById(R.id.mEtMobileNumber);
        mEtId = findViewById(R.id.mEtStudentId);
        mRgGender = findViewById(R.id.mRgGender);
        mRlSignUp = findViewById(R.id.mRlSignUp);
        pBar=new ProgressDialog(SignupActivity.this);

        mRgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.mRbMale) {
                    strGender = "M";
                } else {
                    strGender = "F";
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


                DatePickerDialog datePickerDialog = new DatePickerDialog(SignupActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String strDayOfMonth="",strMonthOfYear="";
                                if(dayOfMonth<10){
                                    strDayOfMonth="0"+dayOfMonth;
                                }else{
                                    strDayOfMonth=""+dayOfMonth;
                                }
                                if((monthOfYear+1)<10){
                                    strMonthOfYear="0"+(monthOfYear+1);
                                }else{
                                    strMonthOfYear=""+(monthOfYear+1);
                                }
                                tv_dob.setText(year + "-" + strMonthOfYear + "-" + strDayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        mRlSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (isNetworkConnected()) {
                        pBar.setMessage("Please wait....");
                        pBar.show();
                        JSONObject object = new JSONObject();
                        RequestQueue requestQueue = Volley.newRequestQueue(SignupActivity.this);
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
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.Add_Student, new Response.Listener<String>() {
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
                                                            Toast.makeText(SignupActivity.this, "" + object.optString("message"), Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                        } else {
                                                            Toast.makeText(SignupActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(SignupActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(SignupActivity.this, "" + object.optString("message"), Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                            }

                                        } else {
                                            Toast.makeText(SignupActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(SignupActivity.this, "" + error, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SignupActivity.this, "Please connect to internet and try again...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private boolean validate() {
        strName = mEtName.getText().toString();
        strId = mEtId.getText().toString();
        strPassword = mEtPassword.getText().toString();
        strCPassword = mEtConfirmPassword.getText().toString();
        strDOB = tv_dob.getText().toString();
        strEmail = mEtEmail.getText().toString();
        strMobile = mEtMobile.getText().toString();
        if (strName.equalsIgnoreCase("")) {
            mEtName.setError("Please Enter Your Name");
            return false;
        } else if (strId.equalsIgnoreCase("")) {
            mEtId.setError("Please Enter Your Id");
            return false;
        }else if (!strId.equalsIgnoreCase("") && strId.length()!=9) {
            mEtId.setError("Please Enter Valid Id");
            return false;
        } else if (strGender.equalsIgnoreCase("")) {
            Toast.makeText(this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
            return false;
        } else if (strPassword.equalsIgnoreCase("")) {
            mEtPassword.setError("Please Enter Your Password");
            return false;
        } else if (strCPassword.equalsIgnoreCase("")) {
            mEtConfirmPassword.setError("Please Confirm your Password");
            return false;
        } else if (!strPassword.equalsIgnoreCase(strCPassword)) {
            mEtConfirmPassword.setError("Passwords Should be equal");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            mEtEmail.setError("Please Enter Your valid Email Id");
            return false;
        } else if (!strMobile.matches(mobileRegX)) {
            mEtMobile.setError("Please Enter Your valid Mobile Number");
            return false;
        } else if (strDOB.equalsIgnoreCase("Select D.O.B")) {
            Toast.makeText(this, "Please Select your DOB", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
