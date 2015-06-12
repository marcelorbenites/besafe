package com.panicbutton.panicbutton;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult>, DialogInterface.OnDismissListener {

    @InjectView(R.id.activity_main_start_stop_button) Button startStopButton;
    @InjectView(R.id.activity_main_tutorial) RelativeLayout mTutorial;

    public static final int REQUEST_RESOLVE_ERROR = 1001;
    public static final String ERROR_DIALOG_TAG = "ERROR_DIALOG_TAG";
    public static final String PARSE_INSTALLATION = "installation";

    private final String LOG_TAG = "MainActivity";
    private static final int REQUEST_CHECK_SETTINGS = 2000;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private boolean resolvingError;
    private ParseObjectParser panicReportParseObjectParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        panicReportParseObjectParser = new PanicReportParseObjectParser();
        resolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();

        startStopButton.setEnabled(false);

        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override public void done(ParseException e) {
                if (e == null) {
                    startStopButton.setEnabled(true);
                } else {
                    Toast.makeText(MainActivity.this, R.string.activity_main_error_starting_application, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void connectToGooglePlayServices() {
        if (googleApiClient.isConnected()) {
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest).setResultCallback(this);
        } else if (!googleApiClient.isConnecting()
                && !resolvingError) {
            googleApiClient.connect();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESOLVE_ERROR:
                resolvingError = false;
                if (resultCode == Activity.RESULT_OK) {
                    connectToGooglePlayServices();
                }
                break;
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    connectToGooglePlayServices();
                }
                break;
        }
    }

    @OnClick(R.id.activity_main_start_stop_button) void onStartStop() {
        startService(new Intent(this, DangerZoneMonitorService.class).setAction(DangerZoneMonitorService.ACTION_INITIALIZE));
    }

    @Override public void onResult(LocationSettingsResult result) {
        final Status status = result.getStatus();
        onLocationSettingsResult(status);
    }

    @Override public void onConnected(Bundle bundle) {
        Log.e(LOG_TAG, "onConnected");
        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest).setResultCallback(this);
    }

    @Override public void onConnectionSuspended(int i) {
    }

    @Override public void onLocationChanged(Location location) {
        Log.e(LOG_TAG, "onLocationChanged");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        ParseObject report = panicReportParseObjectParser.parse(new PanicReport(location.getLatitude(), location.getLongitude(), 1));
        report.put(PARSE_INSTALLATION, ParseInstallation.getCurrentInstallation());
        report.saveInBackground(new SaveCallback() {
            @Override public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(MainActivity.this, "Reported Panic!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error reporting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!resolvingError) {
            if (connectionResult.hasResolution()) {
                try {
                    resolvingError = true;
                    connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    connectToGooglePlayServices();
                }
            } else {
                GooglePlayServicesErrorDialogFragment dialogFragment = GooglePlayServicesErrorDialogFragment.newInstance(connectionResult.getErrorCode(), REQUEST_RESOLVE_ERROR);
                dialogFragment.getDialog().setOnDismissListener(this);
                dialogFragment.show(getSupportFragmentManager(), ERROR_DIALOG_TAG);
                resolvingError = true;
            }
        }
    }

    private void onLocationSettingsResult(Status status) {
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(
                            this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException ignored) {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
            default:
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override public void onDismiss(DialogInterface dialog) {
        resolvingError = false;
    }

    @OnClick(R.id.activity_main_report_location_button) public void reportThisLocation(View v){
        connectToGooglePlayServices();
    }

    @OnClick(R.id.activity_main_show_tutorial) public void showTutorial(View v){
        mTutorial.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.activity_main_tutorial) public void hideTutorial(View v){
        mTutorial.setVisibility(View.GONE);
    }

    @Override public void onBackPressed() {
        if (mTutorial.getVisibility() == View.VISIBLE) {
            mTutorial.setVisibility(View.GONE);
        } else {
            finish();
        }

    }
}
