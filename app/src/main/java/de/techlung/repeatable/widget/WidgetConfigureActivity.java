package de.techlung.repeatable.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.techlung.repeatable.Constants;
import de.techlung.repeatable.DataManager;
import de.techlung.repeatable.R;
import de.techlung.repeatable.generic.BaseActivity;
import de.techlung.repeatable.model.Category;

public class WidgetConfigureActivity extends BaseActivity {

    @InjectView(R.id.list)
    ListView list;

    int appWidgetId;
    ListAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_overview_configure);
        setResult(RESULT_CANCELED);

        Log.d("WidgetConfigureActivity", "Created");

        ButterKnife.inject(this);

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        Log.d("WidgetConfigureActivity", "Continued");

        final List<Category> categories = DataManager.getAllCategories();

        adapter = new OverviewWidgetConfigureListAdapter(this, R.layout.repeatable_list_category, categories);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    WidgetStore.setWidgetCategoryId(appWidgetId, WidgetStore.ALL_CATEGORIES);
                } else {
                    WidgetStore.setWidgetCategoryId(appWidgetId, categories.get(position - 1).getId());
                }

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetConfigureActivity.this);
                WidgetProvider.updateWidget(WidgetConfigureActivity.this, appWidgetManager, appWidgetId);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    static class ViewHolder {
        @InjectView(R.id.colorIndicator)
        View colorIndicator;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.add)
        RelativeLayout add;
        @InjectView(R.id.edit)
        RelativeLayout edit;
        @InjectView(R.id.open)
        RelativeLayout open;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private class OverviewWidgetConfigureListAdapter extends ArrayAdapter<Category> {

        List<Category> objects;

        OverviewWidgetConfigureListAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
            this.objects = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(WidgetConfigureActivity.this);
                convertView = inflater.inflate(R.layout.repeatable_list_category, parent, false);
            }


            Category category = null;
            if (position != 0) {
                category = objects.get(position - 1);
            }

            ViewHolder holder = new ViewHolder(convertView);

            holder.add.setVisibility(View.GONE);
            holder.edit.setVisibility(View.GONE);
            holder.open.setVisibility(View.GONE);

            if (category == null) {
                holder.text.setText(R.string.widget_config_all_categories);
                holder.colorIndicator.setBackgroundResource(R.color.md_grey_700);
            } else {
                holder.text.setText(category.getName());
                holder.colorIndicator.setBackgroundResource(Constants.COLOR_RESOURCE_IDS[category.getColorIndex()]);
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return objects.size() + 1;
        }
    }
}
