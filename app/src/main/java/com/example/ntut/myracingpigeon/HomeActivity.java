package com.example.ntut.myracingpigeon;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String ACTION_NEW = "ACTION_NEW", ACTION_EDIT = "ACTION_EDIT";
    private EditText mEditSearchRing;
    private Spinner mEditSpinOwner;
    private DBHelper pimsDBHelper;
    private ListView mResult;

    @Override
    public void onResume(){
        super.onResume();
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pimsDBHelper = new DBHelper(this);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("我真的沒有要做壞事, 給我權限吧?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            } else {

                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }

        //set view to object
        mEditSearchRing = findViewById(R.id.editSearchRing);
        mEditSpinOwner = findViewById(R.id.editSpinOwner);
        mResult = findViewById(R.id.listPigeons);

        //set events
        mEditSearchRing.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                doSearchRing();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        mResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String ring = mResult.getItemAtPosition(position).toString().split(" - ")[0].trim();
                Toast.makeText(HomeActivity.this, ring, Toast.LENGTH_LONG).show();

                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("腳環號碼 " + ring)
                        .setMessage("選擇動作")
                        .setPositiveButton("編輯", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(HomeActivity.this, EditActivity.class);
                                intent.putExtra("action", ACTION_EDIT);
                                intent.putExtra("ring", ring);
                                HomeActivity.this.startActivity(intent);
                            }
                        })
                        .setNeutralButton("刪除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pimsDBHelper.deletePigeon(ring);
                                init();
                            }
                        })
                        .show();
            }
        });

        mEditSpinOwner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                doSearchRing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, EditActivity.class);
                intent.putExtra("action", ACTION_NEW);
                HomeActivity.this.startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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
            Toast.makeText(this, "Update", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Toast.makeText(this, "功能尚未開放", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_export) {
            backupDB();
        } else if (id == R.id.nav_import) {
            Toast.makeText(this, "功能尚未開放", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_fix_cascade) {
            pimsDBHelper.fixCascadeData();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void init(){
        //set owner list
        ArrayList<String> result = pimsDBHelper.getOwnerList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeActivity.this, R.layout.spinner_owner_list, result);
        //設置下拉列表的風格
        adapter.setDropDownViewResource(R.layout.spinner_owner_list);
        mEditSpinOwner.setAdapter(adapter);
        mEditSearchRing.setText("");
        doSearchRing();
    }

    private void doSearchRing(){
        String ring = mEditSearchRing.getText().toString();
        String owner = mEditSpinOwner.getSelectedItem().toString();
        if(ring.length() >= 0){
            ArrayList<String> result = pimsDBHelper.getRingList(ring, owner);
            mResult.setAdapter(new ArrayAdapter<String>(HomeActivity.this, R.layout.my_listview, result));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void backupDB(){
        // TODO Auto-generated method stub

        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                Calendar currentTime = Calendar.getInstance();
                String backupDBPath = "/PigeonBackupDB/";
                String fileName = String.format("backupDB-%s-%s-%s_%s-%s.db",
                        currentTime.get(Calendar.YEAR),
                        currentTime.get(Calendar.MONTH)+1,
                        currentTime.get(Calendar.DAY_OF_MONTH),
                        currentTime.get(Calendar.HOUR),
                        currentTime.get(Calendar.MINUTE));
                File currentDB = new File(pimsDBHelper.getReadableDatabase().getPath());
                File backupDB = new File(sd, backupDBPath + fileName);
                File dir = new File(sd, backupDBPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(),
                        "/PigeonBackupDB/" + fileName + "匯出成功", Toast.LENGTH_LONG)
                        .show();

            }
        } catch (Exception e)
        {

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }
}
