package siga.appsiga;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created by Sam on 26/05/2017.
 */

public class log_in extends  Activity{

    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        btnConnect = (Button)findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent intent = new Intent(log_in.this, MainActivity.class);
                startActivity(intent);
            }

        });
    }
}
