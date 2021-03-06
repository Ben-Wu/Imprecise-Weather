package benwu.weatherapp.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import benwu.weatherapp.R;
import benwu.weatherapp.utils.AerisWeatherHelper;
import benwu.weatherapp.data.DataManager;
import benwu.weatherapp.utils.OpenWeatherHelper;
import benwu.weatherapp.utils.WUndergroundHelper;
import benwu.weatherapp.data.WeatherDataObject;
import benwu.weatherapp.utils.WorldWeatherHelper;
import benwu.weatherapp.utils.YahooWeatherHelper;
import benwu.weatherapp.utils.Data;

/**
 * Created by Ben Wu on 11/14/2015.
 */

public class WeatherInfoActivity extends AppCompatActivity {

    public static final String TAG = "WeatherInfoActivity";

    public static final String KEY_LOCATION = "LOCATION";
    public static final String KEY_COUNTRY = "COUNTRY";

    public static boolean sLoaded;

    private RetrieveDataTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);

        String location = getIntent().getStringExtra(KEY_LOCATION);
        String country = getIntent().getStringExtra(KEY_COUNTRY);

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setTitle(location);
            if (!country.isEmpty())
                toolbar.setSubtitle(country);
        }


        if(sLoaded) {
            //replaceFrag();
        } else {
            if (location == null)
                displayErrorMessage();
            else {
                mTask = new RetrieveDataTask();
                if (country == null || country.isEmpty()) {
                    mTask.execute(location);
                } else {
                    mTask.execute(country, location);
                }
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LoadingFragment fragment = new LoadingFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null)
            mTask.cancel(true);
    }

    private void displayErrorMessage() {
        Toast.makeText(this, "Invalid location", Toast.LENGTH_SHORT).show();
    }

    private void replaceFrag() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        WeatherInfoFragment fragment = new WeatherInfoFragment();
        transaction.replace(R.id.content_fragment, fragment);
        transaction.commit();
    }

    private class RetrieveDataTask extends AsyncTask<String, Void, WeatherDataObject[]> {
        @Override
        protected WeatherDataObject[] doInBackground(String... params) {
            WeatherDataObject[] weatherData = new WeatherDataObject[5];

            weatherData[0] = OpenWeatherHelper.getDataFor(Data.getOpenweatherkey(), params);
            weatherData[1] = WUndergroundHelper.getDataFor(Data.getWundergroundkey(), params);
            weatherData[2] = YahooWeatherHelper.getDataFor(params);
            weatherData[3] = WorldWeatherHelper.getDataFor(Data.getWorldweatherkey(), params);
            weatherData[4] = AerisWeatherHelper.getDataFor(Data.getAeriskey(), params);

            return weatherData;
        }

        @Override
        protected void onPostExecute(WeatherDataObject[] weatherDataResults) {
            if(weatherDataResults == null)
                displayErrorMessage();
            else {
                DataManager.getInstance().setWeatherData(weatherDataResults);
                sLoaded = true;
                replaceFrag();
            }
        }
    }
}
