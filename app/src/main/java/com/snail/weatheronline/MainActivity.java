package com.snail.weatheronline;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.net.URL;

/** Launched class of application */
public class MainActivity extends AppCompatActivity {

    /** textView of city name */
    private TextView textViewCity;
    /** textView of weather description in city */
    private TextView textViewDescription;
    /** textView of temperature in city */
    private TextView textViewTemperature;
    /** textView of humidity in city */
    private TextView textViewWater;
    /** textView of wind in city */
    private TextView textViewWindy;
    /** textView JSON answer from server */
    private TextView textViewJSON;
    /** editText input city */
    private EditText editTextCityName;

    /** Method onCreate activity person register
     *
     * @param savedInstanceState saved last activity
     */
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
        bGetWeather.setOnClickListener(v -> {
            String city = GetCity();
            if (city.equals("")) {
                editTextCityName.setError("Введите город");
            } else {
                makeRequest(city);
                closeKeyboard();
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

    /**Get city name from editText
     *
     * @return String city name from editText
     */
    private String GetCity() {
        return editTextCityName.getText().toString().trim();
    }

    /**Make request api openweathermap
     *
     * @param city name city
     */
    private void makeRequest(String city) {
        String api_key = getResources().getString(R.string.apiWeatherOnline);
        if (api_key.equals("")) {
            Toast.makeText(this, "Укажите api ключ", Toast.LENGTH_LONG).show();
            return;
        }
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + api_key + "&units=metric&lang=ru";

        new GetWeatherOnline().execute(url);
    }

    /** AsyncTask to make request to api and set data */
    @SuppressLint("StaticFieldLeak")
    private class GetWeatherOnline extends AsyncTask<String, String, String> {

        /**Runs on the UI thread before doInBackground
         *
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewCity.setText("Идет запрос...");
        }

        /**Override this method to perform a computation on a background thread.
         *
         * @param strings The parameters of the task.
         * @return A result, defined by the subclass of this task.
         */
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader    bufRead    = null;

            Log.i("AsyncTask connect", strings[0]);

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream;
                try {
                    stream = connection.getInputStream();
                } catch(IOException exception) {
                    stream = connection.getErrorStream();
                }

                bufRead = new BufferedReader(new InputStreamReader(stream));
                StringBuilder strBuff = new StringBuilder();
                String line;

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

        /**Runs on the UI thread after doInBackground
         *
         * @param s The result of the operation computed by doInBackground
         */
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

        /**Set weather to textView
         *
         * @param response JSON string
         * @throws JSONException exception
         */
        private void setWeather(String response) throws JSONException {
            JSONObject weatherJson = new JSONObject(response);

            if (weatherJson.getString("cod").equals("404")) {
                textViewCity.setText("Неверно указан город");
                return;
            }

            String city  = weatherJson.getString("name");

            String description = getResources().getString(R.string.textDescriptionStringResource) +
                                    weatherJson.getJSONArray("weather").getJSONObject(0).getString("description");

            String temp  = getResources().getString(R.string.textTemperatureStringResource) +
                    weatherJson.getJSONObject("main").getDouble("temp") + "\u00B0" + "C";

            String water = getResources().getString(R.string.textWaterStringResource) +
                    weatherJson.getJSONObject("main").getDouble("humidity") + "%";

            String wind  = getResources().getString(R.string.textWindStringResource) +
                    weatherJson.getJSONObject("wind").getDouble("speed") + "м/с";

            textViewCity.setText(city);
            textViewDescription.setText(description);
            textViewTemperature.setText(temp);
            textViewWater.setText(water);
            textViewWindy.setText(wind);
        }
    }
}