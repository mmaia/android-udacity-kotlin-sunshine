package com.codespair.sunshine

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.codespair.sunshine.R.id.tv_weather_data
import com.codespair.sunshine.data.SunshinePreferences
import com.codespair.sunshine.utilities.NetworkUtils
import com.codespair.sunshine.utilities.OpenWeatherJsonUtils

class MainActivity : AppCompatActivity() {

  private var mWeatherTextView: TextView? = null

  private var mErrorMessageDisplay: TextView? = null

  private var mLoadingIndicator: ProgressBar? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forecast)

    mWeatherTextView = findViewById(tv_weather_data)

    /* This TextView is used to display errors and will be hidden if there are no errors */
    mErrorMessageDisplay = findViewById<View>(R.id.tv_error_message_display) as TextView

    /*
     * The ProgressBar that will indicate to the user that we are loading data. It will be
     * hidden when no data is loading.
     *
     * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
     * circle. We didn't make the rules (or the names of Views), we just follow them.
     */
    mLoadingIndicator = findViewById<View>(R.id.pb_loading_indicator) as ProgressBar

    /* Once all of our views are setup, we can load the weather data. */
    loadWeatherData()
  }

  /**
   * This method will get the user's preferred location for weather, and then tell some
   * background method to get the weather data in the background.
   */
  private fun loadWeatherData() {
    // COMPLETED (20) Call showWeatherDataView before executing the AsyncTask
    showWeatherDataView()

    val location = SunshinePreferences.getPreferredWeatherLocation(this)
    FetchWeatherTask().execute(location)
  }

  // COMPLETED (8) Create a method called showWeatherDataView that will hide the error message and show the weather data
  /**
   * This method will make the View for the weather data visible and
   * hide the error message.
   *
   *
   * Since it is okay to redundantly set the visibility of a View, we don't
   * need to check whether each view is currently visible or invisible.
   */
  private fun showWeatherDataView() {
    /* First, make sure the error is invisible */
    mErrorMessageDisplay!!.visibility = View.INVISIBLE
    /* Then, make sure the weather data is visible */
    mWeatherTextView!!.visibility = View.VISIBLE
  }

  // COMPLETED (9) Create a method called showErrorMessage that will hide the weather data and show the error message
  /**
   * This method will make the error message visible and hide the weather
   * View.
   *
   *
   * Since it is okay to redundantly set the visibility of a View, we don't
   * need to check whether each view is currently visible or invisible.
   */
  private fun showErrorMessage() {
    /* First, hide the currently visible data */
    mWeatherTextView!!.visibility = View.INVISIBLE
    /* Then, show the error */
    mErrorMessageDisplay!!.visibility = View.VISIBLE
  }

  @SuppressLint("StaticFieldLeak")
  inner class FetchWeatherTask : AsyncTask<String, Void, Array<String>>() {

    // COMPLETED (18) Within your AsyncTask, override the method onPreExecute and show the loading indicator
    override fun onPreExecute() {
      super.onPreExecute()
      mLoadingIndicator?.setVisibility(View.VISIBLE)
    }

    override fun doInBackground(vararg params: String): Array<String>? {

      /* If there's no zip code, there's nothing to look up. */
      if (params.isEmpty()) {
        return null
      }

      val location = params[0]
      val weatherRequestUrl = NetworkUtils.buildUrl(location)

      val jsonWeatherResponse = NetworkUtils
          .getResponseFromHttpUrl(weatherRequestUrl!!)

      return OpenWeatherJsonUtils
          .getSimpleWeatherStringsFromJson(this@MainActivity, jsonWeatherResponse!!)

    }

    override fun onPostExecute(weatherData: Array<String>?) {
      // COMPLETED (19) As soon as the data is finished loading, hide the loading indicator
      mLoadingIndicator!!.visibility = View.INVISIBLE
      if (weatherData != null) {
        // COMPLETED (11) If the weather data was not null, make sure the data view is visible
        showWeatherDataView()
        /*
                 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
        for (weatherString in weatherData) {
          mWeatherTextView!!.append(weatherString + "\n\n\n")
        }
      } else {
        // COMPLETED (10) If the weather data was null, show the error message
        showErrorMessage()
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
    val inflater = menuInflater
    /* Use the inflater's inflate method to inflate our menu layout to this menu */
    inflater.inflate(R.menu.forecast, menu)
    /* Return true so that the menu is displayed in the Toolbar */
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    if (id == R.id.action_refresh) {
      mWeatherTextView!!.text = ""
      loadWeatherData()
      return true
    }

    return super.onOptionsItemSelected(item)
  }
}
