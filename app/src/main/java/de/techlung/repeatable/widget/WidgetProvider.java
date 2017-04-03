package de.techlung.repeatable.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import de.techlung.repeatable.Constants;
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

    public static void updateWidget(Realm realm, Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
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
        Category category = null;
        if (categoryId != WidgetStore.ILLEGAL_CATEGORY_ID && categoryId != WidgetStore.ALL_CATEGORIES) {
            category = DataManager.getCategory(realm, categoryId);
        }

        if (category != null) {
            widget.setTextViewText(R.id.title, category.getName());
            widget.setImageViewResource(R.id.titleBackground, Constants.COLOR_RESOURCE_IDS[category.getColorIndex()]);
        } else {
            widget.setTextViewText(R.id.title, context.getString(R.string.app_name));
            widget.setImageViewResource(R.id.titleBackground, R.color.md_white_1000);
        }


        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
        appWidgetManager.updateAppWidget(appWidgetId, widget);
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
        Realm realm = Realm.getInstance(config);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(realm, context, appWidgetManager, appWidgetId);
        }

        realm.close();

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CHECK_ACTION)) {
            Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
            Realm realm = Realm.getInstance(config);

            int itemId = intent.getIntExtra(WidgetViewsFactory.ITEM_ID, -1);

            DataManager.setItemChecked(realm, itemId, true, context);

            realm.close();

            reloadWidgets(context);
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {

            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            if (ids != null) {
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                this.onUpdate(context, manager, ids);
            }
            //reloadWidgets(context);
        }

        super.onReceive(context, intent);
    }
}
