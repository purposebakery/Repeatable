package de.techlung.repeatable;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.techlung.repeatable.backup.BackupManager;
import de.techlung.repeatable.generic.BaseActivity;
import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.model.Item;
import de.techlung.repeatable.ui.AbstractExpandableDataProvider;
import de.techlung.repeatable.ui.ExpandableAdapter;

public class MainActivity extends BaseActivity implements RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener {

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.fab)
    FloatingActionButton fab;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private List<Category> categoryList = new ArrayList<>();
    private RecyclerView.Adapter adapter;
    private RecyclerViewExpandableItemManager itemManager;

    private BackupManager backupManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ButterKnife.inject(this);

        loadList();
        initList();

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategory();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backupManager != null) {
            backupManager.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initBackupManager() {
        if (backupManager == null) {
            backupManager = new BackupManager(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem login = menu.findItem(R.id.login);
        MenuItem loadBackup = menu.findItem(R.id.loadBackup);
        MenuItem createBackup = menu.findItem(R.id.createBackup);

        if (backupManager != null && backupManager.getGoogleApiClient() != null && backupManager.getGoogleApiClient().isConnected()) {
            login.setVisible(false);
            loadBackup.setVisible(true);
            createBackup.setVisible(true);
        } else {
            login.setVisible(true);
            loadBackup.setVisible(false);
            createBackup.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.login) {
            initBackupManager();
        } else if (id == R.id.loadBackup) {
            backupManager.openFilePicker();
            return true;
        } else if (id == R.id.createBackup) {
            backupManager.openFolderPicker();
            return true;
        } else if (id == R.id.about) {
            showAbout();
            return true;
        } else if (id == R.id.help) {
            showHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (backupManager != null) {
            backupManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void loadList() {
        categoryList.clear();
        categoryList.addAll(DataManager.getAllCategories(getRealm()));
    }

    private void addCategory() {
        DataManager.createOrEditCategory(this, null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    provider.addGroupItem(categoryList.size());
                }
            }
        });
    }

    private void initList() {
        itemManager = new RecyclerViewExpandableItemManager(null);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    public AbstractExpandableDataProvider getDataProvider() {
        return provider;
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.about_title);

        String aboutFirst = getString(R.string.about_messagae);
        String aboutSecond = getString(R.string.about_icon_credit);

        builder.setMessage(aboutFirst + aboutSecond);

        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.help_title);

        builder.setMessage(R.string.help_messagae);

        builder.setPositiveButton(R.string.alert_ok, null);
        builder.show();
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
            loadList();
            itemManager.notifyChildItemInserted(groupPosition, childPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void addGroupItem(int groupPosition) {
            loadList();
            itemManager.notifyGroupItemInserted(groupPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public boolean isGroupExpanded(int groupPosition) {
            loadList();
            return itemManager.isGroupExpanded(groupPosition);

        }

        @Override
        public void collapseGroup(int groupPosition) {
            loadList();
            itemManager.collapseGroup(groupPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void expandGroup(int groupPosition) {
            loadList();
            itemManager.expandGroup(groupPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void editedGroup(int groupPosition) {
            loadList();
            itemManager.notifyGroupItemChanged(groupPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void editedChild(int groupPosition, int childPosition) {
            loadList();
            itemManager.notifyChildItemChanged(groupPosition, childPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void removeGroup(int groupPosition) {
            loadList();
            itemManager.notifyGroupItemRemoved(groupPosition);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void removeChildItem(int groupPosition, int childPosition) {
            loadList();
            itemManager.notifyChildItemRemoved(groupPosition, childPosition);
            adapter.notifyDataSetChanged();
        }
    };

}
