package de.techlung.repeatable;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import java.util.List;

import de.techlung.repeatable.model.Category;
import de.techlung.repeatable.model.Item;
import io.realm.Realm;
import io.realm.RealmQuery;

public class DataManager {
    private static final String TAG = DataManager.class.getName();

    public static void createOrEditItem(Context context, @NonNull final Category category, @Nullable Item item, final DialogInterface.OnClickListener listener) {
        Log.d(TAG, "creating Item");
        final Item editItem;
        final boolean isNew;

        if (item == null) {
            isNew = true;

            Realm.getDefaultInstance().beginTransaction();
            editItem = Realm.getDefaultInstance().createObject(Item.class, Item.createPrimaryKey());
            editItem.setCategory(category);
            editItem.setCategoryId(category.getId());
            Realm.getDefaultInstance().commitTransaction();
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
                    Realm.getDefaultInstance().beginTransaction();
                    category.getItems().remove(editItem);
                    editItem.deleteFromRealm();
                    Realm.getDefaultInstance().commitTransaction();

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
                    Realm.getDefaultInstance().beginTransaction();
                    editItem.deleteFromRealm();
                    Realm.getDefaultInstance().commitTransaction();
                }

                dialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "category written");
                Realm.getDefaultInstance().beginTransaction();
                editItem.setName(name.getText().toString());
                if (isNew) {
                    category.getItems().add(editItem);
                }
                Realm.getDefaultInstance().commitTransaction();

                listener.onClick(dialog, which);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public static void createOrEditCategory(Context context, @Nullable Category category, final DialogInterface.OnClickListener listener) {
        Log.d(TAG, "creating Category");
        final Category editCategory;
        final boolean isNew;

        if (category == null) {
            isNew = true;

            Realm.getDefaultInstance().beginTransaction();
            editCategory = Realm.getDefaultInstance().createObject(Category.class, Category.createPrimaryKey());
            Realm.getDefaultInstance().commitTransaction();
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
                    Realm.getDefaultInstance().beginTransaction();
                    for (Item item : editCategory.getItems()) {
                        item.deleteFromRealm();
                    }
                    editCategory.deleteFromRealm();
                    Realm.getDefaultInstance().commitTransaction();

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
                    Realm.getDefaultInstance().beginTransaction();
                    for (Item item : editCategory.getItems()) {
                        item.deleteFromRealm();
                    }
                    editCategory.deleteFromRealm();
                    Realm.getDefaultInstance().commitTransaction();
                }

                dialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "category written");
                Realm.getDefaultInstance().beginTransaction();
                editCategory.setName(name.getText().toString());
                editCategory.setColorIndex(seekBar.getProgress());
                Realm.getDefaultInstance().commitTransaction();

                listener.onClick(dialog, which);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public static List<Category> getAllCategories() {
        RealmQuery<Category> query = Realm.getDefaultInstance().where(Category.class);
        return query.findAll();
    }

    public static long getAllItemsActiveCount() {
        return Realm.getDefaultInstance().where(Item.class).equalTo("isChecked", true).count();
    }

    public static List<Item> getAllItemsActive() {
        return Realm.getDefaultInstance().where(Item.class).equalTo("isChecked", true).findAllSorted("categoryId");
    }

    public static void toggleCheck(Item item) {
        Realm.getDefaultInstance().beginTransaction();
        item.setChecked(!item.getChecked());
        Realm.getDefaultInstance().commitTransaction();
    }
}
