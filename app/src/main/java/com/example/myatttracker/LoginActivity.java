package com.example.myatttracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.material.textfield.TextInputEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText mEtUserName,mEtPassword;
    TextView mTvSignUp;
    String strId,strPassword;
    RelativeLayout mRlSignIn;
    ProgressDialog pBar;
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        pBar=new ProgressDialog(LoginActivity.this);
        mPreferences=getSharedPreferences("Tracker",MODE_PRIVATE);
        mEditor=mPreferences.edit();
        initViews();

    }

    private void initViews() {
        mEtUserName=findViewById(R.id.mEtUserName);
        mEtPassword=findViewById(R.id.mEtPassword);
        mRlSignIn=findViewById(R.id.mRlSignIn);
        mTvSignUp=findViewById(R.id.mTvSignUp);
        mTvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });
        mRlSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    if (isNetworkConnected()) {
                        pBar.setMessage("Please wait....");
                        pBar.show();
                        JSONObject object = new JSONObject();
                        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                        try {
                            object.put("id", strId);
                            object.put("password", strPassword);
                            String mRequestBody = object.toString();
                            Log.e("mRequestBody", mRequestBody);
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.Login_Student, new Response.Listener<String>() {
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
                                                            mEditor.putString("Token", "" + object.optString("data"));
                                                            mEditor.putString("UserId", "" + strId);
                                                            mEditor.apply();
                                                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class)
                                                                    .putExtra("Response", "NA"));
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "" + object.optString("message"), Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                            }

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(LoginActivity.this, "" + error, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(LoginActivity.this, "Please connect to internet and try again...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private boolean validate() {
        strId = mEtUserName.getText().toString();
        strPassword = mEtPassword.getText().toString();
       if (strId.equalsIgnoreCase("")) {
            mEtUserName.setError("Please Enter Your Id");
            return false;
        }else if (!strId.equalsIgnoreCase("") && strId.length()!=9) {
           mEtUserName.setError("Please Enter Valid Id");
            return false;
        }
        return true;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
