package pdm.hellotome.Adapters;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import java.util.Timer;
import java.util.TimerTask;

public abstract class DoubleTapListener implements View.OnClickListener {

    private Timer timer = null;  //at class level;
    private int DELAY   = 400;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
            processDoubleClickEvent(v);
        } else {
            processSingleClickEvent(v);
        }
        lastClickTime = clickTime;
    }

    public void processSingleClickEvent(final View v){

        final Handler handler=new Handler();
        final Runnable mRunnable=new Runnable(){
            public void run(){
                onSingleClick(v); //Do what ever u want on single click
            }
        };

        TimerTask timertask=new TimerTask(){
            @Override
            public void run(){
                handler.post(mRunnable);
            }
        };
        timer=new Timer();
        timer.schedule(timertask,DELAY);
    }


    public void processDoubleClickEvent(View v){
        if(timer!=null)
        {
            timer.cancel(); //Cancels Running Tasks or Waiting Tasks.
            timer.purge();  //Frees Memory by erasing cancelled Tasks.
        }

        onDoubleClick(v);//Do what ever u want on Double Click
    }

    public abstract void onSingleClick(View v);

    public abstract void onDoubleClick(View v);
}