package com.example.android.mygarden;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

// TODO (2): Create a plant watering service that extends IntentService and supports the action
// ACTION_WATER_PLANTS which updates last_watered timestamp for all plants still alive -->

public class PlantWateringService extends IntentService {

    public PlantWateringService() {
        super("PlantWateringService");
    }

    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";

    public static void startActionWaterPlants(Context context) {

        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {

            final String action = intent.getAction();
            if (action.equals(ACTION_WATER_PLANTS)) handleActionWaterPlants();
        }
    }

    private void handleActionWaterPlants() {

        // Plants table URI
        Uri PLANTS_URI = PlantContract.BASE_CONTENT_URI
                .buildUpon().appendPath(PlantContract.PATH_PLANTS).build();

        // prepare new db row
        ContentValues contentValues = new ContentValues();

        // get current time from system util
        long timeNow = System.currentTimeMillis();

        // create a tuple with (column name, time now)
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        // WHERE and SELECT clause are used to compare against a "max time without water"
        getContentResolver().update(PLANTS_URI, contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + "> ?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});

    }
}
