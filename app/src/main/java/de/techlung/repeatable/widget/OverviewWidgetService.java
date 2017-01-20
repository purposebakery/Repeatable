package de.techlung.repeatable.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class OverviewWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new OverviewViewsFactory(this.getApplicationContext(),
                intent);
    }
}