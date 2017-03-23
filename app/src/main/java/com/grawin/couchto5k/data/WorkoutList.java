package com.grawin.couchto5k.data;

import java.util.ArrayList;

/**
 * Created by rgraw on 3/4/2016.
 *
 * Workout list contains all data associated with a given workout (current day's routine).
 */
public class WorkoutList {
    /** The list of steps in the workout. */
    private ArrayList<WorkoutEntry> workoutList;
    /** The total elapsed time in seconds. */
    private int totalElapsed_sec;
    /** The total workout duration in seconds. */
    private int totalWorkoutTime_sec;
    /** The current step in the workout (entry in the workout list). */
    private int currentWorkoutStep;
    /** The current elapsed time in seconds. */
    private int currentElapsed_sec;
    /** The current remaining time in seconds. */
    private int currentRemaining_sec;

    /**
     * Tick method to update workout time. Tick time is one second.
     */
    public void tick() {
        WorkoutEntry entry = workoutList.get(currentWorkoutStep);
        if (entry == null) {
            return;
        }

        totalElapsed_sec++;
        currentElapsed_sec++;
        currentRemaining_sec--;

        if (currentElapsed_sec >= entry.getTime_sec()) {
            if (currentWorkoutStep + 1 < workoutList.size()) {
                currentWorkoutStep++;
            }

            currentElapsed_sec = 0;
            currentRemaining_sec = workoutList.get(currentWorkoutStep).getTime_sec();
        }
    }

    /**
     * Returns the workout list.
     * @return The workout list.
     */
    public ArrayList<WorkoutEntry> getWorkoutList() {
        return workoutList;
    }

    /**
     * Sets the workout list.
     * @param workoutList The workout list.
     */
    public void setWorkoutList(ArrayList<WorkoutEntry> workoutList) {
        if (workoutList == null || workoutList.isEmpty()) {
            return;
        }

        this.workoutList = workoutList;

        // Handle new list
        DataStore.setWorkoutComplete(false);

        totalElapsed_sec = 0;
        totalWorkoutTime_sec = 0;
        currentWorkoutStep = 0;
        currentElapsed_sec = 0;
        currentRemaining_sec = workoutList.get(0).getTime_sec();
        for (WorkoutEntry entry : workoutList) {
            totalWorkoutTime_sec += entry.getTime_sec();
        }
    }

    /**
     * Returns the total elapsed time in seconds.
     * @return Elapsed time in seconds.
     */
    public int getTotalElapsed_sec() {
        return totalElapsed_sec;
    }

    /**
     * Returns the total workout time in seconds.
     * @return The total workout time in seconds.
     */
    public int getTotalWorkoutTime_sec() {
        return totalWorkoutTime_sec;
    }

    /**
     * Returns the current remaining time in seconds.
     * @return The current remaining time in seconds.
     */
    public int getCurrentRemaining_sec() {
        return currentRemaining_sec;
    }

    /**
     * Returns the current workout entry (current step).
     * @return The current workout step.
     */
    public WorkoutEntry getCurrentWorkoutEntry() {
        return workoutList.get(currentWorkoutStep);
    }
}
