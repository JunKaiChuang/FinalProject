package com.example.ntut.myracingpigeon;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {
    private static final String ACTION_NEW = "ACTION_NEW", ACTION_EDIT = "ACTION_EDIT";
    private DBHelper pimsDBHelper;
    private Spinner mSpinSex;
    private String action, ring;
    private TextView mTextTile;
    private Button mBtnSave;
    private EditText mEditRing, mEditBlood, mEditColor, mEditOwner, mEditFather, mEditMother;
    private Spinner mEditGender;

    private Pigeon mPigeon;
    private PInfo mPInfo;
    private Dependent mDependent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mSpinSex = findViewById(R.id.spinGender);
        mTextTile = findViewById(R.id.textTitle);
        mBtnSave = findViewById(R.id.btnSave);

        //Input group
        mEditRing = findViewById(R.id.editRing);
        mEditGender = findViewById(R.id.spinGender);
        mEditBlood = findViewById(R.id.editBlood);
        mEditColor = findViewById(R.id.editColor);
        mEditOwner = findViewById(R.id.editOwner);
        mEditFather = findViewById(R.id.editFather);
        mEditMother = findViewById(R.id.editMother);

        String[] genderList = getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,genderList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinSex.setAdapter(adapter);

        mBtnSave.setOnClickListener(btnSaveListener);

        //init
        pimsDBHelper = new DBHelper(this);
        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        switch (action){
            case ACTION_NEW:
                mTextTile.setText("新增賽鴿資料");
                break;
            case ACTION_EDIT:
                mTextTile.setText("編輯賽鴿資料");
                ring = intent.getStringExtra("ring");
                genEditMode();
                break;
        }
    }

    private Button.OnClickListener btnSaveListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            prepareData();

            switch (action){
                case ACTION_NEW:
                    if(pimsDBHelper.createPigeon(mPigeon, mPInfo, mDependent)){
                        Toast.makeText(EditActivity.this, mPigeon.Ring + "已新增", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    break;
                case ACTION_EDIT:
                    if(pimsDBHelper.editPigeon(mPigeon, mPInfo, mDependent)){
                        Toast.makeText(EditActivity.this, mPigeon.Ring + "已修改", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    break;
            }


        }
    };

    private void genEditMode(){
        Cursor cursor = pimsDBHelper.getPigeonEditData(ring);
        mEditRing.setText(cursor.getString(cursor.getColumnIndex("Ring")));
        mEditGender.setSelection(Integer.parseInt(cursor.getString(cursor.getColumnIndex("Gender"))));
        mEditBlood.setText(cursor.getString(cursor.getColumnIndex("Blood")));
        mEditColor.setText(cursor.getString(cursor.getColumnIndex("Color")));

        String owner, father, mother;
        owner = cursor.getString(cursor.getColumnIndex("Owner"));
        father = cursor.getString(cursor.getColumnIndex("Father"));
        mother = cursor.getString(cursor.getColumnIndex("Mother"));

        owner = owner == null ? "" : owner;
        father = father == null ? "" : father;
        mother = mother == null ? "" : mother;

        mEditOwner.setText(owner);
        mEditFather.setText(father);
        mEditMother.setText(mother);

        mEditRing.setFocusable(false);
    }

    private void prepareData(){

        mPigeon = new Pigeon();
        mPInfo = new PInfo();
        mDependent = new Dependent();

        if(!checkInput()){
            Toast.makeText(this, "資料未輸入完全", Toast.LENGTH_LONG).show();
            return;
        }

        mPigeon.Ring = mEditRing.getText().toString();
        mPigeon.Blood = mEditBlood.getText().toString();
        mPigeon.Color = mEditColor.getText().toString();
        mPigeon.Gender = mEditGender.getSelectedItemPosition();

        mPInfo.Ring = mPigeon.Ring;
        mPInfo.Owner = mEditOwner.getText().toString();

        mDependent.Child = mPigeon.Ring;
        mDependent.Father = mEditFather.getText().toString().equals("") ? null : mEditFather.getText().toString();
        mDependent.Mother = mEditMother.getText().toString().equals("") ? null : mEditMother.getText().toString();
    }

    private Boolean checkInput(){
        Boolean result = true;

        result &= mEditRing.getText().toString().equals("") ? false : true;
        result &= mEditBlood.getText().toString().equals("") ? false : true;
        result &= mEditColor.getText().toString().equals("") ? false : true;

        return result;
    }
}
