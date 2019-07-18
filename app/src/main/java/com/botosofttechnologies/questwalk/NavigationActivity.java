package com.botosofttechnologies.questwalk;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.botosofttechnologies.questwalk.Service.MusicService;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationActivity extends AppCompatActivity implements LocationListener {

    ImageView bar0, bar1, bar2, bar3, bar4, bar5, star0, star1, star2, star3, star4, star5, settings, signOut, next, previous, ad;
    HomeWatcher mHomeWatcher;
    TextView location1, location2, location3, distance1, distance2, distance3,  prize1, prize2, prize3, text, adsText;
    Button viewMore;
    RelativeLayout layout;

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static FirebaseUser User = mAuth.getCurrentUser();
    static final String userID = User.getUid();

    LocationManager locationManager;
    String provider, date, key;

    FirebaseDatabase database;
    DatabaseReference user, tasks, ads;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    int $totem, taskCount, distance, adscount;

    double tasklng, tasklat, userlng, userlat;
    private Typeface header, subheading;

    Map<String, Double> distanceList = new HashMap<>();
    String $key;
    List<Double> rank = new ArrayList<Double>();
    Double refDistance;
    String mkey, $distance1, $distance2, $distance3, adName, $node;
    int setAd, views, totem, vCount;

    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

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
                    .setMessage("We need to access your location so in order for us to point you towards the right direction where a treasure is")
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

                if(locationNetwork != null){
                    onLocationChanged(locationNetwork);
                }else{
                }


            }
        }









    }

    private void initUi() {

        bar2 = (ImageView) findViewById(R.id.bar2);
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


        layout = (RelativeLayout) findViewById(R.id.layout);

        settings = (ImageView) findViewById(R.id.settings);
        signOut = (ImageView) findViewById(R.id.signOut);

        next = (ImageView) findViewById(R.id.next);
        previous = (ImageView) findViewById(R.id.previous);

        next.setVisibility(View.INVISIBLE);
        previous.setVisibility(View.INVISIBLE);

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
        mWave.setColor(R.color.colorAccent);
        progressBar.setIndeterminateDrawable(mWave);


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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


        setTotem();
        setNearestTasks();

        Thread t=new Thread(){


            @Override
            public void run(){

                while(!isInterrupted()){

                    try {
                        Thread.sleep(5000);  //1000ms = 1 sec

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



    }

    private void getAds(final int $setAd) {
        user.child(userID).child("ads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    adscount = (int) dataSnapshot.getChildrenCount();
                    String imageUrl = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("image").getValue());
                    final String view = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("view").getValue());
                    adName = String.valueOf(dataSnapshot.child(String.valueOf($setAd)).child("name").getValue());
                    $node = node();


                    if(setAd == (adscount - 1)){
                        Picasso.with(ad.getContext())
                                .load(imageUrl)
                                .into(ad);

                        if(view.equals("false") || view == null){
                            Toast.makeText(NavigationActivity.this, adName + "0:" + views, Toast.LENGTH_SHORT).show();
                            user.child(userID).child("ads").child(String.valueOf($setAd)).child("view").setValue("true");



                            if(totem == 5){
                                totem  = 4;
                            }


                            totem = totem + 1;
                            ads.child(adName).child($node).setValue(userID);
                            user.child(userID).child("totem").setValue(totem);


                        }
                        setAd = 0;
                    }else{
                        Picasso.with(ad.getContext())
                                .load(imageUrl)
                                .into(ad);

                        if(view.equals("false") || view == null){

                            Toast.makeText(NavigationActivity.this, adName + "0:" + views, Toast.LENGTH_SHORT).show();
                            user.child(userID).child("ads").child(String.valueOf($setAd)).child("view").setValue("true");


                            if(totem == 5){
                                totem  = 4;
                            }


                            totem = totem + 1;
                            ads.child(adName).child($node).setValue(userID);
                            user.child(userID).child("totem").setValue(totem);


                        }
                        setAd++;
                    }


                }else{
                    //no ads available
                    adsText.setVisibility(View.VISIBLE);
                    next.setVisibility(View.INVISIBLE);
                    previous.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                            key = String.valueOf(i);

                            //get the distance
                            double distance = gdistance(tasklat, tasklng, userlat, userlng, "K");

                            user.child(userID).child("tasks").child(key).child("distance").setValue(distance);

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

                $distance1 = "+" + $distance1 + "m";
                $distance2 = "+" + $distance2 + "m";
                $distance3 = "+" + $distance3 + "m";

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



        /*Collections.sort(rank);
        int j;
        for(j=0; j<3; j++){
            refDistance = rank.get(j);
            user.child(userID).child("tasks").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    taskCount = (int) dataSnapshot.getChildrenCount();
                    int i;
                    for(i=0; i <taskCount; i++ ){
                        mkey = String.valueOf(i);
                        double distance = (double) dataSnapshot.child(String.valueOf(i)).child("distance").getValue();
                        if(refDistance == distance && i == 0){
                            tasks.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String location = String.valueOf(dataSnapshot.child(mkey).child("location").getValue());
                                    String prize = String.valueOf(dataSnapshot.child(mkey).child("prize").getValue());
                                    location1.setText(location);
                                    String $distance = String.valueOf(Math.round(refDistance));
                                    distance1.setText($distance);
                                    prize1.setText(prize);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }else if(refDistance == distance && i == 1){
                            tasks.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String location = String.valueOf(dataSnapshot.child(mkey).child("location").getValue());
                                    String prize = String.valueOf(dataSnapshot.child(mkey).child("prize").getValue());
                                    location2.setText(location);
                                    String $distance = String.valueOf(Math.round(refDistance));
                                    distance2.setText($distance);
                                    prize2.setText(prize);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }else if(refDistance == distance && i == 2){
                            tasks.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String location = String.valueOf(dataSnapshot.child(mkey).child("location").getValue());
                                    String prize = String.valueOf(dataSnapshot.child(mkey).child("prize").getValue());
                                    location3.setText(location);
                                    String $distance = String.valueOf(Math.round(refDistance));
                                    distance3.setText($distance);
                                    prize3.setText(prize);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
*/


        /*List<Entry<String, Double>> sortedList = new ArrayList<Entry<String, Double>>(distanceList.entrySet());
        Collections.sort(sortedList, new Comparator<Entry<String, Double>>() {

            @Override
            public int compare(Entry<String, Double> obj1, Entry<String, Double> obj2) {
                return obj1.getValue().compareTo(obj2.getValue());

            }

        });

        int size1 = distanceList.size();
        int size2 = sortedList.size();
        Log.i("size1", String.valueOf(size1));
        Log.i("size2", String.valueOf(size2));

        int i = 0;
        for (Object e : sortedList) {
            Toast.makeText(NavigationActivity.this, "method 3 done", Toast.LENGTH_SHORT).show();
            if(i == 0){
                $key = ((Map.Entry<String, Integer>) e).getKey();
                distance = ((Map.Entry<String, Integer>) e).getValue();
                tasks.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String location = String.valueOf(dataSnapshot.child($key).child("location").getValue());
                        String prize = String.valueOf(dataSnapshot.child($key).child("prize").getValue());
                        Toast.makeText(NavigationActivity.this, "method 1st value", Toast.LENGTH_SHORT).show();
                        location1.setText(location);
                        distance1.setText(distance);
                        prize1.setText(prize);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                i++;
            }else if(i == 1){
                $key = ((Map.Entry<String, Integer>) e).getKey();
                distance = ((Map.Entry<String, Integer>) e).getValue();
                tasks.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String location = String.valueOf(dataSnapshot.child($key).child("location").getValue());
                        String prize = String.valueOf(dataSnapshot.child($key).child("prize").getValue());
                        Toast.makeText(NavigationActivity.this, "method 2nd value", Toast.LENGTH_SHORT).show();
                        location2.setText(location);
                        distance2.setText(distance);
                        prize2.setText(prize);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                i++;
            }else if(i == 2){
                $key = ((Map.Entry<String, Integer>) e).getKey();
                distance = ((Map.Entry<String, Integer>) e).getValue();
                tasks.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String location = String.valueOf(dataSnapshot.child($key).child("location").getValue());
                        String prize = String.valueOf(dataSnapshot.child($key).child("prize").getValue());

                        location3.setText(location);
                        distance3.setText(distance);
                        prize3.setText(prize);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                i++;
            }

        }

*/

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
            locationManager.requestLocationUpdates(provider,900000 , 10, NavigationActivity.this);
        }

    }





    @Override
    protected  void onStart() {
        super.onStart();

    }


    @Override
    public void onLocationChanged(Location location) {
        //gives location when users location is change

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        Double altitude = location.getAltitude();

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
        user.child(userID).child("location").child("date").setValue(date);

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
