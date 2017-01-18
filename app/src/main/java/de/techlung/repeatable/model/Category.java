package de.techlung.repeatable.model;

import android.support.annotation.ColorInt;

import java.util.ArrayList;

import de.techlung.repeatable.R;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject {

    @PrimaryKey
    int id;

    String name;
    int colorIndex;
    RealmList<Item> items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public RealmList<Item> getItems() {
        return items;
    }

    public void setItems(RealmList<Item> items) {
        this.items = items;
    }

    public static int createPrimaryKey() {
        Number lastCategory = Realm.getDefaultInstance().where(Category.class).max("id");
        if (lastCategory == null) {
            return 1;
        } else {
            return lastCategory.intValue() + 1;
        }
    }

}
