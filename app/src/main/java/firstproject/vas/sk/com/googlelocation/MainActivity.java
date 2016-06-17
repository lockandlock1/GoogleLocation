package firstproject.vas.sk.com.googlelocation;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mClient;
    TextView locationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationView = (TextView)findViewById(R.id.text_location);
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if(savedInstanceState != null){
            isErrorProcessing = savedInstanceState.getBoolean(FIELD_ERROR_PROCESSING);
        }
    }
    private static final String FIELD_ERROR_PROCESSING = "errorProcessing";
    private boolean isErrorProcessing = false;
    private static final int RC_PERMISSION = 1;
    private static final int RC_API_CLIENT = 2;

    boolean isFirst = true;

    private void displayLocation(Location location){
        locationView.setText("lat : " + location.getLatitude() + ", lng : " + location.getLongitude());
        if(isFirst){
            isFirst = false;
            Geofence geofence = new Geofence.Builder()
                    .setCircularRegion(location.getLatitude(),location.getLongitude(),100)
                    .setExpirationDuration(24 * 60 * 60 * 1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(60 * 60 * 1000)
                    .setNotificationResponsiveness(90 * 1000)
                    .setRequestId("geofencekey")
                    .build();
            GeofencingRequest request = new GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .build();
            PendingIntent pi = PendingIntent.getService(this,0,new Intent(this,GeofencingService.class),PendingIntent.FLAG_UPDATE_CURRENT);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.GeofencingApi.addGeofences(mClient,request,pi);
        }
    }
    public void getLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RC_PERMISSION);
            }


        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mClient);
        if(location != null){
            displayLocation(location);
        }
        LocationRequest request = new LocationRequest();
        request.setFastestInterval(5000);
        request.setInterval(10000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient,request,mListener);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            displayLocation(location);
        }
    };
/*
    private void registerActivityRecognition(){
        PendingIntent pi = null;
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient, 60 * 60 * 1000 , pi);
    }*/
    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    //    registerActivityRecognition();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length > 0) {
            for (int code : grantResults) {
                if (code == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    return;
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(isErrorProcessing) return;
        isErrorProcessing = true;
        if(connectionResult.hasResolution()){
            try {
                connectionResult.startResolutionForResult(this,RC_API_CLIENT);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                mClient.connect();
            }
        } else {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,connectionResult.getErrorCode(),RC_API_CLIENT);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != RC_API_CLIENT){
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        isErrorProcessing = false;
        if(requestCode == Activity.RESULT_OK){
            mClient.connect();
        }

    }



    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(FIELD_ERROR_PROCESSING,isErrorProcessing);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect();
    }
}
