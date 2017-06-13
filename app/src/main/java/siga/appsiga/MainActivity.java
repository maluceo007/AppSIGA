package siga.appsiga;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnLed1, btnLed2, btnLed3;
    TextView txtView;
    SeekBar seekLed1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLed1= (Button)findViewById(R.id.btnLed1);
        btnLed2= (Button)findViewById(R.id.btnLed2);
        btnLed3= (Button)findViewById(R.id.btnLed3);
        txtView = (TextView)findViewById(R.id.textView);

        //seekLed1 = (SeekBar)findViewById(R.id.seekLed1);
        //seekLed1.setOnSeekBarChangeListener(OnAnalogOutChangeListener);


        RequestUrl("");
/*
        seekLed1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Notification that the progress level has changed.
                if (progress < fillDefault){
                    seekBar.setProgress(fillDefault); // magic solution, ha
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Notification that the user has started a touch gesture.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Notification that the user has finished a touch gesture.

            }
        });
*/
        btnLed1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                //Toast.makeText(MainActivity.this, "Button 1 Works", Toast.LENGTH_SHORT).show();
                RequestUrl("led1");
            }

        });

        btnLed2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                //Toast.makeText(MainActivity.this, "Button 2 Works", Toast.LENGTH_SHORT).show();
                RequestUrl("led2");
            }

        });

        btnLed3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                //Toast.makeText(MainActivity.this, "Button 3 Works", Toast.LENGTH_SHORT).show();
                RequestUrl("led3");
            }

        });

    }

    public void RequestUrl (String get){

        String url = "http://192.168.1.10/" + get;

        // get connection information
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            new DownloadWebPageTask().execute(url);
        }else{
            txtView.setText("No network connection available.");
        }
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            Connecting connecting = new Connecting();

            return connecting.GetArduino(urls[0]);
        }
        //onPostExecute displays the results of te AsyncTask
        @Override
        protected void onPostExecute(String result){
            if (result != null){
                txtView.setText(result);

                // set the text on the led 1 button
                if (result.contains("Led 1 - Ligado")){
                    //btnLed1.setText("Led 1 - ON");
                    btnLed1.setBackgroundResource(R.drawable.toggle_on);
                }else if (result.contains("Led 1 - Desligado")){
                    //btnLed1.setText("Led 1 - OFF");
                    btnLed1.setBackgroundResource(R.drawable.toggle_off);
                }

                // set the text on the led 1 button
                if (result.contains("Led 2 - Ligado")){
                    //btnLed2.setText("Led 2 - ON");
                }else if (result.contains("Led 2 - Desligado")){
                    //btnLed2.setText("Led 2 - OFF");
                }

                // set the text on the led 1 button
                if (result.contains("Led 3 - Ligado")){
                    //btnLed3.setText("Led 3 - ON");
                }else if (result.contains("Led 3 - Desligado")){
                    //btnLed3.setText("Led 3 - OFF");
                }

            }else{
                txtView.setText("Error acessing the arduino...");
            }
        }
    }
}
