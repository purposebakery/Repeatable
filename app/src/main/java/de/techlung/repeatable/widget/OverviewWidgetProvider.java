package de.techlung.repeatable.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import de.techlung.repeatable.MainActivity;
import de.techlung.repeatable.R;

public class OverviewWidgetProvider extends AppWidgetProvider {
    public static String EXTRA_WORD = "com.commonsware.android.appwidget.lorem.WORD";

    int randomNumber = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            Intent listIntent = new Intent(context, OverviewWidgetService.class);

            listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            listIntent.putExtra("random", randomNumber);
            randomNumber++;

            listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(),
                    R.layout.widget_overview);

            widget.setRemoteAdapter(R.id.list,
                    listIntent);

            /*
            Intent clickIntent = new Intent(context, MainActivity.class);
            PendingIntent clickPendingIntent = PendingIntent
                    .getActivity(context, 0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            widget.setPendingIntentTemplate(R.id.title, clickPendingIntent);
*/


            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            widget.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);


            appWidgetManager.updateAppWidget(appWidgetId, widget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
