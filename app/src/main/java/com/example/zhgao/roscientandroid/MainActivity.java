package com.example.zhgao.roscientandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jilk.ros.message.Clock;
import com.jilk.ros.message.GeoTwist;
import com.jilk.ros.message.Message;
import com.jilk.ros.message.MessageType;
import com.jilk.ros.message.RosString;
//import com.jilk.ros.message.RosString;
import com.jilk.ros.rosapi.message.TypeDef;
import com.jilk.ros.rosbridge.operation.Operation;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jilk.ros.rosbridge.FullMessageHandler;
import org.java_websocket.client.*;



public class MainActivity extends AppCompatActivity {
    ROSBridgeClient client = null;
    com.jilk.ros.Topic<RosString> ctrlTopic = null;
    com.jilk.ros.Topic<GeoTwist> VelCmdTopic = null;
    RosState runstate = RosState.IDLE;
    boolean connect_state = false;
    TextView info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = findViewById(R.id.t1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btn_up = (Button)findViewById(R.id.btn_up);
        Button btn_down = (Button)findViewById(R.id.btn_down);
        Button btn_left = (Button)findViewById(R.id.btn_left);
        Button btn_right = (Button)findViewById(R.id.btn_right);
        Button btn_counterwise = (Button)findViewById(R.id.btn_counterwise);
        Button btn_clockwise = (Button)findViewById(R.id.btn_clockwise);
        View.OnTouchListener buttonListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int id  = v.getId();
                // TODO Auto-generated method stub
                if(runstate == RosState.SLAM || runstate == RosState.HOLD){
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        switch(id){
                            case R.id.btn_up:{
                                VelCmdTopic.publish(new GeoTwist(0.2,0,0,0,0,0));
                                break;
                            }
                            case R.id.btn_down:{
                                VelCmdTopic.publish(new GeoTwist(-0.2,0,0,0,0,0));
                                break;
                            }
                            case R.id.btn_left:{
                                VelCmdTopic.publish(new GeoTwist(0,0.2,0,0,0,0));
                                break;
                            }
                            case R.id.btn_right:{
                                VelCmdTopic.publish(new GeoTwist(0,-0.2,0,0,0,0));
                                break;
                            }
                            case R.id.btn_clockwise:{
                                VelCmdTopic.publish(new GeoTwist(0,0,0,0,0,1.0));
                                break;
                            }
                            case R.id.btn_counterwise:{
                                VelCmdTopic.publish(new GeoTwist(0,0,0,0,0,-1.0));
                                break;
                            }
                        }
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        VelCmdTopic.publish(new GeoTwist(0,0,0,0,0,0));
                    }
                    return false;
                }
                else {
                    return true;
                }
            }
        };
        btn_up.setOnTouchListener(buttonListener);
        btn_down.setOnTouchListener(buttonListener);
        btn_left.setOnTouchListener(buttonListener);
        btn_right.setOnTouchListener(buttonListener);
        btn_counterwise.setOnTouchListener(buttonListener);
        btn_clockwise.setOnTouchListener(buttonListener);
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
    RosState getButtonActType(int id){
        switch(id){
            case R.id.button_slam: return RosState.SLAM;

        }
        return null;
    }
    int getButtonid(RosState s){
        if(s== RosState.SLAM){
            return R.id.button_slam;
        }
        return 0;
    }

    public void onClick(View v){
        if(connect_state==false){
            info.setText("Not Connected");
            return;
        }
        VelCmdTopic.publish(new GeoTwist(0,0,0,0,0,0));
        int id = v.getId();
        RosString ctrlstring = new RosString("NULL");
        Button curbtn = (Button) findViewById(id);
        Button statebtn = (Button) findViewById(getButtonid(runstate));
        try {
            if(runstate != RosState.IDLE){
                if(runstate == RosState.SLAM){
                    ctrlstring = new RosString("stop slam");
                    ctrlTopic.publish(ctrlstring);
                }
                statebtn.setBackgroundColor(Color.parseColor("#FFEC8B"));
            }
            if(runstate == getButtonActType(id)){
                runstate = RosState.IDLE;
                return;
            }
            if (ctrlTopic != null) {
                if(id==R.id.button_slam) {
                    ctrlstring = new RosString("start slam");
                    ctrlTopic.publish(ctrlstring);
                    runstate = RosState.SLAM;

                }
                curbtn.setBackgroundColor(Color.parseColor("#0000CD"));
                info.setText("successfully sent:"+ctrlstring.data);
            }
        }
        catch(Exception e){
                info.setText("send fail");

        }
    }
    public void onClickConnect(View v){
        EditText remoteaddr = (EditText)findViewById(R.id.addresstext);
        client = new ROSBridgeClient("ws://" + remoteaddr.toString());
        boolean connect_state = client.connect();
        if(connect_state == false){
            info.setText("Connect Fail");
        }
        else{
            info.setText("Connected");
            ctrlTopic = new com.jilk.ros.Topic<RosString>("/ctrlmsg", RosString.class, client);
            ctrlTopic.advertise();
            VelCmdTopic = new com.jilk.ros.Topic<GeoTwist>("/cmd_vel",GeoTwist.class,client);
            VelCmdTopic.advertise();
        }
    }
    public void onClickMove(View v,MotionEvent event){

    }


}
