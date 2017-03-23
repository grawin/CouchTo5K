package com.grawin.couchto5k;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.grawin.couchto5k.data.DataStore;
import com.grawin.couchto5k.data.WorkoutEntry;
import com.grawin.couchto5k.data.WorkoutList;
import com.grawin.couchto5k.prefs.MainPreferenceActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /** Spinner to select a workout from the overall plan. Defaults to current progress. */
    private Spinner workoutSpinner;

    /** String list to populate the selected workout list view. */
    private ArrayList<String> listViewData = new ArrayList<>();

    /** The array adapter used to update the selected workout list view. */
    ArrayAdapter<String> listViewAdapter = null;

    /** List to map spinner entry to a workout entry. */
    private ArrayList<ArrayList<WorkoutEntry>> spinnerMap = new ArrayList<>();

    /** Overall workout plan list. */
    private ArrayList<ArrayList<ArrayList<WorkoutEntry>>> overallList = new ArrayList<>();

    /** The current workout index out of the entire list of workouts. */
    int currentWorkoutIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the data store class that is used to access shared preferences and static
        // run-time data that lasts the lifetime of this application instance.
        DataStore.initialize(getApplicationContext());

        currentWorkoutIndex = DataStore.getProgress();
        // TODO RPG - add overall progress bar showing how far along the user is in the entire workout program!

        // Build the workout list based on the default XML file.
        // TODO - probably put this and "buildWorkoutList" in it's own XML parsing util class...
        try {
            XmlPullParser xpp = getResources().getXml(R.xml.default_workout);
            buildWorkoutList(xpp);
            DataStore.setWorkoutCount(overallList.size());
        } catch (Resources.NotFoundException | XmlPullParserException | IOException e) {
            // TODO - do something meaningful, raise an alert or something saying app data is corrupt
            e.printStackTrace();
        }

        // Setup the list view for displaying workout contents.
        ListView listview = (ListView) findViewById(R.id.id_list_view);
        View header = getLayoutInflater().inflate(R.layout.header, null);
        listview.addHeaderView(header);
        listViewAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listViewData);
        listview.setAdapter(listViewAdapter);

        // Setup the workout list spinner UI.
        setupSpinner();
    }

    @Override
    public void onResume() {
        super.onResume();

        currentWorkoutIndex = DataStore.getProgress();
        // Set current selection based on stored progress value.
        workoutSpinner.setSelection(DataStore.getProgress());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle each menu option, currently only preferences.
        switch (item.getItemId()) {
            case R.id.preferences: {
                Intent intent = new Intent(this, MainPreferenceActivity.class);
                startActivity(intent);
                return true;
            }
            // TODO RPG - consider adding an "about" or "help" menu option and respective activities...
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleStartButton(View view) {
        int spinnerValue = workoutSpinner.getSelectedItemPosition();
        if (spinnerValue != AdapterView.INVALID_POSITION) {
            currentWorkoutIndex = spinnerValue;
        }
        DataStore.saveProgress(currentWorkoutIndex);
        launchWorkoutActivity(currentWorkoutIndex);
    }

    public void handleResumeButton(View view) {
        int selection = DataStore.getProgress();
        launchWorkoutActivity(selection);
    }

    /** Starts WorkoutActivity with current workout selection. */
    private void launchWorkoutActivity(int selection) {
        // Store workout name.
        String spinnerText = (String) workoutSpinner.getItemAtPosition(selection);
        DataStore.setWorkoutName(spinnerText);

        // Store workout list selected by user.
        WorkoutList workoutList = DataStore.getWorkoutList();
        workoutList.setWorkoutList(spinnerMap.get(selection));

        // Store the workoutlist in shared prefs in the weird case where users are able
        // to resume the workout activity and hit a null workout list. Not quite sure how this
        // happened, but it did occur in a crash report!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(workoutList);
        prefsEditor.putString("WorkoutListObjectPref", json);
        prefsEditor.commit();

        // Launch the WorkoutActivity
        Intent intent = new Intent(this, WorkoutActivity.class);
        startActivity(intent);
    }

    private void buildWorkoutList(XmlPullParser xpp) throws XmlPullParserException, IOException {
        // Weeks and days will be allocated when encountered in XML file.
        ArrayList<ArrayList<WorkoutEntry>> week = null;
        ArrayList<WorkoutEntry> day = null;
        // Allocate rep list container and reuse for each rep case.
        ArrayList<WorkoutEntry> repList = new ArrayList<>();
        int repCount = 0;
        String name = "";
        // For each XML tag, check if it's a reps tag otherwise just add it to the list.
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                name = xpp.getName();
                if (name.equals("week")) {
                    week = new ArrayList<>();
                } else if (name.equals("day")) {
                    day = new ArrayList<>();
                } else if (name.equals("reps")) {
                    // Save off the rep count.
                    repCount = Integer.parseInt(xpp.getAttributeValue(null, "value"));
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (day == null) {
                    throw new XmlPullParserException("Encountered NULL day");
                }
                // Capitalize the name of the workout action for nicer formatting.
                String nameStr = name.substring(0, 1).toUpperCase() + name.substring(1);
                // If not a rep then add it to the day list, otherwise use rep list.
                List<WorkoutEntry> list = (repCount == 0) ? day : repList;
                list.add(new WorkoutEntry(nameStr, Integer.parseInt(xpp.getText())));
            } else if (eventType == XmlPullParser.END_TAG) {
                name = xpp.getName();
                if (name.equals("week")) {
                    overallList.add(week);
                } else if (name.equals("day")) {
                    if (week == null) {
                        throw new XmlPullParserException("Encountered NULL week");
                    }
                    week.add(day);
                } else if (name.equals("reps")) {
                    if (day == null) {
                        throw new XmlPullParserException("Encountered NULL day");
                    }
                    // For each rep, add the entire rep list to the day.
                    for (int i = 0; i < repCount; i++) {
                        for (WorkoutEntry entry : repList) {
                            day.add(new WorkoutEntry(entry));
                        }
                    }
                    repList.clear();
                    repCount = 0;
                }
            }
            eventType = xpp.next();
        }
    }

    private void setupSpinner() {
        workoutSpinner = (Spinner) findViewById(R.id.workoutSpinner);

        // Build string list for spinner.
        List<String> list = new ArrayList<>();
        String weekStr = getString(R.string.week);
        String dayStr = getString(R.string.day);
        int weekCount = 0;
        for (ArrayList<ArrayList<WorkoutEntry>> week : overallList) {
            weekCount++;
            for (int dayCount = 1; dayCount <= week.size(); dayCount++) {
                list.add(weekStr + " " + weekCount + " " + dayStr + " " + dayCount);
                // Add day to spinner map.
                spinnerMap.add(week.get(dayCount - 1));
            }
        }

        // Setup spinner with string list.
        //currentWorkoutString = list.get(currentWorkoutIndex);
        workoutSpinner.setAdapter(new SpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, // android.R.layout.simple_spinner_item,
                list.toArray(new String[0])));

        // Attach selection listener.
        workoutSpinner.setOnItemSelectedListener(new SpinnerListener());

        // Set current selection based on stored progress value.
        workoutSpinner.setSelection(DataStore.getProgress());
    }

    public class SpinnerAdapter extends ArrayAdapter<String>{

        private int defaultColor = 0;
        private boolean colorInit = false;

        public SpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent){
            View spinnerItem = super.getDropDownView(position, convertView, parent);

            if (!colorInit) {
                defaultColor = spinnerItem.getDrawingCacheBackgroundColor();
            }

            // TODO RPG - come up with better styling, rather than using grey background, probably use crossed out gray text or something...
            if (position == workoutSpinner.getSelectedItemPosition()) {
                spinnerItem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.progressColor));
                TextView tv = (TextView)spinnerItem;
                tv.setTextColor(Color.BLACK);
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            } else {
                spinnerItem.setBackgroundColor(defaultColor);
                TextView tv = (TextView)spinnerItem;
                tv.setTextColor(Color.BLACK);
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            if (position < DataStore.getProgress()) {
                TextView tv = (TextView) spinnerItem;
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.progressBackground));
            }

            return spinnerItem;
        }

    }

    public class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            ArrayList<WorkoutEntry> day = spinnerMap.get(pos);
            listViewData.clear();
            for (WorkoutEntry entry : day) {

                int seconds = entry.getTime_sec();
                String timeStr = String.format("%02d", seconds / 60) + ":" +
                        String.format("%02d", seconds % 60);

                String listStringEntry = timeStr + "   " + entry.getName();
                listViewData.add(listStringEntry);
            }

            listViewAdapter.notifyDataSetChanged();
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }
}

