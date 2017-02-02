package de.techlung.repeatable.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Item extends RealmObject {

    @PrimaryKey
    int id;

    String name;
    boolean isChecked;
    boolean isCheckedSaved;
    int categoryId;
    Category category;

    public static int createPrimaryKey(Realm realm) {
        Number lastItem = realm.where(Item.class).max("id");
        if (lastItem == null) {
            return 3000;
        } else {
            return lastItem.intValue() + 1;
        }
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


    public boolean isCheckedSaved() {
        return isCheckedSaved;
    }

    public void setCheckedSaved(boolean checkedSaved) {
        isCheckedSaved = checkedSaved;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
