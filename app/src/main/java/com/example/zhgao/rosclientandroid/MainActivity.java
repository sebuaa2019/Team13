package com.example.zhgao.rosclientandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jilk.ros.message.GeoTwist;
import com.jilk.ros.message.RosString;
//import com.jilk.ros.message.RosString;
import com.jilk.ros.rosbridge.ROSBridgeClient;


public class MainActivity extends AppCompatActivity {
    ROSBridgeClient client = null;
    com.jilk.ros.Topic<RosString> ctrlTopic = null;
    com.jilk.ros.Topic<GeoTwist> VelCmdTopic = null;
    RosState runstate = RosState.IDLE;
    String RemoteIP = null;
    boolean connect_state = false;
    TextView info;
    boolean robot_stop = true;
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
                Button curbtn = (Button)findViewById(id);
                int action = event.getAction();
                if (action == MotionEvent.ACTION_BUTTON_PRESS ||
                        action == MotionEvent.ACTION_DOWN||
                        action == MotionEvent.ACTION_HOVER_ENTER||
                        action == MotionEvent.ACTION_MASK||
                        action == MotionEvent.ACTION_POINTER_DOWN||
                        action == MotionEvent.ACTION_MOVE) {
                    info.setText("action_down");
                    curbtn.setBackgroundColor(Color.parseColor("#0000CD"));
                }
                else{
                    info.setText("action_up");
                    curbtn.setBackgroundColor(Color.parseColor("#DDDDDD"));
                }
                // TODO Auto-generated method stub
                if(VelCmdTopic!=null) {
                    if (runstate == RosState.SLAM || runstate == RosState.HOLD) {

                        if (action == MotionEvent.ACTION_BUTTON_PRESS ||
                                action == MotionEvent.ACTION_DOWN||
                                action == MotionEvent.ACTION_HOVER_ENTER||
                                action == MotionEvent.ACTION_MASK||
                                action == MotionEvent.ACTION_POINTER_DOWN||
                                action == MotionEvent.ACTION_MOVE) {

                            switch (id) {
                                case R.id.btn_up: {
                                    String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0.2 + ",\"y\":" +
                                            0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                                    client.send(MoveMsg);
                                    //VelCmdTopic.publish(new GeoTwist(0.2, 0, 0, 0, 0, 0));
                                    break;
                                }
                                case R.id.btn_down: {
                                    String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + -0.2 + ",\"y\":" +
                                            0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                                    client.send(MoveMsg);
                                    //VelCmdTopic.publish(new GeoTwist(-0.2, 0, 0, 0, 0, 0));
                                    break;
                                }
                                case R.id.btn_left: {
                                    String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                            0.2 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                                    client.send(MoveMsg);
                                    //VelCmdTopic.publish(new GeoTwist(0, 0.2, 0, 0, 0, 0));
                                    break;
                                }
                                case R.id.btn_right: {
                                    String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                            -0.2 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                                    client.send(MoveMsg);
                                    //VelCmdTopic.publish(new GeoTwist(0, -0.2, 0, 0, 0, 0));
                                    break;
                                }
                                case R.id.btn_clockwise: {
                                    String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                            0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.8 + "}}}";
                                    client.send(MoveMsg);
                                    //VelCmdTopic.publish(new GeoTwist(0, 0, 0, 0, 0, 1.0));
                                    break;
                                }
                                case R.id.btn_counterwise: {
                                    String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                            0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + -0.8 + "}}}";
                                    client.send(MoveMsg);
                                    //VelCmdTopic.publish(new GeoTwist(0, 0, 0, 0, 0, -1.0));
                                    break;
                                }
                            }
                            robot_stop = false;
                            info.setText("sent move message");
                        } else {

                            String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                    0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                            for(int i=0;i<10;i++) {
                                try {
                                    Thread.sleep(5);
                                }
                                catch(Exception e){

                                }
                                client.send(MoveMsg);
                            }
                            robot_stop = true;
                            info.setText("sent stop message");
                        }
                        return false;
                    } else {

                        return true;
                    }
                }
                return true;
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
        String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
        robot_stop = true;
        client.send(MoveMsg);
        info.setText("sent stop msg ");
        int id = v.getId();
        RosString ctrlstring = new RosString("NULL");
        Button curbtn = (Button) findViewById(id);
        Button statebtn = (Button) findViewById(getButtonid(runstate));
        try {
            if(runstate != RosState.IDLE){
                if(runstate == RosState.SLAM){
                    ctrlstring = new RosString("stop slam");
                }
                if(runstate == RosState.HOLD){
                    ctrlstring = new RosString("stop hold");
                }
                if(runstate == RosState.NAV){
                    ctrlstring = new RosString("stop nav");
                }
                if(runstate == RosState.GRAB){
                    ctrlstring = new RosString("stop grab");
                }
                ctrlTopic.publish(ctrlstring);
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
                if(id==R.id.btn_nav){
                    ctrlstring = new RosString("start nav");
                    ctrlTopic.publish(ctrlstring);
                    MyImageView imageview = (MyImageView)findViewById(R.id.image_view);
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageURL("ftp://robot:6@"+RemoteIP+"/home/robot/map.png");

                    runstate = RosState.NAV;
                }
                if(id==R.id.btn_hold){
                    ctrlstring = new RosString("start hold");
                    ctrlTopic.publish(ctrlstring);
                    runstate = RosState.HOLD;
                }
                if(id==R.id.btn_grab){
                    ctrlstring = new RosString("start grab");
                    ctrlTopic.publish(ctrlstring);
                    runstate = RosState.GRAB;
                }
                curbtn.setBackgroundColor(Color.parseColor("#0000CD"));
                info.setText("successfully sent:"+ctrlstring.data);
            }
        }
        catch(Exception e){
                info.setText("send fail");

        }
    }
    SendZeroThread sendzero = null;
    public void onClickConnect(View v){
        EditText remoteaddr = (EditText)findViewById(R.id.addresstext);
        RemoteIP = remoteaddr.getText().toString();

        String fulladdr = "ws://" + RemoteIP + ":9090";
        client = new ROSBridgeClient(fulladdr);
        connect_state = client.connect();
        if(connect_state == false){
            info.setText("Connect Fail to "+fulladdr);
        }
        else{
            info.setText("Connected");
            ctrlTopic = new com.jilk.ros.Topic<RosString>("/ctrlmsg", RosString.class, client);
            ctrlTopic.advertise();
            VelCmdTopic = new com.jilk.ros.Topic<GeoTwist>("/cmd_vel",GeoTwist.class,client);
            VelCmdTopic.advertise();
            if(sendzero == null) {
                sendzero = new SendZeroThread();
                sendzero.start();
            }
        }
    }
    public void onClickMove(View v,MotionEvent event){

    }

    class SendZeroThread extends Thread{
        public void run(){
            while(true){
                try{
                    if(client== null){
                        continue;
                    }
                    if(VelCmdTopic == null){
                        continue;
                    }
                    if(robot_stop){
                        String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                        client.send(MoveMsg);
                    }
                    Thread.sleep(10);
                }
                catch(Exception e){

                }
            }
        }

    }
}
