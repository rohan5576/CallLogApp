package in.org.business.calllogapp;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView txtCall;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    //    code for getting phone state
    PhoneStateChangeListener pscl = new PhoneStateChangeListener();
    TextView tvPhoneState;

    //    code for recording call
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String fileName = null;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private boolean isPlaying = false;
    private TelephonyManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCall = (TextView) findViewById(R.id.call);
        tvPhoneState = (TextView) findViewById(R.id.tvOne);
        Log.e("inside", "onCreate()");

        //    code for getting phone state
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (checkAndRequestPermissions()) {

            // carry on the normal flow, as the case of  permissions  granted.
            getCallDetails();

            tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (grantResults.length == 5 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                // carry on the normal flow, as the case of  permissions  granted.
                getCallDetails();

                tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }*/

    private void getCallDetails() {
        Log.e("inside", "getCallDetails()");
        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");
//            Log.e("value: ", "" + sb);
        }
        managedCursor.close();
        txtCall.setText(sb);
    }

    /**
     * Checks for read and receive SMS permission.
     *
     * @return boolean true/false
     */
    private boolean checkAndRequestPermissions() {
        Log.e("inside", "checkAndRequestPermissions()");

        int readCallLog = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG);

        int recordCall = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        int readContacts = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);

        int writeToStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int readFromStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (readCallLog != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CALL_LOG);
        }
        if (readContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (recordCall != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (writeToStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readFromStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private class PhoneStateChangeListener extends PhoneStateListener {
        public boolean wasRinging;
        String LOG_TAG = "PhoneListener";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.e("inside", "PhoneStateChangeListener");
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i(LOG_TAG, "RINGING");
                    wasRinging = true;
                    tvPhoneState.setText("RINGING");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(LOG_TAG, "OFFHOOK");
                    tvPhoneState.setText("OFFHOOK");

                    if (!wasRinging) {
                        Log.i(LOG_TAG, "not Recieved");
                        tvPhoneState.setText("Not Recieved");
                    } else {
                        Log.i(LOG_TAG, "Recieved");
                        tvPhoneState.setText("Recieved");

                        Intent intent = new Intent(MainActivity.this, TService.class);
                        startService(intent);

                      /*  Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvPhoneState.setText("Recording.");

                                        startRecording();
                                    }
                                });
                            }
                        };
                        thread.start();*/
                    }
//                    stopRecording();

                    // this should be the last piece of code before the break
                    wasRinging = true;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(LOG_TAG, "IDLE");
                    tvPhoneState.setText("IDLE");

                    // this should be the last piece of code before the break
                    wasRinging = false;
                    break;
            }
        }
    }



}
