package de.techlung.repeatable.widget;

import com.pixplicity.easyprefs.library.Prefs;

class WidgetStore {
    static final int ILLEGAL_CATEGORY_ID = -1;
    static final int ALL_CATEGORIES = -2;

    private static final String WIDGET_SETTINGS_CATEGORY_ID = "WIDGET_SETTINGS_CATEGORY_ID";

    static int getWidgetCategoryId(int appWidgetId) {
        return Prefs.getInt(getWidgetCategoryIdKey(appWidgetId), ILLEGAL_CATEGORY_ID);
    }

    static void setWidgetCategoryId(int appWidgetId, int categoryId) {
        Prefs.putInt(getWidgetCategoryIdKey(appWidgetId), categoryId);
    }

    private static String getWidgetCategoryIdKey(int appWidgetId) {
        return WIDGET_SETTINGS_CATEGORY_ID + appWidgetId;
    }
}
