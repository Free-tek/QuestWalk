package com.botosofttechnologies.questwalk.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.botosofttechnologies.questwalk.R;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskClicked extends AppCompatActivity {

    ImageView settings, signOut, help, bar0, star0, bar1, star1, bar2, star2, bar3, star3, bar4, star4, bar5, star5, placeImage;
    Button view, enterQuest;
    TextView location, description, distance, prize, worth, time;
    String key, endTime;

    FirebaseDatabase database;
    DatabaseReference user, tasks;

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static FirebaseUser User = mAuth.getCurrentUser();
    static final String userID = User.getUid();

    String $$distance, $$location, $$prize, $$worth, $$description, $$lng, $$lat, $$totem, $$image, $$entered, $$bearing, $$won, $$nameEnteredTask;
    int totem, count, $$noWinners;

    ProgressBar progressBar;
    private Typeface header, subheading;

    int availability = 0;
    String entered, activeTasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_clicked);

        Bundle bundle = getIntent().getExtras();
        key = bundle.getString("Key");

        database = FirebaseDatabase.getInstance();
        user = database.getReference("users");
        tasks = database.getReference("tasks");

        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entered = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("entered").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!isNetworkAvailable()){
            Toast.makeText(TaskClicked.this, "No internet connection",
                    Toast.LENGTH_SHORT).show();
        }

        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activeTasks = String.valueOf(dataSnapshot.child(userID).child("activeTasks").getValue());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        tasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                endTime = String.valueOf(dataSnapshot.child(key).child("endTime").getValue());

                //Check if the task has ended.



            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        initUi();


    }

    private void initUi() {

        progressBar = (ProgressBar)findViewById(R.id.progress);
        Wave mWave = new Wave();
        mWave.setBounds(0,0,100,100);
        mWave.setColor(R.color.orange);
        progressBar.setIndeterminateDrawable(mWave);

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


        placeImage = (ImageView) findViewById(R.id.placeImage);

        settings = (ImageView) findViewById(R.id.settings);
        signOut = (ImageView) findViewById(R.id.signOut);
        help = (ImageView) findViewById(R.id.help);

        location = (TextView) findViewById(R.id.location);
        description = (TextView) findViewById(R.id.description);
        distance = (TextView) findViewById(R.id.distance);
        prize = (TextView) findViewById(R.id.prize);
        worth = (TextView) findViewById(R.id.worth);
        
        time = (TextView) findViewById(R.id.time);

        view = (Button) findViewById(R.id.view);
        enterQuest = (Button) findViewById(R.id.enterQuest);

        view.setVisibility(View.INVISIBLE);
        enterQuest.setVisibility(View.INVISIBLE);

        header = Typeface.createFromAsset(getAssets(), "fonts/heading.ttf");
        subheading = Typeface.createFromAsset(getAssets(), "fonts/subheading.ttf");

        location.setTypeface(header);
        distance.setTypeface(header);
        prize.setTypeface(header);
        description.setTypeface(subheading);
        worth.setTypeface(subheading);

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
        setTime();


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                alertDialog.setTitle("Proceed?");
                alertDialog.setMessage("Do you want to proceed with sign out" );
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent signOut = new Intent (TaskClicked.this, LoginActivity.class);
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

        Thread t=new Thread(){


            @Override
            public void run(){

                while(!isInterrupted()){

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            setTime();
                        }
                    });

                }
            }
        };

        t.start();




        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                $$distance = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("distance").getValue());
                $$lng = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("lng").getValue());
                $$lat = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("lat").getValue());
                $$bearing = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("bearing").getValue());
                $$prize = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("prize").getValue());
                $$location = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("location").getValue());
                $$description = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("description").getValue());
                $$worth = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("worth").getValue());
                $$image = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("image").getValue());
                $$entered = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("entered").getValue());
                $$won = String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("won").getValue());
                $$noWinners = Integer.parseInt(String.valueOf(dataSnapshot.child(userID).child("tasks").child(key).child("noWinners").getValue()));
                $$totem = String.valueOf(dataSnapshot.child(userID).child("totem").getValue());
                $$nameEnteredTask = String.valueOf(dataSnapshot.child(userID).child("nameEnteredTasks").getValue());

                $$distance = $$distance.substring(0,$$distance.indexOf(".")+2);
                $$distance = "+" + $$distance + "km";
                location.setText($$location);
                description.setText($$description);
                distance.setText($$distance);
                prize.setText($$prize);
                worth.setText($$worth);

                view.setVisibility(View.VISIBLE);
                enterQuest.setVisibility(View.VISIBLE);


                Picasso.with(TaskClicked.this).load($$image)
                        .into(placeImage);

                if($$entered.equals("false")){
                    enterQuest.setVisibility(View.VISIBLE);
                    enterQuest.setText("Enter Quest");
                }else if($$entered.equals("true")){
                    enterQuest.setVisibility(View.VISIBLE);
                    enterQuest.setText("Quest Active");
                }

                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        enterQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime();
                Toast.makeText(TaskClicked.this, $$nameEnteredTask, Toast.LENGTH_SHORT).show();
                if(!$$nameEnteredTask.equals($$location)){
                    Toast.makeText(TaskClicked.this, "entered", Toast.LENGTH_SHORT).show();

                    //the user entered a task saved in this node before but not exactly this task itself
                    //so lets change his entered value to false
                    tasks.child(key).child("entered").removeValue();
                    tasks.child(key).child("entered").setValue("false");
                    tasks.child(key).child("won").removeValue();
                    tasks.child(key).child("won").setValue("false");
                    user.child(userID).child("activeTasks").removeValue();
                    user.child(userID).child("activeTasks").setValue("false");
                    user.child(userID).child("nameEnteredTasks").setValue($$location);
                    enterQuest.setText("Enter Quest");

                }
                if(totem < 5){
                    final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Oops...");
                    alertDialog.setMessage("You need a full totem bar to enter this quest, continue using the Quest Walk and viewing ads to increase your totem bar" );
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();
                }else if($$noWinners == 0){
                    final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Oops...");
                    alertDialog.setMessage("All the treasure chests in this location have been totally won" );
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();
                }else if ($$entered.equals("true")){
                    final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Yup...");
                    alertDialog.setMessage("You have entered this quest already go ahead and search" );
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();

                }else if(activeTasks.equals("true")){

                    final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Oops...");
                    alertDialog.setMessage("You cant enter 2 quests together you have to complete one first" );
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();
                } else if (availability == 1){
                    if(activeTasks.equals("false")){

                        final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                        alertDialog.setTitle("Confirm?");
                        alertDialog.setMessage("Will you like to enter to this quest" );
                        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


                                        final AlertDialog alertDialog2 = new AlertDialog.Builder(TaskClicked.this).create();
                                        alertDialog2.setTitle("Let the search begin");
                                        alertDialog2.setMessage("You have successfully entered this quest, use the map to navigate to the destination. Let the search begin!!!" );
                                        alertDialog2.setOnShowListener( new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface arg0) {
                                                alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                                                alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                                            }
                                        });
                                        alertDialog2.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        user.child(userID).child("activeTasks").setValue("true");
                                                        user.child(userID).child("nameEnteredTasks").setValue($$location);
                                                        user.child(userID).child("totem").setValue(0);
                                                        user.child(userID).child("tasks").child(key).child("entered").setValue("true");
                                                    }
                                                });

                                        alertDialog2.show();


                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();
                    }

                }else if(availability == 0){
                    final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Oops?");
                    alertDialog.setMessage("Event has passed or is yet to start" );
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            });
                    alertDialog.show();
                }else if(entered.equals("true")){
                    final AlertDialog alertDialog = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Yeh?");
                    alertDialog.setMessage("you have entered this activity already" );
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            });
                    alertDialog.show();
                }



            }
        });


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



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
                    final AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog.setTitle("Oops!!!");
                    alertDialog.setMessage("Quest is over.");
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Okay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setCancelable(false);
                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog.show();

                }else if(!entered.equals("true")){
                    final AlertDialog alertDialog2 = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog2.setTitle("Oops..");
                    alertDialog2.setMessage("You need to enter this quest first to view map" );
                    alertDialog2.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                            alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog2.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog2.show();

                }else if($$won.equals("true")){
                    final AlertDialog alertDialog2 = new AlertDialog.Builder(TaskClicked.this).create();
                    alertDialog2.setTitle("Oops..");
                    alertDialog2.setMessage("This quest is no longer available to you, since you won already" );
                    alertDialog2.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(TaskClicked.this.getResources().getColor(R.color.orange));
                        }
                    });
                    alertDialog2.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog2.show();

                } else{
                    Intent intent = new Intent(TaskClicked.this, MapsView.class);
                    intent.putExtra("Key", key);
                    startActivity(intent);
                }


            }
        });

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void setTime() {
        tasks.child(key).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String startDate = String.valueOf(dataSnapshot.child("startTime").getValue());
                String endDate = String.valueOf(dataSnapshot.child("endTime").getValue());

                Date d2 = null;
                Date d3 = null;

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
                Date currentDate = (Date) java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Lagos")).getTime();
                Date d1 = currentDate;
                try {
                    d2 = sdf.parse(startDate);
                    d3 = sdf.parse(endDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }



                if(d1.after(d3)){
                    //event has passed
                    time.setText(" off");
                    availability = 0;
                }else if(!d1.after(d3) && d1.after(d2)){
                    //event has started but is yet to close

                    long diff = d3.getTime() - d1.getTime();
                    int diffSeconds = (int) (diff / 1000);
                    int diffMinutes = (int) (diff / (60 * 1000));
                    int diffHours = (int)(diff / (60 * 60 * 1000));

                    if(diffMinutes == 0){
                        time.setText("-" + diffSeconds + "s");
                        availability = 1;
                    }else if(diffMinutes/ (60 * 24) >= 1){
                        int diffdays = diffHours / 24;
                        time.setText(diffdays + "d" );
                        availability = 1;
                    } else if(diffMinutes / 60 >= 1){
                        int diffmin = diffMinutes - (diffHours * 60);
                        time.setText("-" + diffHours + "h:" + diffmin + "m");
                        availability = 1;
                    }else if(diffMinutes / 60 < 1){
                        time.setText("-" + diffMinutes + "m");
                        availability = 1;
                    }

                } else if(d2.after(d1)){
                    //event is yet to start
                    long diff = d2.getTime() - d1.getTime();
                    int diffSeconds = (int) (diff / 1000);
                    int diffMinutes = (int) (diff / (60 * 1000));
                    int diffHours = (int) (diff / (60 * 60 * 1000));

                    if(diffMinutes == 0){
                        time.setText(diffSeconds + "s");
                        availability = 0;
                    }else if(diffMinutes/ (60 * 24) >= 1){
                        int diffdays = diffHours / 24;
                        time.setText(diffdays + "d" );
                        availability = 0;
                    }else if(diffMinutes / 60 >= 1){
                        int diffmin = diffMinutes - (diffHours * 60);
                        time.setText(diffHours + "h:" + diffmin + "m");
                        availability = 0;
                    }else if(diffMinutes / 60 < 1){
                        time.setText(diffMinutes + "m");
                        availability = 0;
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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


    @Override
    protected void onResume() {
        super.onResume();

        if(!isNetworkAvailable()){
            Toast.makeText(TaskClicked.this, "No internet connection",
                    Toast.LENGTH_SHORT).show();
        }



    }


    @Override
    protected void onPause() {
        super.onPause();



    }

}
