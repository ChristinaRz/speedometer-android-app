package com.rozaki.android.speedometer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;

public class History extends AppCompatActivity {

    //database
    SQLiteDatabase db;

    //ui
    Button button1,button2,button3;
    private StringBuilder text = new StringBuilder();

    //date
    LocalDate current = LocalDate.now();

    //speech
    MyTts myTts;
    private static final int REC_RESULT = 653;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        myTts = new MyTts(this);
        button1 = findViewById(R.id.button4);
        button2 = findViewById(R.id.button5);
        button3 = findViewById(R.id.button6);

        //create a database "SPEEDOMETER" and a table to keep track of speed violations
        db = openOrCreateDatabase("SPEEDOMETER", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS HISTORY(x DOUBLE,y DOUBLE,speed FLOAT,date DATE,time TIME)");

        setActivityBackgroundColor(Color.parseColor("#353535"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //voice recognition to call history
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.contains("all"))
                button1.performClick();
            if (matches.contains("yesterday"))
                button2.performClick();
            if (matches.contains("last month"))
                button3.performClick();
        }

    }


    public void recognize(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"All,yesterday or last week"); //the possible choices
        startActivityForResult(intent,REC_RESULT);
    }
    //a function to change color
    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    //history
    public void all_history(View view){
        //in the query we select everything
        Cursor cursor = db.rawQuery("SELECT * FROM HISTORY",null);
        //all the records writed
        if (cursor.getCount()>0){
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()){
                builder.append("x:").append(cursor.getDouble(0)).append("\n");
                builder.append("y:").append(cursor.getDouble(1)).append("\n");
                builder.append("Speed:").append(cursor.getFloat(2)).append("\n");
                builder.append("Date:").append(cursor.getString(3)).append("\n");
                builder.append("-----------------------------------\n");
            }
            showMessage("GPS HISTORY",builder.toString());
        }
    }
    public void yesterday(View view){

        LocalDate x = current.minusDays(1);
        //Toast.makeText(this, x.toString(), Toast.LENGTH_LONG).show();

        Cursor cursor = db.rawQuery("SELECT * FROM HISTORY WHERE date >= (SELECT date('now','-1 day'))",null);
        //   Cursor cursor = db.rawQuery("SELECT * FROM HISTORY WHERE  strftime('%s', date) BETWEEN strftime('%s'," + x +") AND strftime('%s',"+ current+") ",null);



        if (cursor.getCount()>0){
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()){
                builder.append("x:").append(cursor.getDouble(0)).append("\n");
                builder.append("y:").append(cursor.getDouble(1)).append("\n");
                builder.append("Speed:").append(cursor.getFloat(2)).append("\n");
                builder.append("Date:").append(cursor.getString(3)).append("\n");
                builder.append("Date:").append(cursor.getString(4)).append("\n");
                builder.append("Time:").append(cursor.getString(4)).append("\n");
                builder.append("-----------------------------------\n");
            }
            showMessage("GPS HISTORY",builder.toString());
        }
    }

    public void month_history(View view){
        LocalDate x = current.minusMonths(1);
        Cursor cursor = db.rawQuery("SELECT * FROM HISTORY WHERE date >= (SELECT date('now','-1 month'))",null);
        if (cursor.getCount()>0){
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()){
                builder.append("x:").append(cursor.getDouble(0)).append("\n");
                builder.append("y:").append(cursor.getDouble(1)).append("\n");
                builder.append("Speed:").append(cursor.getFloat(2)).append("\n");
                builder.append("Date:").append(cursor.getString(3)).append("\n");
                builder.append("Time:").append(cursor.getString(4)).append("\n");
                builder.append("-----------------------------------\n");
            }
            showMessage("GPS HISTORY",builder.toString());
        }
    }

    public void week_history(View view){
        LocalDate x = current.minusMonths(1);
        Cursor cursor = db.rawQuery("SELECT * FROM HISTORY WHERE date >= (SELECT date('now','-7 days'))",null);
        if (cursor.getCount()>0){
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()){
                builder.append("x:").append(cursor.getDouble(0)).append("\n");
                builder.append("y:").append(cursor.getDouble(1)).append("\n");
                builder.append("Speed:").append(cursor.getFloat(2)).append("\n");
                builder.append("Date:").append(cursor.getString(3)).append("\n");
                builder.append("Time:").append(cursor.getString(4)).append("\n");
                builder.append("-----------------------------------\n");
            }
            showMessage("GPS HISTORY",builder.toString());
        }
    }

    //this function show the history as an alert box with builber help
    //
    public void showMessage(String gps_history, String toString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle(gps_history)
                .setMessage(toString)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })
        .show();

    }}