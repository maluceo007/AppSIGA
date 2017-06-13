package siga.appsiga;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sam on 22/05/2017.
 */

public class Connecting {

    private String data= null;

    public String GetArduino (String urlString){

        try{
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            if(httpURLConnection.getResponseCode() == 200){

                InputStream inputStream = new BufferedInputStream (httpURLConnection.getInputStream());

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                StringBuilder stringBuilder = new StringBuilder();

                String line;

                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }

                data = stringBuilder.toString();

                httpURLConnection.disconnect();
            }

        }catch (IOException error){
            return null;
        }

        return data;
    }
}
