package ru.neal.dimble.syncjsonsqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements OnClickListener {

    DBHelper dbHelper;
    SQLiteDatabase db;

    public void SetDataById(int row_id){

        dbHelper = new DBHelper(this);

        try {
            db = dbHelper.getWritableDatabase();

            String selection = "comp_id = ?";
            String[] selectionArgs = new String[]{String.valueOf(row_id)};
            Cursor c = db.query("computers", null, selection, selectionArgs, null, null, null);
            //Cursor mCursor = db.rawQuery("SELECT * FROM computers WHERE    yourKey=? AND yourKey1=?", new String[]{keyValue,keyvalue1});

            if (c != null) {
                if (c.moveToFirst()) {
                    Log.d("syncJSONSQLiteDebugMM", "Запись найдена "+ c.getString(3));

                } else {
                    Log.d("syncJSONSQLiteDebugMM", "Запись не найдена");
                    Toast.makeText(this, R.string.identity_not_found, Toast.LENGTH_SHORT).show();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            dbHelper.close();
        }

    }


    public void SyncSQLiteWithJSON() {

        try {
            JSONArray jsonFromSensor = new GetDatabaseData(this).execute().get();

            if (jsonFromSensor != null) {
                //If you want to delete database
                //this.deleteDatabase("inventoryDB");
                dbHelper = new DBHelper(this);
                db = dbHelper.getWritableDatabase();

                int clearCount = db.delete("computers", null, null);
                Log.d("syncJSONSQLite", "deleted rows count = " + clearCount);
                long rowID = 0;

                for (int i = 0; i < jsonFromSensor.length(); i++) {
                    ContentValues cv = new ContentValues();
                    JSONObject row = jsonFromSensor.getJSONObject(i);
                    cv.put("comp_id", row.getInt("comp_id"));
                    cv.put("mon_id", row.getInt("mon_id"));
                    cv.put("fio", row.getString("fio"));
                    cv.put("hostname", row.getString("hostname"));
                    cv.put("login", row.getString("login"));
                    cv.put("processor", row.getString("processor"));
                    cv.put("memory", row.getString("memory"));
                    cv.put("hdd", row.getString("hdd"));
                    cv.put("os", row.getString("os"));
                    rowID = db.insert("computers", null, cv);
                    Log.d("syncJSONSQLite", "row inserted, ID = " + rowID);
                }
                Toast.makeText(this, R.string.str_added + Long.toString(rowID) + R.string.str_rows, Toast.LENGTH_SHORT).show();
                dbHelper.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int data_id = 1303162;
        SetDataById(data_id);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

    }
}
