package app.ride.Services;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class TimerService extends Service {

    private final static String TAG = "BroadcastService";

    public static final String COUNTDOWN_BR = "your_package_name.countdown_br";
    Intent timer_intent = new Intent(COUNTDOWN_BR);

    CountDownTimer timer = null;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Starting timer...");

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                timer_intent.putExtra("countdown", millisUntilFinished);
                sendBroadcast(timer_intent);
            }

            @Override
            public void onFinish() {

                Log.i(TAG, "Timer finished");
            }
        };

        timer.start();
    }

    @Override
    public void onDestroy() {

        timer.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
