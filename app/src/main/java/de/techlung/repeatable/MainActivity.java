package de.techlung.repeatable;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.techlung.repeatable.generic.BaseActivity;
import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.model.Item;
import de.techlung.repeatable.ui.AbstractExpandableDataProvider;
import de.techlung.repeatable.ui.ExpandableAdapter;


public class MainActivity extends BaseActivity implements RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.fab)
    FloatingActionButton fab;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private List<Category> categoryList = new ArrayList<>();

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private RecyclerViewExpandableItemManager itemManager;

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.instance = this;

        setContentView(R.layout.main_activity);

        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategory();
            }
        });

        categoryList.addAll(DataManager.getAllCategories());

        initList(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadList() {
        categoryList.clear();
        categoryList.addAll(DataManager.getAllCategories());
    }

    private void addCategory() {
        DataManager.createOrEditCategory(this, null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE){
                    provider.addGroupItem(categoryList.size() - 1);
                }
            }
        });
    }

    private void initList(Bundle savedInstanceState) {
        layoutManager = new LinearLayoutManager(this);

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        itemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        itemManager.setOnGroupExpandListener(this);
        itemManager.setOnGroupCollapseListener(this);
        itemManager.setDefaultGroupsExpandedState(false);

        //adapter
        final ExpandableAdapter myItemAdapter = new ExpandableAdapter(this, getDataProvider());

        adapter = itemManager.createWrappedAdapter(myItemAdapter);       // wrap for expanding

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Need to disable them when using animation indicator.
        animator.setSupportsChangeAnimations(false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);  // requires *wrapped* adapter
        recyclerView.setItemAnimator(animator);
        recyclerView.setHasFixedSize(false);

        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(this, R.drawable.list_divider_h), true));

        itemManager.attachRecyclerView(recyclerView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Export Realm Database
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Debugging.exportDatabase(this);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onGroupCollapse(int groupPosition, boolean fromUser) {

    }

    @Override
    public void onGroupExpand(int groupPosition, boolean fromUser) {

    }

    AbstractExpandableDataProvider provider = new AbstractExpandableDataProvider() {

        @Override
        public int getGroupCount() {
            return categoryList.size();
        }

        @Override
        public int getChildCount(int groupPosition) {
            return categoryList.get(groupPosition).getItems().size();
        }

        @Override
        public Category getGroupItem(int groupPosition) {
            return categoryList.get(groupPosition);
        }

        @Override
        public Item getChildItem(int groupPosition, int childPosition) {
            return categoryList.get(groupPosition).getItems().get(childPosition);
        }

        @Override
        public void addChildItem(int groupPosition, int childPosition) {
            itemManager.notifyChildItemInserted(groupPosition, childPosition);
        }

        @Override
        public void addGroupItem(int groupPosition) {
            itemManager.notifyGroupItemInserted(groupPosition);
        }

        @Override
        public boolean isGroupExpanded(int groupPosition) {
            return itemManager.isGroupExpanded(groupPosition);
        }

        @Override
        public void collapseGroup(int groupPosition) {
            itemManager.collapseGroup(groupPosition);
        }

        @Override
        public void expandGroup(int groupPosition) {
            itemManager.expandGroup(groupPosition);
        }

        @Override
        public void removeGroupItem(int groupPosition) {
            itemManager.notifyGroupItemRemoved(groupPosition);
        }

        @Override
        public void removeChildItem(int groupPosition, int childPosition) {
            itemManager.notifyChildItemRemoved(groupPosition, childPosition);
        }
    };

    public AbstractExpandableDataProvider getDataProvider() {
        return provider;
    }

}
