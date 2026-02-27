package com.rozaki.android.speedometer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import static android.R.color.background_dark;

//the main activity implements the Interface LocationListener in order to receive notifications when location changes
public class MainActivity extends AppCompatActivity implements LocationListener {
    //initialize UI
    Button button,button2;
    ImageButton button3;
    TextView textView;

    //for gps
    LocationManager locationManager;

    //for shared preferences
    SharedPreferences preferences;

    //for database
    SQLiteDatabase db;

    //class for speech
    MyTts myTts;

    boolean flag;

    private static final int REC_RESULT = 653;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.speedview);

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked it will run this function
                set_speed_limit();
            }
        });
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked it will run this function
                open_history_button();
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //create a database "SPEEDOMETER" and a table to keep track of speed violations
        db = openOrCreateDatabase("SPEEDOMETER", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS HISTORY(x DOUBLE,y DOUBLE,speed FLOAT,date DATE,time TIME)");

       // button2.setBackgroundColor(Color.parseColor("#353535"));
       setActivityBackgroundColor(Color.parseColor("#353535"));

       //speech
        myTts = new MyTts(this);

        //if the location permission is already accepteed then we proceed to start the application
       if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        button.setText("start");

        //else we tell user to open location
        else
            button.setText("enable location");
        flag=true;
             }
      @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            //save the possible strings user might say in an arraylist called matches
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //speech recognition in order to open every button
            if (matches.contains("start") || matches.contains("exit") || matches.contains("enable"))
                button.performClick();
            if (matches.contains("history"))
                button3.performClick();
            if (matches.contains("set speed limit"))
                button2.performClick();
        }

    }

    public void recognize(View view){
        //opens speech recognizer activity
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Enable,start/exit, set speed limit or history"); //tell user what to speak
        startActivityForResult(intent,REC_RESULT);
    }

    //a function to opens a new activity
    public void set_speed_limit() {
        Intent intent = new Intent(this, SpeedLimit.class);
        startActivity(intent);
    }

    public void start(View view) {
        //if the permission for location access is given and name is start then request for location updates and change name button to exit
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && button.getText()=="start"){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0,this);//we request location updates
            button.setText("exit");
            button.setTextColor(Color.RED);

        } //if the permission for location access is given and name is start then we close the app
        else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && button.getText()=="exit"){
                System.exit(0);
        } //if permission is not giver then request for it
        else if (button.getText() == "enable location"){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},6);
            button.setText("start");
            button.setTextColor(Color.GREEN);
        }

    }

//a function to change color
    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

//a function to open history activity
    public void open_history_button() {
        Intent intent = new Intent(this, History.class);
        startActivity(intent);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
//saving coordinates and initialise current speed
        double x = location.getLatitude();
        double y = location.getLongitude();
        float current_speed = 0;

//display speed
        if (location == null) {
            textView.setText("-.-");
        } else {
           current_speed = location.getSpeed()*3.6f;
           textView.setText(String.format("%.2f", (current_speed)));
        }

       speed_violation(x,y,current_speed);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    //if gps open
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    //if gps closes
    }

    //a function to checks speed violations
    public void speed_violation(double x,double y, float speed){
        //write the speed limit using shared preference
        String s = preferences.getString("mykey1", "60");
        //save it and cast it
        double speed_limit = Double.parseDouble(s);

        //if current speed exceeds limit
        if (speed > speed_limit && flag == true) {
            flag = false; // flag in order to prevent loop
            myTts.speak("Speed Limit Violation"); //text to speech
            setActivityBackgroundColor(Color.RED);//change activity color

            LocalDate date= LocalDate.now(); // create a date object
            LocalTime time = LocalTime.now();// create a time object
            Toast.makeText(this, "WARNING: DANGER.", Toast.LENGTH_LONG).show();

            // we keep track of the information when the limit violation happened
            //the recording inserted into the history table
            db.execSQL("INSERT INTO HISTORY VALUES ('"+x+"', '"+y+"', '"+speed+"', '"+ date+"', '"+time+"');");
            try { //keep track of timestamp to let users know
                String timeStamp = java.text.DateFormat.getDateTimeInstance().format(new Date());
                Toast.makeText(this,"You exceeded max speed at "+ timeStamp,Toast.LENGTH_SHORT).show();
            } catch(Exception e) {}

        }
        else if( speed <= speed_limit && flag == false){ //if speed not violates limit anymore the flag is again true
            flag = true;
            setActivityBackgroundColor(Color.parseColor("#353535"));
        }

    }
}