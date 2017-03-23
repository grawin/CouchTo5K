package com.grawin.couchto5k.data;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.grawin.couchto5k.R;

/**
 * Created by Ryan on 2/29/2016.
 */
public class DataStore {

    /** Number of workouts in total program. */
    private static int workoutCount;

    /** Static ID for the notification used while timer service is active. */
    public static final int NOTIFICATION_ID = 1;

    /** Persistent storage for circle progress bar animation. Required for resuming activity. */
    public static ObjectAnimator animation = null;

    // Current Workout Data

    private static String workoutName;
    private static WorkoutList workoutList = new WorkoutList();
    private static boolean workoutComplete;

    // Saved preferences

    private static SharedPreferences sharedPrefs;

    private static String prefStrSavedDay;

    // Option menu saved preferences

    private static String prefStrNotifSound;

    private static String prefStrNotifVibrate;

    private static String prefStrNotifCoundown;

    /**
     * Initialize any data that depends on application context.
     * @param context The main application context.
     */
    public static void initialize(Context context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefStrSavedDay = context.getString(R.string.saved_progress_day);
        prefStrNotifSound = context.getString(R.string.pref_key_notif_sound);
        prefStrNotifVibrate = context.getString(R.string.pref_key_notif_vibrate);
        prefStrNotifCoundown = context.getString(R.string.pref_key_notif_countdown);
    }

    public static String getWorkoutName() {
        return workoutName;
    }

    public static void setWorkoutName(String workoutName) {
        DataStore.workoutName = workoutName;
    }

    public static WorkoutList getWorkoutList() {
        return DataStore.workoutList;
    }

    public static void setWorkoutList(WorkoutList workoutList) {
        if (workoutList != null) {
            DataStore.workoutList = workoutList;
        }
    }

    public static boolean isWorkoutComplete() {
        return workoutComplete;
    }

    public static void setWorkoutComplete(boolean workoutComplete) {
        DataStore.workoutComplete = workoutComplete;

        if (workoutComplete) {
            int currentWorkout = DataStore.getProgress();
            if (currentWorkout < DataStore.getWorkoutCount()) {
                DataStore.saveProgress(currentWorkout + 1);
            }
        }
    }

    public static int getWorkoutCount() {
        return workoutCount;
    }

    public static void setWorkoutCount(int workoutCount) {
        DataStore.workoutCount = workoutCount;
    }

    // Shared Preferences Methods

    public static int getProgress() {
        return sharedPrefs.getInt(prefStrSavedDay, 0);
    }

    public static void saveProgress(int newProgress) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(prefStrSavedDay, newProgress);
        editor.apply();
    }

    public static boolean isSoundNotifEnabled() {
        return sharedPrefs.getBoolean(prefStrNotifSound, true);
    }

    public static boolean isVibrateNotifEnabled() {
        return sharedPrefs.getBoolean(prefStrNotifVibrate, false);
    }

    public static int getNotifCountdownSec() {
        return sharedPrefs.getInt(prefStrNotifCoundown, 3);
    }
}
