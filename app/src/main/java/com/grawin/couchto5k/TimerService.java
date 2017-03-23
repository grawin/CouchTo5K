package com.grawin.couchto5k;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.grawin.couchto5k.data.DataStore;
import com.grawin.couchto5k.data.WorkoutList;

/**
 * Created by Ryan on 2/26/2016.
 */
public class TimerService extends Service {
    /** Used to identify the source of a log message. */
    private final static String TAG = TimerService.class.getName();

    /** CountDownTimer is inaccurate so use a tick rate less than a second. */
    private static final long TICK_RATE_MS = 100;

    public static final String TIMER_BR = "com.grawin.couchto5k.timer_br";
    Intent bi = new Intent(TIMER_BR);

    /** Underlying timer implementation. */
    CountDownTimer mTimer = null;

    /** The remaining time in seconds. */
    private long mRemaining_sec;
    /** The elapsed time in seconds. */
    private long mElapsed_sec; // TODO - might need this for pausing? If not then get rid of it...
    /** The duration of the timer in seconds. */
    private long mDuration_sec;

    // TODO - implement pausing...
    private boolean isStarted;
    private boolean isPaused;

    private NotificationManager mNotifier;
    private NotificationCompat.Builder mNotifyBuilder;

    /** Media player for countdown sound notification. */
    MediaPlayer mBeepPlayer = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        mBeepPlayer.release();
        // Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mBeepPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep_1khz);

        WorkoutList workoutList = DataStore.getWorkoutList();
        mDuration_sec = workoutList.getTotalWorkoutTime_sec() - workoutList.getTotalElapsed_sec();
        mRemaining_sec = mDuration_sec;

        setupNotification();

        // Log.i(TAG, "Starting timer...");

        createTimer(mDuration_sec * 1000);
        mTimer.start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    // TODO - Notification should probably go in its own class?
    private void setupNotification() {
        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, WorkoutActivity.class);

        // Flags needed to pass Intent with notification.
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // FLAG_UPDATE_CURRENT needed to pass Intent with notification and call onNewIntent.
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifyBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(DataStore.getWorkoutName())
                .setSmallIcon(R.drawable.ic_directions_run_white_18dp)
                .setContentIntent(intent);

        Notification notification = mNotifyBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR
                | Notification.FLAG_ONGOING_EVENT;

        mNotifier = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifier.notify(DataStore.NOTIFICATION_ID, notification);
    }

    private void createTimer(long duration_ms) {
        mTimer = new CountDownTimer(duration_ms, TICK_RATE_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Determine if this is a new second.
                final int newRemaining_sec = Math.round((float) millisUntilFinished / 1000.0f);
                if (newRemaining_sec != mRemaining_sec) {
                    // Grab the workout list for decision making purposes.
                    WorkoutList workoutList = DataStore.getWorkoutList();

                    // Need to keep track of ticks in case weirdness happens where multiple
                    // seconds pass. This can happen if the phone is lagging so play catchup
                    // and handle multiple ticks.
                    long ticks =  mRemaining_sec - newRemaining_sec;
                    for (int i = 0; i < ticks; i++) {
                        workoutList.tick();
                    }

                    // Update state with new time.
                    mRemaining_sec = newRemaining_sec;
                    mElapsed_sec = mDuration_sec - mRemaining_sec;

                    // Log.i(TAG, "Countdown seconds remaining: " + mRemaining_sec);



                    String notifText =
                    workoutList.getCurrentWorkoutEntry().getName() + "   " +
                            Utils.formatTimeString(workoutList.getCurrentRemaining_sec());

                    mNotifyBuilder.setContentText(notifText);

                    Notification notification = mNotifyBuilder.build();
                    notification.flags |= Notification.FLAG_NO_CLEAR
                            | Notification.FLAG_ONGOING_EVENT;

                    mNotifier.notify(DataStore.NOTIFICATION_ID, notification);

                    // Handle notifications
                    final boolean sound = DataStore.isSoundNotifEnabled();
                    final boolean vibrate = DataStore.isVibrateNotifEnabled();
                    if (sound || vibrate) {
                        final int notifCount = DataStore.getNotifCountdownSec();
                        final int stepRemaining_sec = workoutList.getCurrentRemaining_sec();

                        if (stepRemaining_sec > 0 && stepRemaining_sec <= notifCount) {
                            //Log.i(TAG, "s " + sound + " v " + vibrate + " n " + notifCount);
                            if (sound) {
                                mBeepPlayer.start();
                            }
                            if (vibrate) {
                                ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
                            }
                        }
                    }

                    // Inform subscribers of tick event now that data store is updated.
                    bi.putExtra("end", false);
                    sendBroadcast(bi);
                }
            }

            @Override
            public void onFinish() {
                isStarted = false;

                mNotifyBuilder.setContentText(getString(R.string.str_complete));
                Notification notification = mNotifyBuilder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;


                mNotifier.notify(DataStore.NOTIFICATION_ID, notification);

                DataStore.setWorkoutComplete(true);

                // Log.i(TAG, "Timer finished");
                bi.putExtra("end", true);
                sendBroadcast(bi);
            }
        };
    }
}
