package de.techlung.repeatable.widget;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import de.techlung.repeatable.Constants;
import de.techlung.repeatable.DataManager;
import de.techlung.repeatable.R;
import de.techlung.repeatable.model.Item;
import de.techlung.repeatable.preferences.Preferences;
import io.realm.Realm;
import io.realm.RealmConfiguration;

class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final String ITEM_ID = "ITEM_ID";
    private static final String TAG = WidgetViewsFactory.class.getName();

    private Context context;
    private Intent intent;

    private int categoryId = WidgetStore.ILLEGAL_CATEGORY_ID;

    private WidgetType type;

    WidgetViewsFactory(Context context, Intent intent) {
        Preferences.init(context);

        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        categoryId = WidgetStore.getWidgetCategoryId(appWidgetId);

        if (categoryId != WidgetStore.ILLEGAL_CATEGORY_ID && categoryId != WidgetStore.ALL_CATEGORIES) {
            type = WidgetType.CATEGORY;
        } else {
            type = WidgetType.ALL;
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public int getCount() {


        Realm.init(context);
        Realm.removeDefaultConfiguration();
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm instance = Realm.getInstance(config);


        int count;
        if (type == WidgetType.ALL) {
            count = (int) DataManager.getAllItemsActiveCount(instance);
        } else {
            count = (int) DataManager.getCategoryItemsActiveCount(categoryId, instance);
        }

        instance.close();

        Log.d(TAG, "getCount: " + count + " for category " + categoryId + " in type " + type.name());

        return count;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG, "getViewAt " + position);

        Realm.init(context);
        Realm.removeDefaultConfiguration();
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm instance = Realm.getInstance(config);

        Item item;

        if (type == WidgetType.ALL) {
            item = DataManager.getAllItemsActive(instance).get(position);
        } else {
            item = DataManager.getCategoryItemsActive(categoryId, instance).get(position);
        }

        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_overview_list_item);

        row.setImageViewResource(R.id.colorIndicator, Constants.COLOR_RESOURCE_IDS[item.getCategory().getColorIndex()]);
        row.setTextViewText(R.id.name, item.getName());

        Bundle extras = new Bundle();
        extras.putInt(ITEM_ID, item.getId());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.widgetOverViewRowRoot, fillInIntent);

        instance.close();

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
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onDataSetChanged() {
        // no-op
        Log.d("WidgetViewsFactory", "onDataSetChanged");
    }
}
