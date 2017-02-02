package de.techlung.repeatable;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.model.Item;
import io.realm.Realm;
import io.realm.RealmQuery;

public class DataManager {
    private static final String TAG = DataManager.class.getName();

    // Main Activity only
    public static void createOrEditItem(final Context context, @NonNull final Category category, @Nullable Item item, final DialogInterface.OnClickListener listener) {
        Log.d(TAG, "creating Item");
        final Item editItem;
        final boolean isNew;

        final Realm realm = MainActivity.getInstance().getRealm();

        if (item == null) {
            isNew = true;

            realm.beginTransaction();
            editItem = realm.createObject(Item.class, Item.createPrimaryKey(realm));
            editItem.setCategory(category);
            editItem.setCategoryId(category.getId());
            realm.commitTransaction();
        } else {
            isNew = false;
            editItem = item;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.item);

        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_dialog, null, false);
        builder.setView(view);

        final TextInputEditText name = (TextInputEditText) view.findViewById(R.id.name);

        name.setText(editItem.getName());

        if (!isNew) {
            builder.setNeutralButton(R.string.alert_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "item deleted");
                    realm.beginTransaction();
                    category.getItems().remove(editItem);
                    editItem.deleteFromRealm();
                    realm.commitTransaction();

                    listener.onClick(dialog, which);
                    dialog.dismiss();
                }
            });
        }

        builder.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isNew) {
                    Log.d(TAG, "item deleted");
                    realm.beginTransaction();
                    editItem.deleteFromRealm();
                    realm.commitTransaction();
                }

                dialog.dismiss();
            }
        });

        @StringRes int positive = isNew ? R.string.alert_create : R.string.alert_ok;
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "category written");
                realm.beginTransaction();
                editItem.setName(name.getText().toString());
                if (isNew) {
                    category.getItems().add(editItem);
                }
                realm.commitTransaction();

                listener.onClick(dialog, which);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // Main Activity only
    public static void createOrEditCategory(Context context, @Nullable Category category, final DialogInterface.OnClickListener listener) {
        Log.d(TAG, "creating Category");
        final Category editCategory;
        final boolean isNew;

        final Realm realm = MainActivity.getInstance().getRealm();

        if (category == null) {
            isNew = true;

            realm.beginTransaction();
            editCategory = realm.createObject(Category.class, Category.createPrimaryKey(realm));
            realm.commitTransaction();
        } else {
            isNew = false;
            editCategory = category;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.category);

        View view = LayoutInflater.from(context).inflate(R.layout.category_edit_dialog, null, false);
        builder.setView(view);

        final TextInputEditText name = (TextInputEditText) view.findViewById(R.id.name);
        final View colorBackground = view.findViewById(R.id.colorBox);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.colorSeek);

        seekBar.setMax(Constants.COLOR_COUNT - 1);
        seekBar.setProgress(editCategory.getColorIndex());

        colorBackground.setBackgroundResource(Constants.COLOR_RESOURCE_IDS[editCategory.getColorIndex()]);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorBackground.setBackgroundResource(Constants.COLOR_RESOURCE_IDS[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        name.setText(editCategory.getName());

        if (!isNew) {
            builder.setNeutralButton(R.string.alert_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "category deleted");
                    realm.beginTransaction();
                    editCategory.getItems().deleteAllFromRealm();
                    editCategory.deleteFromRealm();
                    realm.commitTransaction();

                    listener.onClick(dialog, which);
                    dialog.dismiss();
                }
            });
        }

        builder.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isNew) {
                    Log.d(TAG, "category deleted");
                    realm.beginTransaction();
                    editCategory.getItems().deleteAllFromRealm();
                    editCategory.deleteFromRealm();
                    realm.commitTransaction();
                }

                dialog.dismiss();
            }
        });

        @StringRes int positive = isNew ? R.string.alert_create : R.string.alert_ok;
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "category written");
                realm.beginTransaction();
                editCategory.setName(name.getText().toString());
                editCategory.setColorIndex(seekBar.getProgress());
                realm.commitTransaction();

                listener.onClick(dialog, which);
                dialog.dismiss();
            }
        });

        builder.show();
    }


    // Main Activity only
    public static void selectAllItemsOfCategory(int categoryId) {
        final Realm realm = MainActivity.getInstance().getRealm();


        boolean noneUnchecked = (realm.where(Item.class).equalTo("categoryId", categoryId).equalTo("isChecked", false).count() == 0);

        realm.beginTransaction();
        for (Item item : realm.where(Item.class).equalTo("categoryId", categoryId).findAll()) {
            item.setChecked(!noneUnchecked);
        }
        realm.commitTransaction();
    }

    // Main Activity only
    public static void saveItemStateOfCategory(int categoryId) {
        final Realm realm = MainActivity.getInstance().getRealm();

        realm.beginTransaction();
        for (Item item :realm.where(Item.class).equalTo("categoryId", categoryId).findAll()) {
            item.setCheckedSaved(item.getChecked());
        }
        realm.commitTransaction();
    }

    // Main Activity only
    public static void loadItemStateOfCategory(int categoryId) {
        final Realm realm = MainActivity.getInstance().getRealm();

        realm.beginTransaction();
        for (Item item :realm.where(Item.class).equalTo("categoryId", categoryId).findAll()) {
            item.setChecked(item.isCheckedSaved());
        }
        realm.commitTransaction();
    }

    public static List<Category> getAllCategories(Realm realm) {
        RealmQuery<Category> query = realm.where(Category.class);
        return query.findAll();
    }

    public static Category getCategory(Realm realm, int id) {
        RealmQuery<Category> query = realm.where(Category.class);
        query.equalTo("id", id);
        return query.findFirst();
    }


    public static long getAllItemsActiveCount(Realm realm) {
        return realm.where(Item.class).equalTo("isChecked", true).count();
    }

    /*
    public static long getAllItemsActiveCount() {
        return getAllItemsActiveCount(Realm.getDefaultInstance());
    }*/

    public static long getCategoryItemsActiveCount(int categoryId, Realm realm) {
        return realm.where(Item.class).equalTo("isChecked", true).equalTo("categoryId", categoryId).count();
    }

    /*
    public static long getCategoryItemsActiveCount(int categoryId) {
        return getCategoryItemsActiveCount(categoryId, Realm.getDefaultInstance());
    }*/

    /*
    public static List<Item> getAllItemsActive() {
        return getAllItemsActive(Realm.getDefaultInstance());
    }*/

    public static List<Item> getAllItemsActive(Realm realm) {
        return realm.where(Item.class).equalTo("isChecked", true).findAllSorted("categoryId");
    }

    /*
    public static List<Item> getCategoryItemsActive(int categoryId) {
        return getCategoryItemsActive(categoryId, Realm.getDefaultInstance());
    }*/

    public static List<Item> getCategoryItemsActive(int categoryId, Realm realm) {
        return realm.where(Item.class).equalTo("isChecked", true).equalTo("categoryId", categoryId).findAll();
    }


    public static void setItemUnChecked(Realm realm, int id, boolean toast, Context context) {
        Log.d(TAG, "setItemUnChecked " + id);
        realm.beginTransaction();
        Item item = realm.where(Item.class).equalTo("id", id).findFirst();
        if (item != null) {
            item.setChecked(false);
            if (toast) {
                Toast.makeText(context, R.string.widget_item_complete, Toast.LENGTH_SHORT).show();
            }
        }
        realm.commitTransaction();
    }

    // Main Activity only
    public static void toggleCheck(Item item) {
        MainActivity.getInstance().getRealm().beginTransaction();
        item.setChecked(!item.getChecked());
        MainActivity.getInstance().getRealm().commitTransaction();

    }
}
