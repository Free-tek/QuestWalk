package com.botosofttechnologies.questwalk.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.botosofttechnologies.questwalk.HomeWatcher;
import com.botosofttechnologies.questwalk.R;
import com.botosofttechnologies.questwalk.Service.MusicService;
import com.botosofttechnologies.questwalk.SharedPrefManager;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NavigationActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 555;

    int ACCESS_FINE_LOCATION_CODE = 3310;
    int ACCESS_COARSE_LOCATION_CODE = 3410;
    private GoogleApiClient mGoogleApiClient;


    ImageView help, bar0, bar1, bar2, bar3, bar4, bar5, star0, star1, star2, star3, star4, star5, settings, signOut, ad;
    HomeWatcher mHomeWatcher;
    TextView location1, location2, location3, distance1, distance2, distance3, prize1, prize2, prize3, text, adsText;
    Button viewMore;
    RelativeLayout layout;
    VideoView video;

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static FirebaseUser User = mAuth.getCurrentUser();
    static final String userID = User.getUid();

    LocationManager locationManager;
    String provider, date, key, prize, location, worth, description, image;

    FirebaseDatabase database;
    DatabaseReference user, tasks, ads;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    int $totem, taskCount, distance, adscount;

    double tasklng, tasklat, userlng, userlat;
    private Typeface header, subheading;

    Map<String, Double> distanceList = new HashMap<>();
    String $key;
    List<Double> rank = new ArrayList<Double>();
    Double refDistance, taskaltitude;
    String mkey, $distance1, $distance2, $distance3, adName, $node;
    int setAd, views, totem, vCount, noWinners;

    ProgressBar progressBar;
    ProgressDialog progress;

    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Build Google API Client for Location related work
        buildGoogleApiClient();

        setAd = 0;

        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        //get users token for login session
        database = FirebaseDatabase.getInstance();
        user = database.getReference("users");
        tasks = database.getReference("tasks");
        ads = database.getReference("ads");
        user.child(userID).child("token").setValue(SharedPrefManager.getInstance(NavigationActivity.this).getToken());

        //check if network is available to play game
        if(!isNetworkAvailable()){
            Toast.makeText(NavigationActivity.this, "No internet connection",
                    Toast.LENGTH_SHORT).show();
        }


        //Start HomeWatcher
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                $totem = Integer.parseInt(String.valueOf(dataSnapshot.child(userID).child("totem").getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        initUi();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //location can be gotten using GPS, WIFI or Network location but GPS Loctaion is better
        //We will be using GPS but we want to set up a back up just incase the user switches off his GPS
        //The line below gets the best provider for the location available at the time. you can set the criteria for the best location as either based on battery consumption or based on loaction precision.
        provider = locationManager.getBestProvider(new Criteria(), false);

        //get last known location from the user.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            new AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("We need to access your location in order for us to point you towards the right direction where you can find a treasure")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(NavigationActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .create()
                    .show();


            return;
        } else {
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
            } else {

                Location locationNetwork = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (locationNetwork != null) {
                    onLocationChanged(locationNetwork);
                } else {

                }


            }
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }




    // When user first come to this activity we try to connect Google services for location and map related work
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // Google Api Client is connected
    @Override
    public void onConnected(Bundle bundle) {
        if (mGoogleApiClient.isConnected()) {
            //if connected successfully show user the settings dialog to enable location from settings services
            // If location services are enabled then get Location directly
            // Else show options for enable or disable location services
            settingsrequest();
        }
    }


    // This is the method that will be called if user has disabled the location services in the device settings
    // This will show a dialog asking user to enable location services or not
    // If user tap on "Yes" it will directly enable the services without taking user to the device settings
    // If user tap "No" it will just Finish the current Activity
    public void settingsrequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (mGoogleApiClient.isConnected()) {

                            // check if the device has OS Marshmellow or greater than
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                                if (ActivityCompat.checkSelfPermission(NavigationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NavigationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
                                } else {
                                    // get Location
                                }
                            } else {
                                // get Location
                            }

                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(NavigationActivity.this, REQUEST_RESOLVE_ERROR);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    // This method is called only on devices having installed Android version >= M (Marshmellow)
    // This method is just to show the user options for allow or deny location services at runtime
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 3310: {

                if (grantResults.length > 0) {

                    for (int i = 0, len = permissions.length; i < len; i++) {

                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            // Show the user a dialog why you need location
                        } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // get Location
                        } else {
                            this.finish();
                        }
                    }
                }
                return;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // get location method

                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.


                        new AlertDialog.Builder(this)
                                .setTitle("Location Permission")
                                .setMessage("We need to access your location in order for us to point you towards the right direction where you can find a treasure")
                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(NavigationActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                MY_PERMISSIONS_REQUEST_LOCATION);
                                    }
                                })
                                .create()
                                .show();


                        return;

                    }else {
                        Location location = locationManager.getLastKnownLocation(provider);


                        if (location != null) {
                            onLocationChanged(location);

                        } else {
                            Location locationNetwork = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if(locationNetwork != null){
                                onLocationChanged(locationNetwork);

                            }else{

                            }


                        }
                    }


                    break;
                case Activity.RESULT_CANCELED:
                    this.finish();
                    break;
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
    }


    // When there is an error connecting Google Services
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }


    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            //((LocationActivity) getActivity()).onDialogDismissed();
        }
    }


    private void initUi() {


        star0 = (ImageView) findViewById(R.id.star0);
        star1 = (ImageView) findViewById(R.id.star1);
        star2 = (ImageView) findViewById(R.id.star2);
        star3 = (ImageView) findViewById(R.id.star3);
        star4 = (ImageView) findViewById(R.id.star4);
        star5 = (ImageView) findViewById(R.id.star5);
        bar0 = (ImageView) findViewById(R.id.bar0);
        bar1 = (ImageView) findViewById(R.id.bar1);
        bar2 = (ImageView) findViewById(R.id.bar2);
        bar3 = (ImageView) findViewById(R.id.bar3);
        bar4 = (ImageView) findViewById(R.id.bar4);
        bar5 = (ImageView) findViewById(R.id.bar5);

        ad = (ImageView) findViewById(R.id.ad);
        video = (VideoView) findViewById(R.id.video);

        video.setVisibility(View.INVISIBLE);

        layout = (RelativeLayout) findViewById(R.id.layout);

        settings = (ImageView) findViewById(R.id.settings);
        signOut = (ImageView) findViewById(R.id.signOut);
        help = (ImageView) findViewById(R.id.help);

        adsText = (TextView) findViewById(R.id.adsText);
        adsText.setVisibility(View.INVISIBLE);


        text = (TextView) findViewById(R.id.text);

        location1 = (TextView) findViewById(R.id.location1);
        location2 = (TextView) findViewById(R.id.location2);
        location3 = (TextView) findViewById(R.id.location3);


        distance1 = (TextView) findViewById(R.id.distance1);
        distance2 = (TextView) findViewById(R.id.distance2);
        distance3 = (TextView) findViewById(R.id.distance3);


        prize1 = (TextView) findViewById(R.id.prize1);
        prize2 = (TextView) findViewById(R.id.prize2);
        prize3 = (TextView) findViewById(R.id.prize3);


        viewMore = (Button) findViewById(R.id.viewMore);

        progressBar = (ProgressBar)findViewById(R.id.progress);
        Wave mWave = new Wave();
        mWave.setBounds(0,0,100,100);
        mWave.setColor(R.color.orange);
        progressBar.setIndeterminateDrawable(mWave);


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Task = new Intent(NavigationActivity.this, TasksList.class);
                startActivity(Task);
                finish();
            }
        });

        header = Typeface.createFromAsset(getAssets(), "fonts/heading.ttf");
        subheading = Typeface.createFromAsset(getAssets(), "fonts/subheading.ttf");

        text.setTypeface(header);
        location1.setTypeface(subheading);
        location2.setTypeface(subheading);
        location3.setTypeface(subheading);

        distance1.setTypeface(subheading);
        distance2.setTypeface(subheading);
        distance3.setTypeface(subheading);

        prize1.setTypeface(subheading);
        prize2.setTypeface(subheading);
        prize3.setTypeface(subheading);


        star0.setVisibility(View.INVISIBLE);
        star1.setVisibility(View.INVISIBLE);
        star2.setVisibility(View.INVISIBLE);
        star3.setVisibility(View.INVISIBLE);
        star4.setVisibility(View.INVISIBLE);
        star5.setVisibility(View.INVISIBLE);
        bar0.setVisibility(View.INVISIBLE);
        bar1.setVisibility(View.INVISIBLE);
        bar2.setVisibility(View.INVISIBLE);
        bar3.setVisibility(View.INVISIBLE);
        bar4.setVisibility(View.INVISIBLE);
        bar5.setVisibility(View.INVISIBLE);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(NavigationActivity.this).create();
                alertDialog.setTitle("Proceed?");
                alertDialog.setMessage("Do you want to proceed with sign out" );
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(NavigationActivity.this.getResources().getColor(R.color.orange));
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(NavigationActivity.this.getResources().getColor(R.color.orange));
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent signOut = new Intent (NavigationActivity.this, LoginActivity.class);
                                //sign out
                                signOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                FirebaseAuth.getInstance().signOut();
                                startActivity(signOut);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        setTotem();
        setNearestTasks();

        t=new Thread(){


            @Override
            public void run(){

                while(!isInterrupted()){

                    try {
                        Thread.sleep(10000);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                getAds(setAd);
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        t.start();



        location1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "0");
                startActivity(intent);
            }

        });
        prize1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "0");
                startActivity(intent);
            }
        });
        distance1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "0");
                startActivity(intent);
            }
        });

        location2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "1");
                startActivity(intent);
            }
        });
        prize2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "1");
                startActivity(intent);
            }
        });
        distance2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "1");
                startActivity(intent);
            }
        });

        location3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "2");
                startActivity(intent);
            }
        });
        prize3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "2");
                startActivity(intent);
            }
        });
        distance3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, TaskClicked.class);
                intent.putExtra("Key", "2");
                startActivity(intent);
            }
        });


    }

    private void getAds(final int $setAd) {
        user.child(userID).child("ads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    adscount = (int) dataSnapshot.getChildrenCount();
                    String imageUrl = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("image").getValue());
                    String adType = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("type").getValue());
                    final String view = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("view").getValue());
                    adName = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("name").getValue());
                    $node = node();


                    if(setAd == (adscount - 1) && adType.equals("0")){
                        video.setVisibility(View.INVISIBLE);
                        ad.setVisibility(View.VISIBLE);

                        Picasso.with(ad.getContext())
                                .load(imageUrl)
                                .into(ad);

                        if(view.equals("false") || view == null){
                            user.child(userID).child("ads").child(String.valueOf($setAd)).child("view").setValue("true");



                            if(totem == 5){
                                totem  = 4;
                            }


                            new Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            totem = totem + 1;
                                            ads.child(adName).child($node).setValue(userID);
                                            user.child(userID).child("totem").setValue(totem);
                                        }
                                    },
                                    6000);




                        }
                        setAd = 0;
                    }else if (adType.equals("0")){
                        video.setVisibility(View.INVISIBLE);
                        ad.setVisibility(View.VISIBLE);

                        Picasso.with(ad.getContext())
                                .load(imageUrl)
                                .into(ad);

                        if(view.equals("false") || view == null){

                            user.child(userID).child("ads").child(String.valueOf($setAd)).child("view").setValue("true");


                            if(totem == 5){
                                totem  = 4;
                            }


                            new Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            totem = totem + 1;
                                            ads.child(adName).child($node).setValue(userID);
                                            user.child(userID).child("totem").setValue(totem);
                                        }
                                    },
                                    6000);

                        }
                        setAd++;
                    }else if(setAd == (adscount - 1) && adType.equals("1")){
                        //load Video
                        video.setVisibility(View.VISIBLE);
                        ad.setVisibility(View.INVISIBLE);

                        loadvideo(imageUrl);

                        if(view.equals("false") || view == null){
                            user.child(userID).child("ads").child(String.valueOf($setAd)).child("view").setValue("true");



                            if(totem == 5){
                                totem  = 4;
                            }


                            new Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            totem = totem + 1;
                                            ads.child(adName).child($node).setValue(userID);
                                            user.child(userID).child("totem").setValue(totem);
                                        }
                                    },
                                    6000);


                        }
                        setAd = 0;
                    }else if(adType.equals("1")){
                        //view video
                        video.setVisibility(View.VISIBLE);
                        ad.setVisibility(View.INVISIBLE);

                        loadvideo(imageUrl);

                        if(view.equals("false") || view == null){

                            user.child(userID).child("ads").child(String.valueOf($setAd)).child("view").setValue("true");


                            if(totem == 5){
                                totem  = 4;
                            }


                            new Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            totem = totem + 1;
                                            ads.child(adName).child($node).setValue(userID);
                                            user.child(userID).child("totem").setValue(totem);
                                        }
                                    },
                                    6000);


                        }
                        setAd++;
                    }


                }else{
                    //no ads available
                    adsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void loadvideo(String videoUrl){

        video.setVisibility(View.VISIBLE);
        ad.setVisibility(View.GONE);

        try{
            if(!video.isPlaying()){
                Uri uri = Uri.parse(videoUrl);
                video.setVideoURI(uri);
                int length = video.getDuration();

                video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                    }
                });

            }else{
                video.pause();
            }

        }
        catch (Exception ex){

        }
        video.requestFocus();
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
                video.start();


            }
        });



    }

    private void setNearestTasks() {


        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlng = Double.parseDouble((String.valueOf(dataSnapshot.child(userID).child("location").child("lng").getValue())));
                userlat = Double.parseDouble((String.valueOf(dataSnapshot.child(userID).child("location").child("lat").getValue())));

                tasks.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        taskCount = (int) dataSnapshot.getChildrenCount();
                        int i;
                        for(i=0; i <taskCount; i++ ){

                            tasklng = Double.parseDouble((String.valueOf(dataSnapshot.child(String.valueOf(i)).child("lng").getValue())));
                            tasklat = Double.parseDouble((String.valueOf(dataSnapshot.child(String.valueOf(i)).child("lat").getValue())));
                            prize = String.valueOf(dataSnapshot.child(String.valueOf(i)).child("prize").getValue());
                            location = String.valueOf(dataSnapshot.child(String.valueOf(i)).child("location").getValue());
                            description = String.valueOf(dataSnapshot.child(String.valueOf(i)).child("description").getValue());
                            worth = String.valueOf(dataSnapshot.child(String.valueOf(i)).child("worth").getValue());
                            image = String.valueOf(dataSnapshot.child(String.valueOf(i)).child("image").getValue());
                            taskaltitude = Double.parseDouble(String.valueOf(dataSnapshot.child(String.valueOf(i)).child("altitude").getValue()));
                            noWinners = Integer.parseInt(String.valueOf(dataSnapshot.child(String.valueOf(i)).child("noWinners").getValue()));
                            key = String.valueOf(i);

                            //get the distance
                            double distance = gdistance(tasklat, tasklng, userlat, userlng, "K");

                            user.child(userID).child("tasks").child(key).child("distance").setValue(distance);
                            user.child(userID).child("tasks").child(key).child("lng").setValue(tasklng);
                            user.child(userID).child("tasks").child(key).child("lat").setValue(tasklat);
                            user.child(userID).child("tasks").child(key).child("prize").setValue(prize);
                            user.child(userID).child("tasks").child(key).child("location").setValue(location);
                            user.child(userID).child("tasks").child(key).child("description").setValue(description);
                            user.child(userID).child("tasks").child(key).child("worth").setValue(worth);
                            user.child(userID).child("tasks").child(key).child("altitude").setValue(taskaltitude);
                            user.child(userID).child("tasks").child(key).child("image").setValue(image);
                            user.child(userID).child("tasks").child(key).child("noWinners").setValue(noWinners);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        user.child(userID).child("tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                $distance1 = String.valueOf(dataSnapshot.child("0").child("distance").getValue());
                $distance2 = String.valueOf(dataSnapshot.child("1").child("distance").getValue());
                $distance3 = String.valueOf(dataSnapshot.child("2").child("distance").getValue());


                $distance1 = "+" + $distance1.substring(0,$distance1.indexOf(".")+2) + "km";
                $distance2 = "+" + $distance2.substring(0,$distance1.indexOf(".")+2) + "km";
                $distance3 = "+" + $distance3.substring(0,$distance1.indexOf(".")+2) + "km";

                tasks.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String $prize1 = String.valueOf(dataSnapshot.child("0").child("prize").getValue());
                        String $location1 = String.valueOf(dataSnapshot.child("0").child("location").getValue());
                        String $prize2 = String.valueOf(dataSnapshot.child("1").child("prize").getValue());
                        String $location2 = String.valueOf(dataSnapshot.child("1").child("location").getValue());
                        String $prize3 = String.valueOf(dataSnapshot.child("2").child("prize").getValue());
                        String $location3 = String.valueOf(dataSnapshot.child("2").child("location").getValue());

                        progressBar.setVisibility(View.INVISIBLE);

                        distance1.setText($distance1);
                        distance2.setText($distance2);
                        distance3.setText($distance3);


                        prize1.setText($prize1);
                        prize2.setText($prize2);
                        prize3.setText($prize3);

                        location1.setText($location1);
                        location2.setText($location2);
                        location3.setText($location3);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }



    private static double gdistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit == "K") {
                dist = dist * 1.609344;
            } else if (unit == "N") {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }


    private void setTotem() {
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totem = Integer.parseInt((String.valueOf(dataSnapshot.child(userID).child("totem").getValue())));
                if(totem == 0){
                    bar0.setVisibility(View.VISIBLE);
                    bar1.setVisibility(View.INVISIBLE);
                    bar2.setVisibility(View.INVISIBLE);
                    bar3.setVisibility(View.INVISIBLE);
                    bar4.setVisibility(View.INVISIBLE);
                    bar5.setVisibility(View.INVISIBLE);

                    star0.setVisibility(View.VISIBLE);
                    star1.setVisibility(View.INVISIBLE);
                    star2.setVisibility(View.INVISIBLE);
                    star3.setVisibility(View.INVISIBLE);
                    star4.setVisibility(View.INVISIBLE);
                    star5.setVisibility(View.INVISIBLE);
                }else if(totem == 1){
                    bar0.setVisibility(View.INVISIBLE);
                    bar1.setVisibility(View.VISIBLE);
                    bar2.setVisibility(View.INVISIBLE);
                    bar3.setVisibility(View.INVISIBLE);
                    bar4.setVisibility(View.INVISIBLE);
                    bar5.setVisibility(View.INVISIBLE);

                    star0.setVisibility(View.INVISIBLE);
                    star1.setVisibility(View.VISIBLE);
                    star2.setVisibility(View.INVISIBLE);
                    star3.setVisibility(View.INVISIBLE);
                    star4.setVisibility(View.INVISIBLE);
                    star5.setVisibility(View.INVISIBLE);
                }else if(totem == 2){
                    bar0.setVisibility(View.INVISIBLE);
                    bar1.setVisibility(View.INVISIBLE);
                    bar2.setVisibility(View.VISIBLE);
                    bar3.setVisibility(View.INVISIBLE);
                    bar4.setVisibility(View.INVISIBLE);
                    bar5.setVisibility(View.INVISIBLE);

                    star0.setVisibility(View.INVISIBLE);
                    star1.setVisibility(View.INVISIBLE);
                    star2.setVisibility(View.VISIBLE);
                    star3.setVisibility(View.INVISIBLE);
                    star4.setVisibility(View.INVISIBLE);
                    star5.setVisibility(View.INVISIBLE);
                }else if(totem == 3){
                    bar0.setVisibility(View.INVISIBLE);
                    bar1.setVisibility(View.INVISIBLE);
                    bar2.setVisibility(View.INVISIBLE);
                    bar3.setVisibility(View.VISIBLE);
                    bar4.setVisibility(View.INVISIBLE);
                    bar5.setVisibility(View.INVISIBLE);

                    star0.setVisibility(View.INVISIBLE);
                    star1.setVisibility(View.INVISIBLE);
                    star2.setVisibility(View.INVISIBLE);
                    star3.setVisibility(View.VISIBLE);
                    star4.setVisibility(View.INVISIBLE);
                    star5.setVisibility(View.INVISIBLE);
                }else if(totem == 4){
                    bar0.setVisibility(View.INVISIBLE);
                    bar1.setVisibility(View.INVISIBLE);
                    bar2.setVisibility(View.INVISIBLE);
                    bar3.setVisibility(View.INVISIBLE);
                    bar4.setVisibility(View.VISIBLE);
                    bar5.setVisibility(View.INVISIBLE);


                    star0.setVisibility(View.INVISIBLE);
                    star1.setVisibility(View.INVISIBLE);
                    star2.setVisibility(View.INVISIBLE);
                    star3.setVisibility(View.INVISIBLE);
                    star4.setVisibility(View.VISIBLE);
                    star5.setVisibility(View.INVISIBLE);
                }else{
                    bar0.setVisibility(View.INVISIBLE);
                    bar1.setVisibility(View.INVISIBLE);
                    bar2.setVisibility(View.INVISIBLE);
                    bar3.setVisibility(View.INVISIBLE);
                    bar4.setVisibility(View.INVISIBLE);
                    bar5.setVisibility(View.VISIBLE);

                    star0.setVisibility(View.INVISIBLE);
                    star1.setVisibility(View.INVISIBLE);
                    star2.setVisibility(View.INVISIBLE);
                    star3.setVisibility(View.INVISIBLE);
                    star4.setVisibility(View.INVISIBLE);
                    star5.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider,900000 , 10, NavigationActivity.this);

        }

        if(!isNetworkAvailable()){
            Toast.makeText(NavigationActivity.this, "No internet connection",
                    Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    protected void onPause() {
        super.onPause();

        //Detect idle screen
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider,900000 , 10, NavigationActivity.this);
        }




    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //UNBIND music service
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            locationManager.requestLocationUpdates(provider,900000 , 3, NavigationActivity.this);
        }

    }





    @Override
    protected  void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    // Stop the service when we are leaving this activity
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //gives location when users location is change

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        Double altitude = location.getAltitude();
        float bearing = location.getBearing();


        Log.i("Location info: Lat", lat.toString());
        Log.i("Location info: Lng", lng.toString());



        //users location has been change i think we should update it to the database asap

        //Get current Time
        Date currentDate = (Date) java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Lagos")).getTime();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        date = dateFormat.format(currentDate);
        user.child(userID).child("location").child("lng").setValue(String.valueOf(lng));
        user.child(userID).child("location").child("lat").setValue(String.valueOf(lat));
        user.child(userID).child("location").child("altitude").setValue(String.valueOf(altitude));
        user.child(userID).child("location").child("bearing").setValue(String.valueOf(bearing));
        user.child(userID).child("location").child("date").setValue(date);

        setNearestTasks();

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

    public String node(){
        int number1, number2;
        char a, b, c, d;
        final String nodeCode;

        Random rnd = new Random();
        a = (char) (rnd.nextInt(26) + 'A');
        b = (char) (rnd.nextInt(26) + 'A');
        c = (char) (rnd.nextInt(26) + 'A');
        d = (char) (rnd.nextInt(26) + 'A');
        number1 = (int) (10 * Math.random());
        number2 = (int) (10 * Math.random());
        nodeCode = a + "" + b + number1 + c + "" + number2 + d;


        return nodeCode;
    }
}
