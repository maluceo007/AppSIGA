package siga.appsiga;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Button btnLed1, btnLed2;
    private boolean btnLed1State = false, btnLed2State = false;
    private TextView txtView, textTemp, textDspMin, textDspMax, textMax, textMin;
    private SeekBar seekLed1, seekLed2;
    private int bright1, bright2;
    private boolean isBusy = false;//this flag to indicate whether your async task completed or not
    private boolean stop = false;//this flag to indicate whether your button stop clicked
    private Handler handler = new Handler();

    private String ip = "http://192.168.1.10/";
    private String urlString;
    private int min, max;
    int number;
    private String savedTemp;
    private SharedPreferences sharedPreferences, sp;
    private boolean alarm; // temperature alarm
    Notification.Builder notification;
    private static final int uniqueID = 456123; //id for notification of alarm
    //public static Context contextOfApplication; // variable to be access in non-activity class
    //private SwitchPreference prefMinMax;

    //set index to http call setter and getter
    public void setUrlString( String index){ urlString = ip + index;}
    public String getUrlString(){ return this.urlString;}
    // temperature alarm setter and getter
    public void setAlarm (Boolean v){ this.alarm = v;}
    public Boolean getAlarm (){ return this.alarm;}
    // temper min and max setter and getter
    public void setMin (int m){this.min = m;}
    public int getMin (){return this.min;}
    public void setMax (int m){this.max = m;}
    public int getMax(){return this.max;}
    //set button light state
    public void setBntState(String btn){
        if (btn == ("led1:")) {
            if (!btnLed1State) {
                btnLed1.setBackgroundResource(R.drawable.white_light);
                btnLed1State = true;

            } else {
                btnLed1.setBackgroundResource(R.drawable.white_light_off);
                btnLed1State = false;

            }
        }
        if (btn == ("led2:")){

            if (!btnLed2State) {
                btnLed2.setBackgroundResource(R.drawable.blue_light);
                btnLed2State = true;

            } else {
                btnLed2.setBackgroundResource(R.drawable.blue_light_off);
                btnLed2State = false;

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //customize toolbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        btnLed1= (Button)findViewById(R.id.btnLed1);
        btnLed2= (Button)findViewById(R.id.btnLed2);

        txtView = (TextView)findViewById(R.id.textView);
        textTemp = (TextView) findViewById(R.id.textTemp);
        textDspMin = (TextView) findViewById(R.id.textDspMin);
        textDspMax = (TextView) findViewById(R.id.textDspMax);
        textMax = (TextView) findViewById(R.id.textMax);
        textMin = (TextView) findViewById(R.id.textMin);

        seekLed1 = (SeekBar)findViewById(R.id.seekLed1);
        getSeekValue (seekLed1 );
        seekLed2 = (SeekBar)findViewById(R.id.seekLed2);
        getSeekValue (seekLed2 );
        //addPreferencesFromResource(R.xml.numberPicker);

        //sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //prefMinMax = (SwitchPreference) findPrefernece("minMax");

        //textDspMin.setText(getReference("minNewValue"));
        //getMessage(getReference("minNewValue"));
        //textDspMax.setText(getReference("maxNewValue"));

        //intial call to arduino to start readting temp
        //beacause of delay in Handler first read must be outside handler
        setUrlString("continua:");
        callAsyncTask(getUrlString());
        // call continuous handler to arduino for temperature reading with delay
        startHandler();
        //initialize variable to be accessed outsite MainActiviy
        //contextOfApplication = getApplicationContext();

        loadAlarm(); // load alarm boolean option on/off
        loadAlarmSetgs(); // load alarm setting
        viewAlarmMinMax(); // show or hide alarm views

        //notification information
        notification = new Notification.Builder(this);
        notification.setAutoCancel(true);

        /* comment before new changes */
        btnLed1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                isBusy = true;
                String urlIndex = "led1:";
                if (btnLed1State){
                    bright1 = 0;
                }
                if (!btnLed1State){
                    bright1 = 255;
                }

                setUrlString("led1:" + bright1);
                callAsyncTask(getUrlString());
                setBntState("led1:");

                setBrightness(seekLed1, bright1); // set seek bar to on or off
                isBusy = false;



                setReference("currentTemp",  savedTemp);
            }

        });

        btnLed2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                isBusy = true;
                String urlIndex = "led2:";
                if (btnLed2State){
                    bright2 = 0;
                }
                if (!btnLed2State){
                    bright2 = 255;
                }

                setUrlString("led2:" + bright2);
                callAsyncTask(getUrlString());
                setBntState("led2:");
                setBrightness(seekLed2, bright2);
                isBusy = false;
                getMessage(getReference("currentTemp"));

            }



        });

    }// end onCreate class



    // function used to constatly query JSON data
    public void startHandler(){

        handler.postDelayed( new Runnable(){

            @Override
            public void run(){
                setUrlString("continua:");
                if (!isBusy) callAsyncTask(getUrlString());

                if (!stop) startHandler();
            }
        },60000);

    }

    // call AsyncTask for connecting
    private void callAsyncTask(String url){ new GetAsync().execute(url); }

    // create option menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    // take action when menu button is selected
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.preference_setting:

                //open new activity with preference settings
                startActivity(new Intent(this, PreferencesActivity.class));

                return (true);

            default:
                return super.onOptionsItemSelected(item);
        }

        //SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadAlarm(); // load alarm boolean option on/off
        loadAlarmSetgs(); // load alarm setting
        viewAlarmMinMax(); // show or hide alarm views
    }


    class GetAsync extends  AsyncTask <String, Void, String>{

    protected String doInBackground(String... strings){

        String stream = null;
        String urlString = strings[0];

        JSONParser jsonParser = new JSONParser();
        stream = jsonParser.GetHTTPData(urlString);
        //getMessage("AsyncTask");
        // Return the data from specified url
        return stream;

        //return connecting.GetArduino(urlString);

        //HTTPDataHandler hh = new HTTPDataHandler();
        //stream = hh.GetHTTPData(urlString);


    }

    protected void onPostExecute(String stream){


            /*
                Important in JSON DATA
                -------------------------
                * Square bracket ([) represents a JSON array
                * Curly bracket ({) represents a JSON object
                * JSON object contains key/value pairs
                * Each key is a String and value may be different data types
             */

        //..........Process JSON DATA................
        if(stream !=null){
            try{
                //getMessage("inside full stream");
                // Get the full HTTP Data as JSONObject
                JSONObject reader= new JSONObject(stream);

                // Get the JSONObject "coord"...........................
                //JSONObject coord = reader.getJSONObject("coord");
                // Get the value of key "lon" under JSONObject "coord"
                String temp = reader.getString("temp");
                int temp2 = reader.getInt("temp");
                // Get the value of key "lat" under JSONObject "coord"
                String key = reader.getString("res");

                txtView.setText("We are processing the JSON data....\n\n");
                //txtView.setText(txtView.getText()+ "\tcoord...\n");
                txtView.setText(txtView.getText()+ "\t\ttemp..."+ temp + "\n");
                txtView.setText(txtView.getText()+ "\t\tkey..."+ key + "\n\n");

                textTemp.setText(temp);
                savedTemp = temp;

               // int temp2 = Integer.valueOf(savedTemp);

                if ( temp2 < getMin() || temp2 > getMax()  ){
                    displayNotification();
                }
/*
                    // Get the JSONObject "sys".........................
                    JSONObject sys = reader.getJSONObject("sys");
                    // Get the value of key "message" under JSONObject "sys"
                    String message = sys.getString("message");
                    // Get the value of key "country" under JSONObject "sys"
                    String country = sys.getString("country");
                    // Get the value of key "sunrise" under JSONObject "sys"
                    String sunrise = sys.getString("sunrise");
                    // Get the value of key "sunset" under JSONObject "sys"
                    String sunset = sys.getString("sunset");

                    txtView.setText(txtView.getText()+ "\tsys...\n");
                    txtView.setText(txtView.getText()+ "\t\tmessage..."+ message + "\n");
                    txtView.setText(txtView.getText()+ "\t\tcountry..."+ country + "\n");
                    txtView.setText(txtView.getText()+ "\t\tsunrise..."+ sunrise + "\n");
                    txtView.setText(txtView.getText()+ "\t\tsunset..."+ sunset + "\n\n");

                    // Get the JSONArray weather
                    JSONArray weatherArray = reader.getJSONArray("weather");
                    // Get the weather array first JSONObject
                    JSONObject weather_object_0 = weatherArray.getJSONObject(0);
                    String weather_0_id = weather_object_0.getString("id");
                    String weather_0_main = weather_object_0.getString("main");
                    String weather_0_description = weather_object_0.getString("description");
                    String weather_0_icon = weather_object_0.getString("icon");

                    txtView.setText(txtView.getText()+ "\tweather array...\n");
                    txtView.setText(txtView.getText()+ "\t\tindex 0...\n");
                    txtView.setText(txtView.getText()+ "\t\t\tid..."+ weather_0_id + "\n");
                    txtView.setText(txtView.getText()+ "\t\t\tmain..."+ weather_0_main + "\n");
                    txtView.setText(txtView.getText()+ "\t\t\tdescription..."+ weather_0_description + "\n");
                    txtView.setText(txtView.getText()+ "\t\t\ticon..."+ weather_0_icon + "\n\n");
*/
                // process other data as this way..............

            }catch(JSONException e){
                e.printStackTrace();
            }

        } // end if statement

    } // end onPostExecute()
}// end GetAsync class


    public void getSeekValue (SeekBar bar ) {
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                isBusy = true;
                if ( seekBar == seekLed1){
                    if (!btnLed1State || seekBar.getProgress() == 0)  {
                        setBntState("led1:");
                        setUrlString("led1:" + seekBar.getProgress());

                    }else{
                        //RequestUrl ("led1:", seekBar.getProgress());
                        setUrlString("led1:" + seekBar.getProgress());


                    }

                    bright1=seekBar.getProgress();

                }
                if ( seekBar == seekLed2){
                    if (!btnLed2State || seekBar.getProgress()== 0)  {
                        setBntState("led2:");
                        setUrlString("led2:" + seekBar.getProgress());
                    }else{
                        //RequestUrl ("led1:", seekBar.getProgress());
                        setUrlString("led2:" + seekBar.getProgress());
                        //callAsyncTask(getUrlString());

                    }

                    bright2=seekBar.getProgress();

                }
                callAsyncTask(getUrlString());
                //bright1 = progr   ess;
                txtView.setText(String.valueOf(seekBar.getProgress()));
                isBusy = false;
            }
        });
    }

    public void setBrightness (SeekBar bar, int dimm){

        bar.setProgress(dimm);
    }

/*
    private class DownloadWebPageTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            Connecting connecting = new Connecting();

            return connecting.getArduino(urls[0]);
        }
        //onPostExecute displays the results of te AsyncTask
        @Override
        protected void onPostExecute(String result){
            if (result != null){
                txtView.setText(result);

                /*
                // set the text on the led 1 button
                if (result.contains("Led 1 - Ligado")){
                    //btnLed1.setText("Led 1 - ON");
                    btnLed1.setBackgroundResource(R.drawable.white_light);
                }else if (result.contains("Led 1 - Desligado")){
                    //btnLed1.setText("Led 1 - OFF");
                    btnLed1.setBackgroundResource(R.drawable.white_light_off);
                }

                // set the text on the led 2 button
                if (result.contains("Led 2 - Ligado")){
                    //btnLed2.setText("Led 2 - ON");
                    btnLed2.setBackgroundResource(R.drawable.blue_light);
                }else if (result.contains("Led 2 - Desligado")){
                    //btnLed2.setText("Led 2 - OFF");
                    btnLed2.setBackgroundResource(R.drawable.blue_light_off);
                }

                // set the temp value in text field
                String temp = result.substring(result.indexOf("Temperature = "), 5);
                textTemp.setText(temp);



            }else{
                txtView.setText("Error acessing the arduino...");
            }
        }
    } //end DownloadWebpageTask class
*/

    // save value to PreferenceManager
    public void setReference(String key, String value){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getReference(String key) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString(key, "string not found");
    }
    public void getMessage ( String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    //function used to update the min and max alarms in the textFields
    public void loadAlarmSetgs(){
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int min = sp.getInt("temp_min", 0);
        setMin(min);
        int max = sp.getInt("temp_max", 0);
        setMax(max);
        textDspMin.setText(Integer.toString(min)+ "º");
        textDspMax.setText(Integer.toString(max)+ "º");
        sp.registerOnSharedPreferenceChangeListener(MainActivity.this);

    }

    public void loadAlarm(){
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean alarm = sp.getBoolean("temp_alarm", false);
        setAlarm(alarm); // set alarm value
    }

    public void viewAlarmMinMax(){

        if (getAlarm()){
            textMin.setVisibility(txtView.VISIBLE);
            textMax.setVisibility(txtView.VISIBLE);
            textDspMin.setVisibility(txtView.VISIBLE);
            textDspMax.setVisibility(txtView.VISIBLE);
        }
        if (!getAlarm()){
            textMin.setVisibility(txtView.GONE);
            textMax.setVisibility(txtView.GONE);
            textDspMin.setVisibility(txtView.GONE);
            textDspMax.setVisibility(txtView.GONE);
        }

    }

    public void displayNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.setSmallIcon(R.drawable.siga_background);
            notification.setTicker("Temperature Alarm");
            notification.setWhen(System.currentTimeMillis());
            notification.setContentTitle("Aquarium Notification");
            notification.setContentText("The temperature as past thresholds!");
            //send to home screen after notification is selected
            Intent intent = new Intent (this,  MainActivity.class);
            PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pendingIntent);
            //build notification and issue it
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(uniqueID, notification.build());
        }else{
            //TODO notification prior to android jellybeans.
        }


    }


    /*
    @Override
    public void upDateAlarm(TextView view, int msg)
    {
        if (view == textDspMin) {
            TextView txtView = (TextView) findViewById(R.id.textDspMin);
            txtView.setText(msg);
        }
        if (view == textDspMax) {
            TextView txtView = (TextView) findViewById(R.id.textDspMax);
            txtView.setText(msg);
        }
    }
*/
    /*
    // method to return context value outside MainActivity
    public static Context getContextOfApplication(){
        return contextOfApplication;
    }*/
}
