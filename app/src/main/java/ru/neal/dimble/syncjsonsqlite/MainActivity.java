package ru.neal.dimble.syncjsonsqlite;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements OnClickListener {

    DBHelper dbHelper;
    SQLiteDatabase db;

    TextView tvCompId;
    TextView tvMonId;
    TextView tvFio;
    TextView tvLogin;
    TextView tvHostname;
    TextView tvProcessor;
    TextView tvMemory;
    TextView tvHdd;
    TextView tvOS;

    public void SetDataById(String row_id){

        dbHelper = new DBHelper(this);

        try {
            db = dbHelper.getWritableDatabase();

            String selection = "comp_id = ?";
            String[] selectionArgs = new String[]{row_id};
            Cursor c = db.query("computers", null, selection, selectionArgs, null, null, null);
            //Cursor mCursor = db.rawQuery("SELECT * FROM computers WHERE    yourKey=? AND yourKey1=?", new String[]{keyValue,keyvalue1});

            if (c != null) {
                if (c.moveToFirst()) {
                    Log.d("syncJSONSQLiteDebugMM", "Запись найдена");
                    tvMonId.setText(getString(R.string.mon_id) + c.getString(c.getColumnIndex("mon_id")));
                    tvFio.setText(getString(R.string.fio) + c.getString(c.getColumnIndex("fio")));
                    tvHostname.setText(getString(R.string.hostname) + c.getString(c.getColumnIndex("hostname")));
                    tvLogin.setText(getString(R.string.login) + c.getString(c.getColumnIndex("login")));
                    tvProcessor.setText(getString(R.string.processor) + c.getString(c.getColumnIndex("processor")));
                    tvMemory.setText(getString(R.string.memory) + c.getString(c.getColumnIndex("memory")));
                    tvHdd.setText(getString(R.string.hdd) + c.getString(c.getColumnIndex("hdd")));
                    tvOS.setText(getString(R.string.os) + c.getString(c.getColumnIndex("os")));

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
            int PastleCount = 0;
            if (jsonFromSensor != null) {
                //If you want to delete database
                //this.deleteDatabase("inventoryDB");
                dbHelper = new DBHelper(this);
                db = dbHelper.getWritableDatabase();


                int clearCount = db.delete("computers", null, null);
                Log.d("syncJSONSQLite", "deleted rows count = " + clearCount);


                for (int i = 0; i < jsonFromSensor.length(); i++) {
                    ContentValues cv = new ContentValues();
                    JSONObject row = jsonFromSensor.getJSONObject(i);
                    if(!row.isNull("comp_id") && !row.get("comp_id").equals("")) {
                        cv.put("comp_id", row.getString("comp_id"));

                        //if(!row.isNull("mon_id") && !row.get("mon_id").equals("")){
                        cv.put("mon_id", row.getString("mon_id"));
                        //}
                        cv.put("fio", row.getString("fio"));
                        cv.put("hostname", row.getString("hostname"));
                        cv.put("login", row.getString("login"));
                        cv.put("processor", row.getString("processor"));
                        cv.put("memory", row.getString("memory"));
                        cv.put("hdd", row.getString("hdd"));
                        cv.put("os", row.getString("os"));
                        PastleCount = i;
                        long rowID = db.insert("computers", null, cv);
                        Log.d("syncJSONSQLite", "row inserted, ID = " + rowID);
                    }
                }
                Toast.makeText(this, String.valueOf(PastleCount), Toast.LENGTH_SHORT).show();
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

        Button recogniseButton = (Button)findViewById(R.id.btn_recognise);
        recogniseButton.setOnClickListener(this);

        Button syncButton = (Button)findViewById(R.id.btn_sync);
        syncButton.setOnClickListener(this);

        tvCompId = (TextView) findViewById(R.id.tvCompId);
        tvMonId = (TextView) findViewById(R.id.tvMonId);
        tvFio = (TextView) findViewById(R.id.tvFio);
        tvHostname = (TextView) findViewById(R.id.tvHostname);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvProcessor = (TextView) findViewById(R.id.tvProcessor);
        tvMemory = (TextView) findViewById(R.id.tvMemory);
        tvHdd = (TextView) findViewById(R.id.tvHdd);
        tvOS = (TextView) findViewById(R.id.tvOs);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        String code = data.getStringExtra("code");
        tvCompId.setText(getString(R.string.comp_id) + code);
        if(code != null && !code.isEmpty()) {
            SetDataById(code);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_recognise:
                Intent intent = new Intent(this, Activity_Scan.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_sync:
                SyncSQLiteWithJSON();
                break;
            default:
                break;

        }
    }
}
