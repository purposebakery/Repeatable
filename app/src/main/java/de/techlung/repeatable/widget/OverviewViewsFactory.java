package de.techlung.repeatable.widget;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import de.techlung.repeatable.Constants;
import de.techlung.repeatable.DataManager;
import de.techlung.repeatable.R;
import de.techlung.repeatable.model.Item;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class OverviewViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = OverviewViewsFactory.class.getName();

    private Context context = null;

    public OverviewViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        Realm.getDefaultInstance().close();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount ");
        return (int) DataManager.getAllItemsActiveCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG, "getViewAt " + position);

        Item item = DataManager.getAllItemsActive().get(position);

        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_overview_list_item);

        row.setImageViewResource(R.id.colorIndicator, Constants.COLOR_RESOURCE_IDS[item.getCategory().getColorIndex()]);
        row.setTextViewText(R.id.name, item.getName());

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.widget_overview_loading);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return DataManager.getAllItemsActive().get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }

    @Override
    public void onDataSetChanged() {
        // no-op
    }
}
