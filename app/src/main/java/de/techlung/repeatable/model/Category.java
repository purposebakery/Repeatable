package de.techlung.repeatable.model;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject {

    @PrimaryKey
    private int id;

    private String name;
    private int colorIndex;
    private RealmList<Item> items;

    public static int createPrimaryKey(Realm realm) {
        Number lastCategory = realm.where(Category.class).max("id");
        if (lastCategory == null) {
            return 1000;
        } else {
            return lastCategory.intValue() + 1;
        }
    }

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

}
