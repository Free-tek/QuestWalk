package com.botosofttechnologies.questwalk.Activities;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.botosofttechnologies.questwalk.AltitudeFinder;
import com.botosofttechnologies.questwalk.DirectionsParser;
import com.botosofttechnologies.questwalk.R;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MapsView extends FragmentActivity implements OnMapReadyCallback, LocationListener, SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 555;

    int ACCESS_FINE_LOCATION_CODE = 3310;
    int ACCESS_COARSE_LOCATION_CODE = 3410;
    private GoogleApiClient mGoogleApiClient;


    private GoogleMap mMap;
    ArrayList<LatLng> listPoints;

    LatLng point, origin;

    LocationManager locationManager;
    String provider, $lat, $lng, key;

    ImageView walking, driving;

    FirebaseDatabase database;
    DatabaseReference user, task;

    Context context;
    int $$bearing, $bearing, mode, noWinners;

    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static FirebaseUser User = mAuth.getCurrentUser();
    static final String userID = User.getUid();

    Double ulat, ulng, ubearing, ualtitude;
    Polyline polyline;
    String lng, lat, bearing, altitude, won, startTime, endTime, $provider;

    Double dlat, dlng, dbearing, daltitude, maltitude;
    int check;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_view);

        buildGoogleApiClient();


        Bundle bundle = getIntent().getExtras();
        key = bundle.getString("Key");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        if(!isNetworkAvailable()){
            Toast.makeText(MapsView.this, "No internet connection",
                    Toast.LENGTH_SHORT).show();
        }


        listPoints = new ArrayList<>();


        database = FirebaseDatabase.getInstance();
        user = database.getReference("users");
        task = database.getReference("tasks");


        mode = 0;


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        initUi();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("We need to access your location in order for us to point you towards the right direction where you can find a treasure")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MapsView.this,
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


        task.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lng = String.valueOf(dataSnapshot.child(key).child("lng").getValue());
                lat = String.valueOf(dataSnapshot.child(key).child("lat").getValue());
                bearing = String.valueOf(dataSnapshot.child(key).child("bearing").getValue());

                addMarker("walking");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        check = 0;


        Thread t = new Thread() {


            @Override
            public void run() {

                while (!isInterrupted()) {

                    try {
                        Thread.sleep(10000);

                        runOnUiThread(new Runnable() {

                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void run() {
                                if (check != 0) {
                                    checkMatch();

                                }
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        t.start();
    }

    private void checkMatch() {

        //Toast.makeText(MapsView.this, "entered check match", Toast.LENGTH_SHORT).show();
        task.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lng = String.valueOf(dataSnapshot.child(key).child("lng").getValue());
                lat = String.valueOf(dataSnapshot.child(key).child("lat").getValue());
                bearing = String.valueOf(dataSnapshot.child(key).child("bearing").getValue());
                altitude = String.valueOf(dataSnapshot.child(key).child("altitude").getValue());
                won = String.valueOf(dataSnapshot.child(key).child("won").getValue());
                endTime = String.valueOf(dataSnapshot.child(key).child("endTime").getValue());

                dlat = Double.valueOf(lat);
                dlng = Double.valueOf(lng);
                dbearing = Double.valueOf(bearing);
                daltitude = Double.valueOf(altitude);


                //Check if the task has ended.

                Date d3 = null;

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
                Date currentDate = (Date) java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Lagos")).getTime();
                Date d1 = currentDate;
                try {
                    d3 = sdf.parse(endTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if (d1.after(d3)) {
                    //event has passed
                    final AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(MapsView.this).create();
                    alertDialog.setTitle("Oops!!!");
                    alertDialog.setMessage("Quest is over.");
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Okay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(MapsView.this, TaskClicked.class);
                                    intent.putExtra("Key", key);
                                    startActivity(intent);
                                }
                            });
                    alertDialog.setCancelable(false);
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(MapsView.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(MapsView.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.show();

                }

                checkMatch2();




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void checkMatch2() {

        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ulat = Double.valueOf(String.valueOf(dataSnapshot.child(userID).child("location").child("lat").getValue()));
                ulng = Double.valueOf(String.valueOf(dataSnapshot.child(userID).child("location").child("lng").getValue()));
                ubearing = Double.valueOf(String.valueOf(dataSnapshot.child(userID).child("location").child("bearing").getValue()));
                ualtitude = Double.valueOf(String.valueOf(dataSnapshot.child(userID).child("location").child("altitude").getValue()));

                double a = gdistance(dlat, dlng, ulat, ulng, "K");

                if (a <= 0.01) {
                    mMap.clear();
                    point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                    mMap.addMarker(new MarkerOptions().position(point).title("Prize").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18.0f));

                }


                BigDecimal bd1 = new BigDecimal(dlat).setScale(5, RoundingMode.HALF_UP);
                dlat = bd1.doubleValue();

                BigDecimal bd2 = new BigDecimal(ulat).setScale(5, RoundingMode.HALF_UP);
                ulat = bd2.doubleValue();

                BigDecimal bd3 = new BigDecimal(dlng).setScale(5, RoundingMode.HALF_UP);
                dlng = bd3.doubleValue();

                BigDecimal bd4 = new BigDecimal(ulng).setScale(5, RoundingMode.HALF_UP);
                ulng = bd4.doubleValue();


                if (dlat >= (ulat - 0.00001) && dlat <= (ulat + 0.00001 )) {

                    if (dlng >= (ulng - 0.00001) && dlng <= (ulng + 0.000001)) {
                        //point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));


/*
                                if (dbearing >= (ubearing - 50) && dbearing <= (ubearing + 50)) {
                                    //Toast.makeText(MapsView.this, "dbearing" + dbearing + "dbearing" + ubearing, Toast.LENGTH_SHORT).show();
                                    if (ualtitude >= daltitude + 30 && ualtitude <= daltitude + 30) {
                                        //Toast.makeText(MapsView.this, "talt" + daltitude + "ualt" + ualtitude, Toast.LENGTH_SHORT).show();

*/
                        check = 0;
                        final AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(MapsView.this).create();
                        alertDialog.setTitle("Congratulations!!!");
                        alertDialog.setMessage("You have found the treasure.");
                        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Claim",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Intent intent = new Intent(MapsView.this, TaskClicked.class);
                                        intent.putExtra("Key", key);
                                        String node = getNode();
                                        task.child(key).child("winners").child(node).setValue(userID);
                                        user.child(userID).child("tasks").child(key).child("won").setValue("true");
                                        user.child(userID).child("activeTasks").setValue("false");



                                        //somebody just won the task lets change the lng and lat and altitude so that the user doesnt bring his friends to thesame location
                                        String newlngdigits = getRand();
                                        String newLng = lng.substring(0, lng.indexOf(".") + 5) + newlngdigits;

                                        String newlatdigits = getRand();
                                        String newLat = lat.substring(0, lat.indexOf(".") + 5) + newlatdigits;

                                        noWinners--;

                                        user.child(userID).child("noWinners").setValue(noWinners);
                                        task.child(key).child("noWinners").setValue(noWinners);
                                        task.child(key).child("lat").setValue(newLat);
                                        task.child(key).child("lng").setValue(newLng);


                                        startActivity(intent);
                                        dialog.dismiss();


                                    }
                                });
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(MapsView.this.getResources().getColor(R.color.orange));
                            }
                        });
                        alertDialog.setCancelable(false);

                        alertDialog.show();
/*
                                    }
                                }*/
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initUi() {
        walking = (ImageView) findViewById(R.id.walking);
        driving = (ImageView) findViewById(R.id.driving);

        driving.setVisibility(View.VISIBLE);
        walking.setVisibility(View.INVISIBLE);

        walking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driving.setVisibility(View.VISIBLE);
                walking.setVisibility(View.INVISIBLE);
                mode = 0;
                addMarker("walking");
            }
        });

        driving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walking.setVisibility(View.VISIBLE);
                driving.setVisibility(View.INVISIBLE);
                mode = 1;
                addMarker("driving");
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void addMarker(final String mode) {

        mMap.clear();

        point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

        mMap.addMarker(new MarkerOptions().position(point).title("Prize").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18.0f));

        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double userLng = Double.parseDouble(String.valueOf(dataSnapshot.child(userID).child("location").child("lng").getValue()));
                double userLat = Double.parseDouble(String.valueOf(dataSnapshot.child(userID).child("location").child("lat").getValue()));
                String userAltitude = String.valueOf(dataSnapshot.child(userID).child("location").child("altitude").getValue());
                String bearing = String.valueOf(dataSnapshot.child(userID).child("location").child("bearing").getValue());
                noWinners = Integer.parseInt(String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("noWinners").getValue()));


                origin = new LatLng(userLat, userLng);
                String url = getRequestUrl(origin, point, mode);
                TaskRequestDirection taskRequestDirections = new TaskRequestDirection();
                taskRequestDirections.execute(url);
                check = 1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private String getRequestUrl(LatLng origin, LatLng destination, String $mode) {
        //Value for origin
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        //value of destination
        String str_destination = "destination=" + destination.latitude + "," + destination.longitude;

        //Set value enable the sensor
        String sensor = "sensor=false";

        //Mode for find direction
        String mode = "mode=" + $mode;

        //Directions API key
        String directionsApi = "AIzaSyCnQT-aqdhBqqrP4xuAfl7fXzeMa1_cHYA";

        //build full parameter
        String param = str_origin + "&" + str_destination + "&" + sensor + "&" + mode;

        //output format
        String output = "json";

        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param + "&key=" + directionsApi;
        return url;
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

    private String requestDirection(String requestUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(requestUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //GET the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();

        }
        return responseString;
    }

    public class TaskRequestDirection extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";

            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //parsing json here
            //Log.i("API ERROR", s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser dataParser = new DirectionsParser();
                routes = dataParser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route display into map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;
            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(((point.get("lat"))));
                    double lng = Double.parseDouble(((point.get("lon"))));

                    points.add(new LatLng(lat, lng));


                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(getResources().getColor(R.color.orange));
                polylineOptions.geodesic(true);
            }


            if (polylineOptions != null) {
                /*mMap.addPolyline(polylineOptions);*/
                polyline = mMap.addPolyline(polylineOptions);


            } else {
                Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        String json = "[\n" +
                "  {\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#242f3e\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#746855\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#242f3e\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"administrative.locality\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#d59563\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#d59563\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi.park\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#263c3f\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi.park\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#6b9a76\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#38414e\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#212a37\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#9ca5b3\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#746855\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#1f2835\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#f3d19c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"transit\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#2f3948\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"transit.station\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#d59563\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#17263c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#515c6d\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#17263c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";




        MapStyleOptions style = new MapStyleOptions(json);
        mMap.setMapStyle(style);

        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            } else {

                mMap.setMyLocationEnabled(true);

            }
        }
        /*drawMarker(origin);*/
    }

    @Override
    public void onLocationChanged(Location location) {
        ulat = location.getLatitude();
        ulng = location.getLongitude();
        Double altitude = location.getAltitude();
        String provider = location.getProvider();
        float bearing = location.getBearing();
        long time = location.getTime();
        float speed = location.getSpeed();

        //updateAll(location);
        /*parseNmeaString();*/


        user.child(userID).child("location").child("lng").setValue(String.valueOf(ulng));
        user.child(userID).child("location").child("lat").setValue(String.valueOf(ulat));
        user.child(userID).child("location").child("altitude").setValue(String.valueOf(altitude));
        user.child(userID).child("location").child("bearing").setValue(String.valueOf(bearing));




        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(5);
        $lat = String.valueOf(df.format(ulat));
        $lng = String.valueOf(df.format(ulng));
        $bearing = (int) bearing;


        origin = new LatLng(ulat, ulng);
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


    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        currentDegree = degree;

        user.child(userID).child("location").child("degree").setValue(String.valueOf(degree));

    }






    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }


    private void drawMarker(LatLng point) {
        // Clears all the existing coordinates
        mMap.clear();

        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting latitude and longitude for the marker
        markerOptions.position(point);

        // Setting title for the InfoWindow
        markerOptions.title("Position");

        // Setting InfoWindow contents
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        // Adding marker on the Google Map
        mMap.addMarker(markerOptions);

        // Moving CameraPosition to the user input coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));


    }

    @Override
    protected void onResume() {
        super.onResume();

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
            locationManager.requestLocationUpdates(provider,90 , 1, MapsView.this);

        }


        /*point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

        mMap.addMarker(new MarkerOptions().position(point).title("Prize").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18.0f));*/

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);


        if(!isNetworkAvailable()){
            Toast.makeText(MapsView.this, "No internet connection",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

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
            locationManager.requestLocationUpdates(provider,90 , 10, MapsView.this);
        }



        // to stop the listener and save battery
        mSensorManager.unregisterListener((SensorEventListener) this);



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(MapsView.this, TaskClicked.class);
        intent.putExtra("Key", key);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


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
            locationManager.requestLocationUpdates(provider,90 , 1, MapsView.this);
        }

    }





    @Override
    protected  void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }


    public String getNode() {
        int number1, number2;
        char a, b, c, d;
        final String node;

        Random rnd = new Random();
        a = (char) (rnd.nextInt(26) + 'A');
        b = (char) (rnd.nextInt(26) + 'A');
        c = (char) (rnd.nextInt(26) + 'A');
        d = (char) (rnd.nextInt(26) + 'A');
        number1 = (int) (10 * Math.random());
        number2 = (int) (10 * Math.random());
        node = a + "" + b + number1 + c + "" + number2 + d;

        return node;
    }

    public String getRand() {
        int number1, number2;
        final String node;

        Random rnd = new Random();
        number1 = (int) (10 * Math.random());
        number2 = (int) (10 * Math.random());
        node = number1 + "" + number2;

        return node;
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
                                if (ActivityCompat.checkSelfPermission(MapsView.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsView.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MapsView.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
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
                            status.startResolutionForResult(MapsView.this, REQUEST_RESOLVE_ERROR);
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
                                        ActivityCompat.requestPermissions(MapsView.this,
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
        NavigationActivity.ErrorDialogFragment dialogFragment = new NavigationActivity.ErrorDialogFragment();
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
}
