package com.example.zhgao.roscientandroid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jilk.ros.message.Clock;
import com.jilk.ros.message.Message;
import com.jilk.ros.message.MessageType;
import com.jilk.ros.message.RosString;
//import com.jilk.ros.message.RosString;
import com.jilk.ros.rosapi.message.TypeDef;
import com.jilk.ros.rosbridge.operation.Operation;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jilk.ros.rosbridge.FullMessageHandler;

public class MainActivity extends AppCompatActivity {
    ROSBridgeClient client;
    com.jilk.ros.Topic<RosString> ctrlTopic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new ROSBridgeClient("ws://192.168.1.101:11311");
        boolean connect_state = client.connect();
        if(connect_state == false){
            TextView t1 = (TextView)findViewById(R.id.t1);
            if(t1!=null)
                t1.setText("connect fail");

        }
        else {
            ctrlTopic = new com.jilk.ros.Topic<RosString>("/ctrlmsg", RosString.class, client);
            ctrlTopic.advertise();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
    public void onClick(View v){
        if(ctrlTopic!=null) {
            RosString ctrlstring = new RosString("start slam");
            ctrlTopic.publish(ctrlstring);
        }
    }

}
