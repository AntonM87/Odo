package com.hfad.odometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private String TAG = "log";
    private final int PERMISSION_REQUEST_CODE = 698;
    private OdometerService odometer = new OdometerService();
    private boolean serviceConnect = false; //Признак связи активности со службой

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        //Код, выполняемый при связывании со службой
        public void onServiceConnected(ComponentName name, IBinder service) {
            OdometerService.OdometerBinder binder = (OdometerService.OdometerBinder)service;
            odometer = binder.getOdometer();
            serviceConnect = true;
        }
        @Override
        //Код, выполняемый при разрыве связи со службой
        public void onServiceDisconnected(ComponentName name) {
            serviceConnect = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showDisplayDistance();
    }
    @Override
    protected void onStart() {
        super.onStart();
        //Запрос разрешение на подключение службы
        if (ContextCompat.checkSelfPermission(this,OdometerService.PERMISSION_STRING)
                        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{OdometerService.PERMISSION_STRING},
                    PERMISSION_REQUEST_CODE
            );
        } else {
            Intent intent = new Intent(this, OdometerService.class);
            //Метод bindService() использует интент
            //и соединение со службой для связывания актив- ности со службой
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
            serviceConnect = true;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (serviceConnect){
            //Использует объект ServiceConnection connection для отмены связывания со службой.
            unbindService(connection);
            //Устанавливает признак связи с службой false
            serviceConnect = false;
        }
    }

    //я так понимаю исользуется новый поток так как если использовать wait в основном то
    //происходить торможение главного экрана
    private void showDisplayDistance(){
        final TextView textView = (TextView)findViewById(R.id.distance);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance = 0.0;
                if (odometer != null && serviceConnect) {
                    distance = odometer.getDistance();
                }
                String text = String.format(Locale.getDefault(),"%1$, .2f miles",distance);
                textView.setText(text);
                handler.postDelayed(this,1000);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0]){
                    Intent intent = new Intent(this,OdometerService.class);
                    bindService(intent,connection,Context.BIND_AUTO_CREATE);
                } else {

                }
        }

    }
}