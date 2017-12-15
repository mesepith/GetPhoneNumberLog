package com.zahiralam.getphonenumberlog.getphonenumberlog;

import android.Manifest;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import android.util.Log;

import android.database.Cursor;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CallLogPlugin";

    String[] permissions = new String[]{
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.INTERNET,
    };
    TextView txtView = null;
    EditText phone_no_view = null;
    Button submit_btn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView) findViewById(R.id.helloz);
        phone_no_view = (EditText) findViewById(R.id.phone_number);
        submit_btn = (Button) findViewById(R.id.submit_buttonz);


        submit_btn.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {

                        Toast.makeText(getApplicationContext(),phone_no_view.getText(),Toast.LENGTH_SHORT).show();

                        String phone_number = (String) phone_no_view.getText().toString();

                        JSONObject callLogz = getCallLog(phone_number);

                        try {
                            postUsingVolley(callLogz);
                        }catch(Exception e){

                        }
                    }
                });

        Toast.makeText(getApplicationContext(),txtView.getText(),Toast.LENGTH_SHORT).show();

        checkPermissions();

    }

    private JSONObject getCallLog( String phone_numb )
    {

        JSONObject callLog = new JSONObject();


        String[] strFields = {
                android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.NUMBER,
                android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.DURATION,
                android.provider.CallLog.Calls.NEW,
                android.provider.CallLog.Calls.CACHED_NAME,
                android.provider.CallLog.Calls.CACHED_NUMBER_TYPE,
                android.provider.CallLog.Calls.CACHED_NUMBER_LABEL };

        try {

            Calendar calendar = Calendar.getInstance();

            calendar.set(2017, Calendar.JANUARY, 1);
            String fromDate = String.valueOf(calendar.getTimeInMillis());
            calendar.set(2017, Calendar.JANUARY, 30);
            String toDate = String.valueOf(calendar.getTimeInMillis());

            String[] whereValue = {fromDate,toDate};

            Log.d( TAG, "STATIC from Date in milli sec : " + fromDate + " , to Date in milli sec : " + toDate );

            String whereClause = new String();

            String[] whereDateRange = new String[2];

            String condition = new String();

            String limit = new String();

            String[] wherePhoneNo = new String[1];

            wherePhoneNo[0] = "%"+phone_numb+"%";

            //whereClause = android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?";
            whereClause = android.provider.CallLog.Calls.NUMBER + " like ? ";

            condition = "if";

            whereDateRange[0] = fromDate;

            whereDateRange[1] = toDate;

            //limit = " limit 5";
            limit = "";
            //limit = " LIMIT 0,1";

            String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
			/* Query the CallLog Content Provider */

            Cursor callLogCursor = getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI
                    , strFields
                    , whereClause
                    , wherePhoneNo
                    , strOrder + limit);


            int callCount = callLogCursor.getCount();

            if (callCount > 0) {
                JSONObject callLogItem = new JSONObject();

                JSONObject callLogItems = new JSONObject();

                callLogCursor.moveToFirst();
                do {


                    String strcallDate = callLogCursor.getString(0);
                    Date callDate = new Date(Long.valueOf(strcallDate));

                    String callTypeCode = callLogCursor.getString(2);

                    String callType = null;
                    int callcode = Integer.parseInt(callTypeCode);
                    switch (callcode) {
                        case 1:
                            callType = "Incoming";
                            break;
                        case 2:
                            callType = "Outgoing";
                            break;
                        case 3:
                            callType = "Missed";
                            break;
                    }

                    callLogItem.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(callDate));
                    callLogItem.put("number", callLogCursor.getString(1));
                    callLogItem.put("callTypeCode", callTypeCode);
                    callLogItem.put("callType", callType);
                    callLogItem.put("duration", callLogCursor.getLong(3));
                    callLogItem.put("name", callLogCursor.getString(5));

                    callLogItems.put(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(callDate),callLogItem);

                    callLogItem = new JSONObject();
                } while (callLogCursor.moveToNext());

                callLog.put("callInOutData", callLogItems);
                callLog.put("totalCall", callCount);
                callLog.put("toDate", toDate);
                callLog.put("condition", condition);

                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();

                return callLog;

            }

            callLogCursor.close();
        } catch (Exception e) {

            Toast.makeText(getApplicationContext(),"Fail",Toast.LENGTH_SHORT).show();

            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

            Log.d("CallLog_Plugin", " ERROR : SQL to get cursor: ERROR " + e.getMessage());
        }

        return callLog;

    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }


    private void postUsingVolley(JSONObject callLog)throws Exception {

        String url = "https://e3db6ee9.ngrok.io/zahir/android/php/log.php";

        JSONObject js = new JSONObject();

        js.put("data", callLog);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, callLog,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());

                        txtView.setText(response.toString() + " , success");


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                txtView.setText(error.toString() + " , error");
            }
        }) {



        };
        queue.add(jsonObjReq);

    }
}
