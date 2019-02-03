package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

//  TODO (2): Create a RemoteViewsService class and a RemoteViewsFactory class with:
//        - onDataSetChanged querying the list of all plants in the database
//        - getViewAt creating a RemoteView using the plant_widget layout
//        - getViewAt setting the fillInIntent for widget_plant_image with the plant ID as extras -->

public class GridWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }

}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    Cursor mCursor;

    public GridRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        // get plants table address
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();

        // reset the Cursor if necessary
        if (mCursor != null) mCursor.close();

        // get all plants ordered by time created
        mCursor = mContext.getContentResolver().query(PLANT_URI,
                null, null, null, PlantContract.PlantEntry.COLUMN_CREATION_TIME);

    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /** This method acts like the onBindViewHolder method in an Adapter */
    @Override
    public RemoteViews getViewAt(int position) {

        // check that there are some plants to work with
        if (mCursor == null || mCursor.getCount() == 0) return null;

        // move to position
        mCursor.moveToPosition(position);

        // get the column index numbers
        int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int createTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        // get the plant data using the column index numbers
        long plantId = mCursor.getLong(idIndex);
        int plantType = mCursor.getInt(plantTypeIndex);
        long createdAt = mCursor.getLong(createTimeIndex);
        long wateredAt = mCursor.getLong(waterTimeIndex);

        // get the current time from the system utils
        long timeNow = System.currentTimeMillis();

        // the whole widget layout is the RemoteViews
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        // get image based on plant ages
        int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt, timeNow - wateredAt, plantType);

        // bind data to the views in the layout
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

        // hide water drop in the gridview mode
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        // create a "fill in" intent with the plantID bundled in
        Bundle extras = new Bundle();
        extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // means all views in the gridview are of the same type
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

