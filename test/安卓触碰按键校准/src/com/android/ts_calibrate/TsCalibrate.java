/*
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/
package com.android.ts_calibrate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.os.SystemProperties;

public class TsCalibrate extends Activity {
    final String TAG = "TsCalibration";
    private Button myok;
    CrossView myview;
    int direction;
    int x1;
    int y1;
    private Calibrate cal;

	int UI_SCREEN_WIDTH;
	int UI_SCREEN_HEIGHT;
    int xList[] = {0,0,0,0,0};
//            50, UI_SCREEN_WIDTH - 50, UI_SCREEN_WIDTH - 50, 50, UI_SCREEN_WIDTH / 2
//    };

    int yList[] = {0,0,0,0,0};
//            50, 50, UI_SCREEN_HEIGHT - 50, UI_SCREEN_HEIGHT - 50, UI_SCREEN_HEIGHT / 2
//    };

    private static final int DIALOG_START_CONFIRM = 1;
    private static final int DIALOG_FINISH_CALIBRATE = 2;
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_FINISH_CALIBRATE:
        	Dialog dia = new AlertDialog.Builder(TsCalibrate.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_finish_dialog_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Toast.makeText(getBaseContext(), "Calibrate Done!", Toast.LENGTH_SHORT).show();
                        TsCalibrate.this.finish();
                    }
                }).create();
/*
setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						direction = 0;
						TsCalibrate.this.finish();
					}
                })
*/
        	dia.setCanceledOnTouchOutside(false);
        	return dia;
        }
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myview = new CrossView(this);
        setContentView(myview);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        //SystemProperties.set("ts.calibrate", "start");

   		WindowManager windowManager = getWindowManager();
   		Display display = windowManager.getDefaultDisplay();
    	UI_SCREEN_WIDTH = 800;//display.getWidth();
    	UI_SCREEN_HEIGHT = 600;//display.getHeight();
    	xList[0] = 50;
    	xList[1] = UI_SCREEN_WIDTH - 50;
    	xList[2] = UI_SCREEN_WIDTH - 50;
    	xList[3] = 50;
    	xList[4] = UI_SCREEN_WIDTH / 2;
    	yList[0] = 50;
    	yList[1] = 50;
    	yList[2] = UI_SCREEN_HEIGHT - 50;
    	yList[3] = UI_SCREEN_HEIGHT - 50;
    	yList[4] = UI_SCREEN_HEIGHT / 2;
    	Log.d(TAG, "UI_SCREEN_WIDTH: + " + UI_SCREEN_WIDTH + " UI_SCREEN_HEIGHT: " + UI_SCREEN_HEIGHT);
        cal = new Calibrate();
        direction = 0;
		cal.del_calfile();
		SystemProperties.set("gzsd.calibrate", "reset");
        myview.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, event.getX() + "," + event.getY() + ",------> DownTime : " + event.getDownTime() + " event.getAction(): " + event.getAction());
                //if (event.getAction() == MotionEvent.ACTION_UP) {
	                v.invalidate();
	                //x1 = (int)(( event.getX() * 2047 ) / UI_SCREEN_WIDTH);//4095
	                //y1 = (int)(( event.getY() * 2047 ) / UI_SCREEN_HEIGHT);//usb touch x 0x7ff
	                x1 = (int)(event.getX());//4095
	                y1 = (int)( event.getY());//usb touch x 0x7ff
	                Log.d(TAG, " --->x1=" + x1 + "," + " --->y1=" + y1);
	
	                if (direction <= 4) {
	                    cal.get_sample(direction, x1, y1, xList[direction], yList[direction]);
	                    direction++;
	                }
                //}
                return false;
            }
        });
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            //SystemProperties.set("ts.calibrate", "done");
            Toast.makeText(getBaseContext(), "Calibrate BACK key cancel!", Toast.LENGTH_SHORT).show();
            TsCalibrate.this.finish();
            break;
        case KeyEvent.KEYCODE_MENU:
            if(direction == 5) {
                direction++;
                cal.calibrate_main();
                //SystemProperties.set("ts.calibrate", "done");
                showDialog(DIALOG_FINISH_CALIBRATE);
            }
            break;
        }
        return false;
    }
    
    public class CrossView extends View {
        public CrossView(Context context) {
            super(context);
        }

        /*
        @Override
		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			//return super.onTouchEvent(event);
            Log.d(TAG, event.getX() + "," + event.getY() + ",------> DownTime : " + event.getDownTime() + " event.getAction(): " + event.getAction());
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //v.invalidate();
                x1 = (int)(( event.getX() * 4095 ) / UI_SCREEN_WIDTH);
                y1 = (int)(( event.getY() * 4095 ) / UI_SCREEN_HEIGHT);
                Log.d(TAG, " --->x1=" + x1 + "," + " --->y1=" + y1);

                if (direction <= 4) {
                    cal.get_sample(direction, x1, y1, xList[direction], yList[direction]);
                    direction++;
                    
                }
            }
        	return true;
		}
		*/

		public void onDraw(Canvas canvas) {

            Paint paint = new Paint();
            paint.setColor(Color.WHITE); 
           // paint.setTextSize(24);
            //canvas.drawText(getResources().getString(R.string.screen_note), 112, 420, paint);
            paint.setTextSize(48);
            if (direction == 0) {
                canvas.drawLine(30, 50, 70, 50, paint);
                canvas.drawLine(50, 30, 50, 70, paint);
                canvas.drawText(getResources().getString(R.string.screen_top_left),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 1) {
                canvas.drawLine(UI_SCREEN_WIDTH - 70, 50, UI_SCREEN_WIDTH - 30, 50, paint);
                canvas.drawLine(UI_SCREEN_WIDTH - 50, 30, UI_SCREEN_WIDTH - 50, 70, paint);
                canvas.drawText(getResources().getString(R.string.screen_top_right),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 2) {
                canvas.drawLine(UI_SCREEN_WIDTH - 70, UI_SCREEN_HEIGHT - 50, UI_SCREEN_WIDTH - 30,
                        UI_SCREEN_HEIGHT - 50, paint);
                canvas.drawLine(UI_SCREEN_WIDTH - 50, UI_SCREEN_HEIGHT - 70, UI_SCREEN_WIDTH - 50,
                        UI_SCREEN_HEIGHT - 30, paint);
                canvas.drawText(getResources().getString(R.string.screen_bottom_right),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 3) {
                canvas.drawLine(30, UI_SCREEN_HEIGHT - 50, 70, UI_SCREEN_HEIGHT - 50, paint);
                canvas.drawLine(50, UI_SCREEN_HEIGHT - 70, 50, UI_SCREEN_HEIGHT - 30, paint);
                canvas.drawText(getResources().getString(R.string.screen_bottom_left),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 4) {
                canvas.drawLine(UI_SCREEN_WIDTH / 2 - 20, UI_SCREEN_HEIGHT / 2,
                        UI_SCREEN_WIDTH / 2 + 20, UI_SCREEN_HEIGHT / 2, paint);
                canvas.drawLine(UI_SCREEN_WIDTH / 2, UI_SCREEN_HEIGHT / 2 - 20,
                        UI_SCREEN_WIDTH / 2, UI_SCREEN_HEIGHT / 2 + 20, paint);
                canvas.drawText(getResources().getString(R.string.screen_center),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else {
               // paint.setTextSize(32);
                //canvas.drawText(getResources().getString(R.string.screen_done), UI_SCREEN_WIDTH/2, UI_SCREEN_HEIGHT/2, paint);
                if(direction == 5) {
                    direction++;
					cal.calibrate_main();
                    SystemProperties.set("gzsd.calibrate", "done");
                    showDialog(DIALOG_FINISH_CALIBRATE);
                }
            }
            super.onDraw(canvas);
        }
    }
}

