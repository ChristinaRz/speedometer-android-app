package com.rozaki.android.speedometer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.InputMismatchException;

//this activity is to set up speed limit
public class SpeedLimit extends AppCompatActivity {

    //init
    Button button;
    TextView textView;
    SharedPreferences preferences;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_limit);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        button = findViewById(R.id.apply_button);
        editText = findViewById(R.id.editLim);
        textView = findViewById(R.id.textView);

//write the previus speed limit to have it as a default
        String s = preferences.getString("mykey1", "60");
        editText.setText(s);

        setActivityBackgroundColor(Color.parseColor("#353535"));
    }

    //a function to change color
    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);}

    //handle all possible wrong use inputs
    public void apply(View view) {
        if (editText.getText().toString().isEmpty())
            Toast.makeText(this, "Please write a limit.", Toast.LENGTH_LONG).show();
        else if (editText.getText().toString().equals(" "))
            Toast.makeText(this, "Don't use space letter.", Toast.LENGTH_LONG).show();
        else if (editText.getText().toString().startsWith("."))
            Toast.makeText(this, "Please write a limit.", Toast.LENGTH_LONG).show();
        else {
            try {
                //if input is right then we read the value on a shared preference
                //on a try catch to handle wrong input exception
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("mykey1", editText.getText().toString());
                editor.apply();
                //message print
                Toast.makeText(this, "Speed Limit Changed.", Toast.LENGTH_LONG).show();
            } catch (InputMismatchException ime) {
                Toast.makeText(this, "invalid input.", Toast.LENGTH_LONG).show();

            }
        }


    }}