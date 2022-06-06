package com.example.smartvest;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class WelcomActivity extends AppCompatActivity {
    TextView logout_worker;
    ConstraintLayout location_worker;
    ConstraintLayout safety_worker;
    ConstraintLayout manual_worker;
    ConstraintLayout report_worker;
    LinearLayout bluetooth_connect;
    private BluetoothSPP bt;
    TextView vest_connection;
    String input_name = "";
    Intent intent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        location_worker = findViewById(R.id.location_worker);
        manual_worker = findViewById(R.id.manual_worker);

        //로그아웃 버튼
        logout_worker = findViewById(R.id.logout_worker);
        logout_worker.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        logout_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WelcomActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(WelcomActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        });



        //블루투스 연결 하는부분
        bt = new BluetoothSPP(this);
        bluetooth_connect = findViewById(R.id.bluetooth_connect);
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext(),
                    "Bluetooth is not available",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(WelcomActivity.this, message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            //연결해제
            @Override
            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();

            }

            //연결실패
            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }

        });
        //작업자 현재 위치 보기
        location_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WorkerMapActivity.class);
                startActivity(intent);
            }
        });
        //작업자 매뉴얼
        manual_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WorkerManualActivity.class);
                startActivity(intent);
            }
        });
        //블루투스 연결하기
        bluetooth_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }

        });
    }

    public void onDestroy() {

        super.onDestroy();
        bt.stopService();//블루투스 중지
    }

    //오류나는 부분 해결해야함
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(intent, REQUEST_ENABLE_BT);

        }else{
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

            }
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {//연결됨
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {//사용불가
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}