package ru.specx.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.text.TextUtils.split;


public class MainActivity extends ActionBarActivity {

    private Button mbutton;
    private TextView mtextview;
    private Toast mnety, mnetn;
    private BufferedReader reader;
    private Timer mtimer;
    private TimerTask mtimertask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbutton = (Button)findViewById(R.id.button);
        mtextview = (TextView)findViewById(R.id.textView);
        mnety = Toast.makeText(getApplicationContext(),"Сеть доступна", Toast.LENGTH_SHORT);
        mnety.setGravity(Gravity.CENTER,0,0);
        mnetn = Toast.makeText(getApplicationContext(),"Сеть не доступна", Toast.LENGTH_SHORT);
        mnetn.setGravity(Gravity.CENTER, 0, 0);
        mtimer = new Timer();
        mtimertask = new TimerTask() {
            @Override
            public void run() {
                new getwebpage().execute("http://devices.specx.ru/index.php");
            }
        };
        mtimer.schedule(mtimertask,1000,5000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class getwebpage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            String[] data = split(result, "\n");
            for (int i = 0; i < data.length - 1; i++) {
                if (!data[i].isEmpty()) {
                    //mtextview.setText(mtextview.getText()+"=>"+data[i]+"<=");
                    String[] kd = split(data[i], ": ");
                    if (kd[0].equalsIgnoreCase("temperature")) {
                        mtextview.setText(kd[1]);
                    }
                }
            }
        }

    }

         private String printString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            sb.append(Integer.toHexString((int)ch)).append(" ");
        }
        return sb.toString();
    }


    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String header = "Basic " + new String(android.util.Base64.encode("7up_z1:getdata".getBytes(), android.util.Base64.NO_WRAP));
            conn.addRequestProperty("Authorization", header);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            //Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;
            // Makes sure that the InputStream is closed after the app is
            // finished using it.

//            return "ok";
        }      finally
        {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


    public void TestNet(View view) {
        ConnectivityManager connmgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = connmgr.getActiveNetworkInfo();

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        mtextview.setText("my ip:"+ip);

        if (netinfo != null && netinfo.isConnected())
        {
            mnety.show();
        }
        else
        {
            mnetn.show();
        }
    }

    public void MakeReq(View view) {
        //getwebpage aa = new getwebpage();
        //mtextview.setText(aa.doInBackground("http://192.168.0.109/"));
        new getwebpage().execute("http://devices.specx.ru/index.php");
    }

}
