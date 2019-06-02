package com.example.zhgao.rosclientandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jilk.ros.message.GeoTwist;
import com.jilk.ros.message.RosString;
//import com.jilk.ros.message.RosString;
import com.jilk.ros.rosbridge.ROSBridgeClient;


public class MainActivity extends AppCompatActivity {
    ROSBridgeClient client = null;
    com.jilk.ros.Topic<RosString> ctrlTopic = null;
    com.jilk.ros.Topic<RosString> grabTopic = null;
    com.jilk.ros.Topic<GeoTwist> VelCmdTopic = null;
    RosState runstate = RosState.IDLE;
    String RemoteIP = null;
    boolean connect_state = false;
    TextView info;
    boolean robot_stop = true;
    float originalImageOffsetX;
    float originalImageOffsetY;
    boolean returnImageOffset = false;
    Object imageRetLock;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = findViewById(R.id.t1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("about:blank");
        MyImageView imageView = (MyImageView) findViewById(R.id.image_view);
        imageView.mainActivity = this;
        Button btn_up = (Button) findViewById(R.id.btn_up);
        Button btn_down = (Button) findViewById(R.id.btn_down);
        Button btn_left = (Button) findViewById(R.id.btn_left);
        Button btn_right = (Button) findViewById(R.id.btn_right);
        Button btn_counterwise = (Button) findViewById(R.id.btn_counterwise);
        Button btn_clockwise = (Button) findViewById(R.id.btn_clockwise);

        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//当按下时获取到屏幕中的xy位置
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.e("point", event.getX() + "," + event.getY());

//更多关于坐标转换的参考
                    ImageView imageView = findViewById(R.id.image_view);
                    Drawable drawable = imageView.getDrawable();

                    Rect imageBounds = drawable.getBounds();

//初始化bitmap的宽高
                    int intrinsicHeight = drawable.getIntrinsicHeight();
                    int intrinsicWidth = drawable.getIntrinsicWidth();

//可见image的宽高
                    int scaledHeight = imageBounds.height();
                    int scaledWidth = imageBounds.width();

//使用fitXY
                    float heightRatio = intrinsicHeight / scaledHeight;
                    float widthRatio = intrinsicWidth / scaledWidth;

//获取图像边界值
                    float scaledImageOffsetX = event.getX() - imageBounds.left;
                    float scaledImageOffsetY = event.getY() - imageBounds.top;

//根据你图像的缩放比例设置
                    synchronized (imageRetLock) {
                        returnImageOffset = true;
                        originalImageOffsetX = scaledImageOffsetX * widthRatio;
                        originalImageOffsetY = scaledImageOffsetY * heightRatio;
                    }
                }
                return true;
            }
        });

        View.OnTouchListener buttonListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int id = v.getId();
                Button curbtn = (Button) findViewById(id);
                int action = event.getAction();
                if (action == MotionEvent.ACTION_BUTTON_PRESS ||
                        action == MotionEvent.ACTION_DOWN ||
                        action == MotionEvent.ACTION_HOVER_ENTER ||
                        action == MotionEvent.ACTION_MASK ||
                        action == MotionEvent.ACTION_POINTER_DOWN ||
                        action == MotionEvent.ACTION_MOVE) {

                    curbtn.setBackgroundColor(Color.parseColor("#0000CD"));
                } else {
                    
                    curbtn.setBackgroundColor(Color.parseColor("#DDDDDD"));
                }
                // TODO Auto-generated method stub
                if (VelCmdTopic != null) {
                    if (runstate == RosState.SLAM || runstate == RosState.HOLD) {

                        if (action == MotionEvent.ACTION_BUTTON_PRESS ||
                                action == MotionEvent.ACTION_DOWN ||
                                action == MotionEvent.ACTION_HOVER_ENTER ||
                                action == MotionEvent.ACTION_MASK ||
                                action == MotionEvent.ACTION_POINTER_DOWN ||
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
                        } else {

                            String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                    0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                            for (int i = 0; i < 10; i++) {
                                try {
                                    Thread.sleep(5);
                                } catch (Exception e) {

                                }
                                client.send(MoveMsg);
                            }
                            robot_stop = true;
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

    RosState getButtonActType(int id) {
        switch (id) {
            case R.id.button_slam:
                return RosState.SLAM;
            case R.id.btn_nav:
                return RosState.NAV;
            case R.id.btn_grab:
                return RosState.GRAB;
        }
        return null;
    }

    int getButtonid(RosState s) {
        if (s == RosState.SLAM) {
            return R.id.button_slam;
        }
        if(s == RosState.NAV){
            return R.id.btn_nav;
        }
        if(s == RosState.GRAB){
            return R.id.btn_grab;
        }
        return 0;
    }

    public void onClick(View v) {
        if (connect_state == false) {
            info.setText("Not Connected");
            return;
        }
        String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
        robot_stop = true;
        client.send(MoveMsg);
        int id = v.getId();
        RosString ctrlstring = new RosString("NULL");
        Button curbtn = (Button) findViewById(id);
        Button statebtn = (Button) findViewById(getButtonid(runstate));
        try {
            if (runstate != RosState.IDLE) {
                if (runstate == RosState.SLAM) {
                    ctrlstring = new RosString("stop slam");
                }
                if (runstate == RosState.HOLD) {
                    ctrlstring = new RosString("stop hold");
                }
                if (runstate == RosState.NAV) {
                    ctrlstring = new RosString("stop nav");

                }
                if (runstate == RosState.GRAB) {
                    ctrlstring = new RosString("stop grab");
                }
                WebView webView = (WebView) findViewById(R.id.webView);
                webView.setVisibility(View.GONE);
                ctrlTopic.publish(ctrlstring);
                statebtn.setBackgroundColor(Color.parseColor("#FFEC8B"));
            }
            if (runstate == getButtonActType(id)) {
                runstate = RosState.IDLE;
                return;
            }
            if (ctrlTopic != null) {
                if (id == R.id.button_slam) {
                    ctrlstring = new RosString("start slam");

                    WebView webView = (WebView) findViewById(R.id.webView);
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl("http://"+RemoteIP+":8080/stream?topic=/kinect2/qhd/image_color&bitrate=200000&type=vp8&qmin=0&qmax=10");
                    ctrlTopic.publish(ctrlstring);
                    runstate = RosState.SLAM;

                }
                if (id == R.id.btn_nav) {
                    info.setText("btn_nav");
                    MyImageView imageview = (MyImageView) findViewById(R.id.image_view);
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageURL("ftp://robot:6@" + RemoteIP + "/home/robot/map.png");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //    设置Title的图标

                    //    设置Title的内容
                    builder.setTitle("Map Origin");
                    //    设置Content来显示一个信息
                    builder.setMessage("Which start point to use?");
                    //    设置一个PositiveButton
                    builder.setPositiveButton("Last Stop Point", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ctrlTopic.publish(new RosString("start nav last"));
                            Toast.makeText(MainActivity.this, "started nav with last", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //    设置一个NegativeButton
                    builder.setNegativeButton("Map Origin Start Point", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ctrlTopic.publish(new RosString("start nav origin"));
                            Toast.makeText(MainActivity.this, "started nav with origin", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //    显示出该对话框
                    builder.setCancelable(false);
                    builder.show();

                    runstate = RosState.NAV;
                }
                if (id == R.id.btn_hold) {
                    ctrlstring = new RosString("start hold");
                    ctrlTopic.publish(ctrlstring);
                    runstate = RosState.HOLD;
                }
                if (id == R.id.btn_grab) {
                    WebView webView = (WebView) findViewById(R.id.webView);
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl("http://"+RemoteIP+":8080/stream?topic=/kinect2/qhd/color_image");
                    ctrlstring = new RosString("start grab");
                    ctrlTopic.publish(ctrlstring);
                    Thread.sleep(5000);
                    EditText et = (EditText)findViewById(R.id.grabtext);
                    grabTopic.publish(new RosString("itemindex " + et.getText()));
                    runstate = RosState.GRAB;
                }
                curbtn.setBackgroundColor(Color.parseColor("#0000CD"));
                info.setText("successfully sent:" + ctrlstring.data);
            }
        } catch (Exception e) {
            info.setText("send fail");

        }
    }

    SendZeroThread sendzero = null;

    public void onClickConnect(View v) {
        EditText remoteaddr = (EditText) findViewById(R.id.addresstext);
        RemoteIP = remoteaddr.getText().toString();

        String fulladdr = "ws://" + RemoteIP + ":9090";
        client = new ROSBridgeClient(fulladdr);
        connect_state = client.connect();
        if (connect_state == false) {
            info.setText("Connect Fail to " + fulladdr);
        } else {
            info.setText("Connected");
            ctrlTopic = new com.jilk.ros.Topic<RosString>("/ctrlmsg", RosString.class, client);
            ctrlTopic.advertise();
            grabTopic =  new com.jilk.ros.Topic<RosString>("/itemmsg", RosString.class, client);
            VelCmdTopic = new com.jilk.ros.Topic<GeoTwist>("/cmd_vel", GeoTwist.class, client);
            VelCmdTopic.advertise();
            if (sendzero == null) {
                sendzero = new SendZeroThread();
                sendzero.start();
            }
        }
    }

    class SendZeroThread extends Thread {
        public void run() {
            while (true) {
                try {
                    if (client == null) {
                        continue;
                    }
                    if (VelCmdTopic == null) {
                        continue;
                    }
                    if (robot_stop) {
                        String MoveMsg = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0 + "}}}";
                        client.send(MoveMsg);
                    }
                    Thread.sleep(10);
                } catch (Exception e) {

                }
            }
        }
    }

    class MapListener extends Thread {
        public void run() {
            while (true) {
                synchronized (imageRetLock) {
                    if (returnImageOffset) {
                        ImageView imageView = (ImageView) findViewById(R.id.image_view);
                        imageView.setVisibility(View.GONE);
                        WebView webView = (WebView) findViewById(R.id.webView);
                        webView.setVisibility(View.VISIBLE);
                        ctrlTopic.publish(new RosString("movebase "+originalImageOffsetX*0.05+" " + originalImageOffsetY*0.05));
                        Toast.makeText(MainActivity.this, "movebase "+originalImageOffsetX*0.05+" " + originalImageOffsetY*0.05, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (Exception e) {

                }
            }

        }
    }


}
