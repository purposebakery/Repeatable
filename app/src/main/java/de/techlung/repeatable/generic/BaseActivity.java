package de.techlung.repeatable.generic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.techlung.repeatable.preferences.Preferences;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseActivity extends AppCompatActivity {

    protected String CLASS_TAG = this.getClass().getName();

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(config);

        Preferences.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public Realm getRealm() {
        return realm;
    }
}
