package de.techlung.repeatable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import io.realm.Realm;

@SuppressWarnings("ALL")
class Debugging {

    public static void exportDatabase(Context context) {

        // init realm
        Realm realm = Realm.getDefaultInstance();

        File exportRealmFile = null;
        try {
            // get or create an "export.realm" file
            exportRealmFile = new File(context.getExternalCacheDir(), "export.realm");

            // if "export.realm" already exists, delete
            exportRealmFile.delete();

            // copy current realm to "export.realm"
            realm.writeCopyTo(exportRealmFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        realm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportRealmFile));

        // start email intent
        context.startActivity(Intent.createChooser(intent, "Export Realm"));
    }
}
