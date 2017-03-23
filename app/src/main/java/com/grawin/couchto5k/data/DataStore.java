package com.grawin.couchto5k.data;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.grawin.couchto5k.R;

/**
 * Created by Ryan on 2/29/2016.
 *
 * Data store class to save common data between activities.
 * Also provides interface to application preferences (settings).
 */
public class DataStore {

    /** Number of workouts in total program. */
    private static int workoutCount;

    /** Static ID for the notification used while timer service is active. */
    public static final int NOTIFICATION_ID = 1;

    /** Persistent storage for circle progress bar animation. Required for resuming activity. */
    public static ObjectAnimator animation = null;

    /** Current workout name. */
    private static String workoutName;
    /** Current workout list. */
    private static WorkoutList workoutList = new WorkoutList();
    /** Indicates whether the current workout session is complete. */
    private static boolean workoutComplete;

    /** Shared preferences. */
    private static SharedPreferences sharedPrefs;
    /** Save day preference. */
    private static String prefStrSavedDay;
    /** Saved preference for notification sound. */
    private static String prefStrNotifSound;
    /** Saved preference for vibrate. */
    private static String prefStrNotifVibrate;
    /** Saved preference for notification countdown. */
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

    // Data store methods.

    /**
     * Gets the workout name.
     * @return The workout name.
     */
    public static String getWorkoutName() {
        return workoutName;
    }

    /**
     * Sets the workout name.
     * @param workoutName The workout name.
     */
    public static void setWorkoutName(String workoutName) {
        DataStore.workoutName = workoutName;
    }

    /**
     * Gets the workout list.
     * @return The workout list.
     */
    public static WorkoutList getWorkoutList() {
        return DataStore.workoutList;
    }

    /**
     * Sets the workout list.
     * @param workoutList The workout list.
     */
    public static void setWorkoutList(WorkoutList workoutList) {
        if (workoutList != null) {
            DataStore.workoutList = workoutList;
        }
    }

    /**
     * Returns whether the current workout is complete.
     * @return Workout complete flag.
     */
    public static boolean isWorkoutComplete() {
        return workoutComplete;
    }

    /**
     * Sets whether the workout is complete.
     * If complete then it updates the stored workout program progress.
     * @param workoutComplete TRUE if workout is complete.
     */
    public static void setWorkoutComplete(boolean workoutComplete) {
        DataStore.workoutComplete = workoutComplete;

        if (workoutComplete) {
            int currentWorkout = DataStore.getProgress();
            if (currentWorkout < DataStore.getWorkoutCount()) {
                DataStore.saveProgress(currentWorkout + 1);
            }
        }
    }

    /**
     * Returns the workout count.
     * @return The workout count.
     */
    public static int getWorkoutCount() {
        return workoutCount;
    }

    /**
     * Sets the workout count.
     * @param workoutCount The workout count.
     */
    public static void setWorkoutCount(int workoutCount) {
        DataStore.workoutCount = workoutCount;
    }

    // Shared Preferences methods.

    /**
     * Returns the stored progress from the shared preferences.
     * @return Stored progress.
     */
    public static int getProgress() {
        return sharedPrefs.getInt(prefStrSavedDay, 0);
    }

    /**
     * Sets the progress value in the shared preferences.
     * @param newProgress The progress indicator.
     */
    public static void saveProgress(int newProgress) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(prefStrSavedDay, newProgress);
        editor.apply();
    }

    /**
     * Returns whether notification sounds are enabled.
     * @return TRUE if notification sounds are enabled.
     */
    public static boolean isSoundNotifEnabled() {
        return sharedPrefs.getBoolean(prefStrNotifSound, true);
    }

    /**
     * Returns whether vibrate notifications are enabled.
     * @return TRUE if vibrate is enabled.
     */
    public static boolean isVibrateNotifEnabled() {
        return sharedPrefs.getBoolean(prefStrNotifVibrate, false);
    }

    /**
     * Returns the notification countdown value in seconds.
     * @return Countdown in seconds.
     */
    public static int getNotifCountdownSec() {
        return sharedPrefs.getInt(prefStrNotifCoundown, 3);
    }
}
