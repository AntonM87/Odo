package com.hfad.odometer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Random;

public class OdometerService extends Service {

    private final IBinder binder = new OdometerBinder();

    private LocationListener locationListener;

    private LocationManager locationManager;
    public static String PERMISSION_STRING = Manifest.permission.ACCESS_FINE_LOCATION;

    private static double distanceMeter;
    private static Location lastLocation = null;

    @Override
    // Метод onCreate стандартный в жизненном цикле службы
    public void onCreate() {
        super.onCreate();
        // Слушатель позиционирования
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (lastLocation == null){
                    lastLocation = location;
                }
                distanceMeter += location.distanceTo(lastLocation);
                lastLocation = location;
                Log.d("log","distanceMeter " + distanceMeter + " : lastLocation" + lastLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        // Проверить наличие разрешения
        if (ContextCompat.checkSelfPermission(this,PERMISSION_STRING) == PackageManager.PERMISSION_GRANTED){
            String provider = locationManager.getBestProvider(new Criteria(),true);
            if (provider != null){
                //Запросить обновления от провайдера данных местонахождения.
                locationManager.requestLocationUpdates(provider,1000,1,locationListener);
            }
        }
    }

    @Override
    //Метод onBind() вызывается при запросе компонента на связывание со службой
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public double getDistance(){
        return distanceMeter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener);
            }
            locationListener = null;
            locationManager = null;
        }
    }

    public class OdometerBinder extends Binder {
        OdometerService getOdometer(){
            return OdometerService.this;
        }
        public IBinder onBind() {
            return null;
        }
    }
}
