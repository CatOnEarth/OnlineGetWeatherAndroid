package com.snail.weatheronline;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import leakcanary.LeakCanary;

public class MainActivity extends AppCompatActivity {

    private TextView textViewCity;
    private TextView textViewDescription;
    private TextView textViewTemperature;
    private TextView textViewWater;
    private TextView textViewWindy;
    private TextView textViewJSON;
    private EditText editTextCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCityName = findViewById(R.id.editTextNameCity);

        textViewCity        = findViewById(R.id.textViewNameCity);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewWater       = findViewById(R.id.textViewWater);
        textViewJSON        = findViewById(R.id.textViewJSONAns);
        textViewWindy       = findViewById(R.id.textViewWind);

        ImageButton bGetWeather = findViewById(R.id.imageButton);
        bGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = GetCity();
                if (city.equals("")) {
                    editTextCityName.setError("Введите город");
                } else {
                    makeRequest(city);
                    closeKeyboard();
                }
            }
        });
    }

    /**Hide keyboard
     *
     */
    private void closeKeyboard() {
        View view = getCurrentFocus();

        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private String GetCity() {
        return editTextCityName.getText().toString().trim();
    }

    private void makeRequest(String city) {
        String api_key = getResources().getString(R.string.apiWeatherOnline);
        if (api_key.equals("")) {
            Toast.makeText(this, "Укажите api ключ", Toast.LENGTH_LONG).show();
            return;
        }
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + api_key + "&units=metric&lang=ru";

        new GetWeatherOnline().execute(url);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetWeatherOnline extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewCity.setText("Идет запрос...");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader    bufRead    = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                bufRead = new BufferedReader(new InputStreamReader(stream));

                StringBuffer strBuff = new StringBuffer();
                String line = "";

                while ((line = bufRead.readLine()) != null) {
                    strBuff.append(line).append("\n");
                }

                return strBuff.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (bufRead != null) {
                        bufRead.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            textViewJSON.setText(s);
            try {
                setWeather(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void setWeather(String response) throws JSONException {
            JSONObject weatherJson = new JSONObject(response);

            String city  = weatherJson.getString("name");

            String description = getResources().getString(R.string.textDescriptionStringResource) +
                                    weatherJson.getJSONArray("weather").getJSONObject(0).getString("description");

            String temp  = getResources().getString(R.string.textTemperatureStringResource) +
                            String.valueOf(weatherJson.getJSONObject("main").getDouble("temp")) + "\u00B0" + "C";

            String water = getResources().getString(R.string.textWaterStringResource) +
                            String.valueOf(weatherJson.getJSONObject("main").getDouble("humidity")) + "%";

            String wind  = getResources().getString(R.string.textWindStringResource) +
                    String.valueOf(weatherJson.getJSONObject("wind").getDouble("speed")) + "м/с";

            textViewCity.setText(city);
            textViewDescription.setText(description);
            textViewTemperature.setText(temp);
            textViewWater.setText(water);
            textViewWindy.setText(wind);
        }
    }
}