package cat.bcn.vincles.mobile.Client.Migration;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Fase1SQLiteHelper extends SQLiteOpenHelper {

    public static final String MIGRATION_DB_NAME = "vincles-mobile.db";

    public Fase1SQLiteHelper(Context context) {
        super(context, MIGRATION_DB_NAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String[] getUserPassword(int id){
        String[] res = new String[2];

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query("USER", // a. table
                        new String[]{"USERNAME","CIPHER"}, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor != null) {
            res[0] = cursor.getString(0);
            byte[] cypher = cursor.getBlob(1);
            Security sec = new Security();
            sec.loadPlainAESKey(Security.md5(String.valueOf(id)));
            try {
                res[1] = sec.AESdecrypt(cypher);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cursor.close();

        }

        return res;
    }

}
