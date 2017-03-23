package com.grawin.couchto5k.data;

import java.util.ArrayList;

/**
 * Created by rgraw on 3/4/2016.
 */
public class WorkoutList {
    private ArrayList<WorkoutEntry> workoutList;
    private int totalElapsed_sec;
    private int totalWorkoutTime_sec;
    private int currentWorkoutStep;
    private int currentElapsed_sec;
    private int currentRemaining_sec;

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
    

    public ArrayList<WorkoutEntry> getWorkoutList() {
        return workoutList;
    }

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

    public int getTotalElapsed_sec() {
        return totalElapsed_sec;
    }

    public void setTotalElapsed_sec(int totalElapsed_sec) {
        this.totalElapsed_sec = totalElapsed_sec;
    }

    public int getTotalWorkoutTime_sec() {
        return totalWorkoutTime_sec;
    }

    public void setTotalWorkoutTime_sec(int totalWorkoutTime_sec) {
        this.totalWorkoutTime_sec = totalWorkoutTime_sec;
    }

    public int getCurrentWorkoutStep() {
        return currentWorkoutStep;
    }

    public void setCurrentWorkoutStep(int currentWorkoutStep) {
        this.currentWorkoutStep = currentWorkoutStep;
    }

    public int getCurrentElapsed_sec() {
        return currentElapsed_sec;
    }

    public void setCurrentElapsed_sec(int currentElapsed_sec) {
        this.currentElapsed_sec = currentElapsed_sec;
    }

    public int getCurrentRemaining_sec() {
        return currentRemaining_sec;
    }

    public WorkoutEntry getCurrentWorkoutEntry() {
        return workoutList.get(currentWorkoutStep);
    }
}
