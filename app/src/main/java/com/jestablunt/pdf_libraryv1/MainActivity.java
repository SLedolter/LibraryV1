package com.jestablunt.pdf_libraryv1;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
  private Button updateButton;
  private ProgressDialog prgDialog;
  final static int progress_bar_type = 0;
  private JSONObject allData;

  private static String filename = "hrnshn.mp3";
  private static String file_url = "http://www.jestablunt.at/"+filename;
  /***
   * onCreate
   * @param savedInstanceState Standard param
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    /*
      WebView init
     */
    WebView myWebView = (WebView) findViewById(R.id.webview);
    WebSettings myWebSettings = myWebView.getSettings();
    myWebSettings.setJavaScriptEnabled(true);

    //myWebView.loadUrl("file:///android_asset/html/index.html");
    myWebView.loadUrl("file:///android_asset/html/index.html");

    showStatusBar(false);

    /*
      Update Button
     */
    updateButton = (Button) findViewById(R.id.updateButton);
    updateButton.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.d("downloadButton", "onClickStart");
        String file_location = getBaseContext().getFilesDir()+"/"+filename;
        File file = new File(file_location);
        Log.d("Check for File", ""+file.exists()+" "+file_location);
        if(!file.exists()) {
          new DownloadFileFromURL().execute(file_url);
        } else {
          Toast.makeText(getBaseContext(), "File exists", Toast.LENGTH_SHORT);
        }

      }
    });
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    showStatusBar(false);
  }

  /*************************************/
  /*** Function to toggle Satus Bar   **/
  /*************************************/
  protected void showStatusBar(boolean show) {
      View decorView = getWindow().getDecorView();
      // Hide the status bar.
      int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
      decorView.setSystemUiVisibility(uiOptions);
      // Remember that you should never show the action bar if the
      // status bar is hidden, so hide that too if necessary.
      ActionBar actionBar = getSupportActionBar();
      if(actionBar != null) {
        if (show) {
          actionBar.show();
        } else {
          actionBar.hide();
        }
      }
  }

  // Show dialog box with progress bar
  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case progress_bar_type:
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Downloading mp3 file. Please wait...");
        prgDialog.setIndeterminate(false);
        prgDialog.setMax(100);
        prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        prgDialog.setCancelable(false);
        prgDialog.show();
        return prgDialog;
      default:
        return null;
    }
  }

  /***
   * Class extending AsyncTask to download file
   */
  private class DownloadFileFromURL extends AsyncTask<String, String, String> {
    // show progress bar before downloading music
    @Override
    protected void onPreExecute () {
      super.onPreExecute();
      // shows progress bar dialog and then call doInBackground method
      showDialog(progress_bar_type);
    }
    // download music file from internet
    protected String doInBackground(String... f_url) {
      Log.d("doInBackground", "Start");
      int count;
      try {
        // ToDo: yet to code
        URL url = new URL(f_url[0]);
        URLConnection connection = url.openConnection();
        connection.connect();
        // Get music file length
        int lengthOfFile = connection.getContentLength();
        // inputStream to read file - with 8k buffer
        InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);
        // Output stream to write file in SD card
        Log.d("doInBackground", "before outputstream");
        OutputStream output = new FileOutputStream(getFilesDir().getPath() + "/data.json");
        Log.d("doInBackground", output.toString());
        byte data[] = new byte[1024];
        long total = 0;
        while ((count = input.read(data)) != -1) {
          total += count;
          // Publish the progress which triggers onProgressUpdate method
          publishProgress("" + (int)((total*100)/lengthOfFile));
          // Write data to file
          output.write(data, 0, count);
        }
        // Flush output
        output.flush();
        // Close streams
        output.close();
        input.close();
      } catch(Exception e) {
        Log.e("Error: ", e.getMessage());
      }
      Log.d("doInBackground", "End");

      return null;
    }
    protected void onProgressUpdate(String... progress) {
      // Set progress percentage
      prgDialog.setProgress(Integer.parseInt(progress[0]));
    }
    // Once Music file is downloaded
    @Override
    protected void onPostExecute(String file_url) {
      // dismiss the dialog after the music file was downloaded
      dismissDialog(progress_bar_type);
      Toast.makeText(getApplicationContext(), "Download complete, playing music", Toast.LENGTH_LONG).show();
      readFromFile();
    }

  }

  protected String readFromFile() {
    String ret = "";
    try {
      InputStream input = new FileInputStream(new File(getFilesDir()+"/"+filename));
      if(input != null) {
        InputStreamReader inputStreamReader = new InputStreamReader(input);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String recieveString = "";
        StringBuilder stringBuilder = new StringBuilder();

        while((recieveString = bufferedReader.readLine()) != null) {
          stringBuilder.append(recieveString);
        }
        input.close();
        ret = stringBuilder.toString();
      }
    } catch(Exception e) {
      Log.e("Exception", e.getMessage());
    }
    return ret;
  }

  // Storage Permissions
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static String[] PERMISSIONS_STORAGE = {
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  /**
   * Checks if the app has permission to write to device storage
   *
   * If the app does not has permission then the user will be prompted to grant permissions
   *
   * @param activity
   */
  public static void verifyStoragePermissions(Activity activity) {
    // Check if we have write permission
    int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(
        activity,
        PERMISSIONS_STORAGE,
        REQUEST_EXTERNAL_STORAGE
      );
    }
  }
}
