package ru.neal.dimble.syncjsonsqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by dimbler on 05.06.2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "inventoryDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("syncJsonSQLite", "--- onCreate database ---");
        sqLiteDatabase.execSQL("create table computers ("
                + "id integer primary key autoincrement,"
                + "comp_id text,"
                + "mon_id text,"
                + "fio text,"
                + "hostname text,"
                + "login text,"
                + "processor text,"
                + "memory text,"
                + "hdd text,"
                + "phone text,"
                + "telmac text,"
                + "os text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

}
