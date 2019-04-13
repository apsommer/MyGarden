package com.example.android.mygarden;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

public class PlantWidgetProvider extends AppWidgetProvider {

    // setImageViewResource to update the widget’s image
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int appWidgetId) {

        // TODO (4): separate the updateAppWidget logic into getGardenGridRemoteView and getSinglePlantRemoteView

        // TODO (5): Use getAppWidgetOptions to get widget width and use the appropriate RemoteView method

        // get width of widget
        Bundle widgetParameters = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = widgetParameters.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        RemoteViews remoteViews;

        if (width < 300) remoteViews = getSinglePlantRemoteView(context, imgRes, plantId, showWater);
        else remoteViews = getGardenGridRemoteView(context);

        // this is a framework call with the same name as the current method
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, long plantId, boolean showWater, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWater, appWidgetId);
        }
    }

    private static RemoteViews getSinglePlantRemoteView(Context context, int imgRes, long plantId, boolean showWater) {

        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID) {
            intent = new Intent(context, MainActivity.class);
        } else { // Set on click to open the corresponding detail activity
            Log.d(PlantWidgetProvider.class.getSimpleName(), "plantId=" + plantId);
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);
        // Update image
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        // Update plant ID text
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        // Show/Hide the water drop button
        if (showWater) views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
        else views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        // Add the wateringservice click handler
        Intent wateringIntent = new Intent(context, PlantWateringService.class);
        wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0, wateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);

        return views;
    }

    // TODO (6): Set the PendingIntent template in getGardenGridRemoteView to launch PlantDetailActivity

    private static RemoteViews getGardenGridRemoteView(Context context) {

        // create RemoteViews object from the grid xml
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        // set the GridWidgetService class as the adapter for the remote gridview
        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.widget_grid_view, intent);

        // set the pending intent template which applies to all items in the grid
        // the individual plant IDs are added in a "fill in" intent in GridWidgetService getViewAt()
        Intent itemClickIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent itemClickPendingIntent = PendingIntent.getActivity(
                context, 0, itemClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, itemClickPendingIntent);
        views.setEmptyView(R.id.widget_grid_view, R.id.empty_view);

        return views;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWiddgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Perform any action when an AppWidget for this provider is instantiated
    }

    @Override
    public void onDisabled(Context context) {
        // Perform any action when the last AppWidget instance for this provider is deleted
    }

}
