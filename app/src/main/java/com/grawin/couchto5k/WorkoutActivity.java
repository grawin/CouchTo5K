package com.grawin.couchto5k;

import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.grawin.couchto5k.data.DataStore;
import com.grawin.couchto5k.data.WorkoutEntry;
import com.grawin.couchto5k.data.WorkoutList;

public class WorkoutActivity extends AppCompatActivity {
    /**
     * Used to identify the source of a log message.
     */
    private final static String TAG = WorkoutActivity.class.getName();

    /**
     * The timer button.
     */
    Button mTimerButton = null;

    /**
     * Overall daily workout progress bar.
     */
    ProgressBar mOverallProgressBar = null;

    /**
     * Current step progress bar.
     */
    ProgressBar mProgressBar = null;

    /**
     * Timer text for the "step" progress bar.
     */
    TextView mTimerText = null;

    /**
     * Name of current workout step being performed.
     */
    TextView mWorkoutText = null;

    TextView mOverallTimeText = null;
    TextView mOverallTimePercent = null;

    /**
     * Indicates if the timer was started.
     **/
    boolean mTimerStarted = false;

    /**
     * Broadcast receiver for the timer service.
     */
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isEndEvent = intent.getBooleanExtra("end", false);
            if (!isEndEvent) {
                handleTickEvent(intent);
            } else {
                handleEndEvent();
            }
        }
    };

    // Overriden activity methods.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Stores intent so that getIntent() returns this new intent.
        setIntent(intent);

        setupUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI();
        registerReceiver(br, new IntentFilter(TimerService.TIMER_BR));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, TimerService.class));
        //Log.i(TAG, "Stopped service");
        super.onDestroy();
    }

    /**
     * Handles setting up the user interface based on the stored workout data.
     */
    private void setupUI() {
        WorkoutList workoutList = DataStore.getWorkoutList();
        // If for some reason the workout list is null try to restore it from shared prefs.
        if (workoutList == null || workoutList.getCurrentWorkoutEntry() == null) {

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            //SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
            Gson gson = new Gson();
            String json = mPrefs.getString("WorkoutListObjectPref", null);
            workoutList = gson.fromJson(json, WorkoutList.class);

            if (workoutList == null) {
                // If it's still null something is really wrong... return to main screen.
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                DataStore.setWorkoutList(workoutList);
            }
        }

        setTitle(DataStore.getWorkoutName());

        mTimerButton = (Button) findViewById(R.id.timerButton);

        final int currentRemaining_sec = workoutList.getCurrentRemaining_sec();
        final int currentDuration_sec = workoutList.getCurrentWorkoutEntry().getTime_sec();

        // Setup circular progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.activityProgressBar);
        mProgressBar.setMax(currentDuration_sec);
        mProgressBar.setProgress(currentRemaining_sec);

        // Store off animation on DataStore to allow for resuming where left off when re-opening
        // this activity. Kind of hack-ish, but it works.
        if (DataStore.animation == null) {
            ObjectAnimator animation = ObjectAnimator.ofInt(mProgressBar, "progress", 0, 10000);
            animation.setDuration(300); //in milliseconds
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
            DataStore.animation = animation;
        }

        mTimerText = (TextView) findViewById(R.id.timerText);
        updateTimerText(currentRemaining_sec);

        mWorkoutText = (TextView) findViewById(R.id.workoutTextView);
        updateCurrentActionText();

        mOverallTimeText = (TextView) findViewById(R.id.overallTime);
        mOverallTimePercent = (TextView) findViewById(R.id.overallPercent);

        // Setup overall progress bar
        mOverallProgressBar = (ProgressBar) findViewById(R.id.overallProgressBar);

        mOverallProgressBar.setProgress(workoutList.getTotalElapsed_sec());
        mOverallProgressBar.setMax(workoutList.getTotalWorkoutTime_sec());
        // Set the drawable as progress drawable


        Drawable progDraw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_progressbar);
        mOverallProgressBar.setProgressDrawable(progDraw);

        // If the workout is complete then update the UI to reflect that.
        if (DataStore.isWorkoutComplete()) {
            updateForComplete();
        }
    }

    /**
     * Handles the start timer button click.
     * @param view
     */
    public void handleStartTimerButton(View view) {
        if (DataStore.isWorkoutComplete()) {
            finish();
        }

        if (!mTimerStarted) {
            // Start timer service
            Intent intent = new Intent(this, TimerService.class);
            startService(intent);

            // Update state
            mTimerStarted = true;
            mTimerButton.setText(R.string.pause);
            mProgressBar.setMax(DataStore.getWorkoutList().getCurrentWorkoutEntry().getTime_sec());
        } else {
            stopService(new Intent(this, TimerService.class));
            mTimerStarted = false;
            mTimerButton.setText(R.string.start);

        }
    }

    /**
     * Updates the text of the current workout step (e.g. "walk", "jog").
     */
    private void updateCurrentActionText() {
        String str = DataStore.getWorkoutList().getCurrentWorkoutEntry().getName();
        String nameStr = str.substring(0, 1).toUpperCase() + str.substring(1);
        mWorkoutText.setText(nameStr);
    }

    /**
     * Updates the timer text display.
     * @param remaining_sec The remaining time to display in seconds.
     */
    private void updateTimerText(int remaining_sec) {
        mTimerText.setText(Utils.formatTimeString(remaining_sec));
    }

    /**
     * Handles the timer tick event broadcast by the timer service.
     * @param intent Timer service intent data.
     */
    private void handleTickEvent(Intent intent) {
        if (intent.getExtras() != null) {

            updateCurrentActionText();

            WorkoutList workoutList = DataStore.getWorkoutList();
            int remaining_sec = workoutList.getCurrentRemaining_sec();

            // Current activity progress bar.
            mProgressBar.setProgress(remaining_sec);
            WorkoutEntry entry = workoutList.getCurrentWorkoutEntry();
            mProgressBar.setMax(entry.getTime_sec());

            // Update timer text.
            updateTimerText(DataStore.isWorkoutComplete() ? 0 : remaining_sec);

            // Update overall progress bar
            int totalTime_sec = workoutList.getTotalWorkoutTime_sec();
            int totalElapsed_sec = workoutList.getTotalElapsed_sec();
            float percentComplete = ((float) totalElapsed_sec / (float) totalTime_sec) * 100.0f;

            String percentStr = String.format("%d%%", (int) percentComplete);
            mOverallTimePercent.setText(percentStr);

            String timeStr = Utils.formatTimeString(totalTime_sec - totalElapsed_sec);
            mOverallTimeText.setText(timeStr);

            mOverallProgressBar.setProgress(totalElapsed_sec);
        }
    }

    /**
     * Updates the display when the workout is complete. Performs any needed cleanup.
     */
    private void updateForComplete() {
        // Update circular progress bar and timer text.
        mProgressBar.setMax(1);
        mProgressBar.setProgress(1);
        updateTimerText(0);

        mWorkoutText.setText(getString(R.string.str_complete));

        mTimerButton.setText(getString(R.string.finish));

        String percentStr = String.format("%d%%", 100);
        mOverallTimePercent.setText(percentStr);
        String timeStr = Utils.formatTimeString(0);
        mOverallTimeText.setText(timeStr);

        // Cancel notification if it still exists.
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(DataStore.NOTIFICATION_ID);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // If the workout is complete then just honor the key press.
            if (DataStore.isWorkoutComplete()) {
                return super.onKeyDown(keyCode, event);
            }
            // Otherwise build an alert to the user that they are quitting.
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Exiting Workout")
                    .setMessage("Are you sure you want to exit this workout? Current progress will be lost.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel notification if it still exists.
                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(DataStore.NOTIFICATION_ID);

                            WorkoutActivity.this.finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Handles the "end event" broadcast by the timer service.
     */
    private void handleEndEvent() {
        updateForComplete();
    }
}
