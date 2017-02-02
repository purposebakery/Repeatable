package de.techlung.repeatable.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

import de.techlung.repeatable.preferences.Preferences;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetViewsFactory(this.getApplicationContext(), intent);
    }
}