package com.example.calendarquickstart;

/**
 * Created by MJ on 9/17/15.
 */
import android.database.Cursor;
import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mActivity;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (Exception e) {
            mActivity.updateStatus("The following error occurred:\n" +
                    e.getMessage());
        }
        if (mActivity.mProgress.isShowing()) {
            mActivity.mProgress.dismiss();
        }
        return null;
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {



        String [] tableColumns = new String[2];
        tableColumns[0] = bandProvider.Band_Data.TIMESTAMP;
        tableColumns[1] = bandProvider.Band_Data._Value;


        Cursor band_data = mActivity.band_data;
        band_data.moveToFirst();
        java.util.Date time=new java.util.Date((long)band_data.getDouble(0));
        java.util.Date time2=new java.util.Date((long)band_data.getDouble(0)+(60*60*60*20));

        DateTime abcde = new DateTime(time);
        DateTime two = new DateTime(time2);
        EventDateTime example = new EventDateTime();
        example.setDateTime(abcde);
        EventDateTime example1 = new EventDateTime();
        example1.setDateTime(two);

        Event event = new Event()
                .setSummary(band_data.getString(1))
                .setDescription("skin temperature");

        //DateTime startDateTime = new DateTime("2015-09-23T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(abcde)
                .setTimeZone("America/New_York");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2015-09-23T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(two)
                .setTimeZone("America/New_York");
        event.setEnd(end);


        String calendarId = "primary";
        event = mActivity.mService.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());








        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<String>();
        Events events = mActivity.mService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event1 : items) {
            DateTime start1 = event1.getStart().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start1 = event1.getStart().getDate();
            }
            eventStrings.add(
                    String.format("%s (%s)", event1.getSummary(), start1));
        }
        return eventStrings;
    }

}