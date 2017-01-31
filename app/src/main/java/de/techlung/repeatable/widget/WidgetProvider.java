package de.techlung.repeatable.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import de.techlung.repeatable.DataManager;
import de.techlung.repeatable.MainActivity;
import de.techlung.repeatable.R;
import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.preferences.Preferences;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class WidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_IDS_KEY = "WIDGET_IDS_KEY";

    private static final String CHECK_ACTION = "CHECK_ACTION";

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_overview);

        Intent listIntent = new Intent(context, WidgetService.class);
        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));
        widget.setRemoteAdapter(R.id.list, listIntent);

        Intent checkIntent = new Intent(context, WidgetProvider.class);
        checkIntent.setAction(CHECK_ACTION);
        checkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent startActivityPendingIntent = PendingIntent.getBroadcast(context, 0, checkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setPendingIntentTemplate(R.id.list, startActivityPendingIntent);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        widget.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

        int categoryId = WidgetStore.getWidgetCategoryId(appWidgetId);
        if (categoryId != WidgetStore.ILLEGAL_CATEGORY_ID && categoryId != WidgetStore.ALL_CATEGORIES) {
            Category category = DataManager.getCategory(categoryId);
            if (category != null) {
                widget.setTextViewText(R.id.title, category.getName());
            }
        }

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
        appWidgetManager.updateAppWidget(appWidgetId, widget);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
    }

    public static void reloadWidgets(Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(WidgetProvider.WIDGET_IDS_KEY, ids);
        context.sendBroadcast(updateIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Preferences.init(context);

        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(CHECK_ACTION)) {
            //int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            int itemId = intent.getIntExtra(WidgetViewsFactory.ITEM_ID, -1);

            Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
            Realm.setDefaultConfiguration(config);

            DataManager.setItemUnChecked(itemId, true, context);

            reloadWidgets(context);
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            if (ids != null) {
                this.onUpdate(context, manager, ids);
            }
        }

        super.onReceive(context, intent);
    }
}
