package com.example.ntut.myracingpigeon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

/**
 * Created by Junkai on 2018/6/23.
 */

public class DBHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "PIMS.db";
    //private static final String DATABASE_NAME = "PIMS - ancenstorTest.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public ArrayList<String> getRingList(String ring) {

        SQLiteDatabase db = getReadableDatabase();

        String queryByRing = "select P.Ring||' - '||I.Owner from Pigeon P join PInfo I on P.Ring = I.Ring  where  P.Ring like '%" + ring + "%'";
        Cursor cursor = db.rawQuery(queryByRing, null);
        cursor.moveToFirst();
        ArrayList<String> mArrayList = new ArrayList<String>();

        if(cursor.getCount() == 0) return mArrayList;

        do{
            mArrayList.add(cursor.getString(0));
        }
        while(cursor.moveToNext());

        cursor.close();
        return mArrayList;
    }

    public Cursor getPigeonInfo(String ring){
        SQLiteDatabase db = getReadableDatabase();

        String queryByRing = "select P.Ring, P.Gender, P.Blood, P.Color, I.Owner from Pigeon P join PInfo I on P.Ring = I.Ring where P.Ring = '" + ring + "'";
        Cursor cursor = db.rawQuery(queryByRing, null);
        cursor.moveToFirst();
        return cursor;
    }

    public ArrayList<String> getPigeonAncestors(String ring){
        SQLiteDatabase db = getReadableDatabase();

        String queryByRing =
                "with FamilyChart as(" +
                        "select D.*, 1 as TreeLevel " +
                        "from Dependent as D " +
                            "join Pigeon as P on (D.Child = P.Ring) " +
                        "where P.Ring = '" + ring + "' " +
                        "union all " +
                        "select d.* , TreeLevel +1 " +
                        "from Dependent d " +
                            "join FamilyChart fc on (fc.Mother = d.Child) or (fc.Father = d.Child) " +
                 ")select * from FamilyChart"
                ;
        Cursor cursor = db.rawQuery(queryByRing, null);
        cursor.moveToFirst();
        ArrayList<String> mArrayList = new ArrayList<String>();

        //父母
        String father, mother, grandfather, grandmother, child, extraPost;
        father = cursor.getString(cursor.getColumnIndex("Father"));
        mother = cursor.getString(cursor.getColumnIndex("Mother"));
        father = father == null ? "未登錄" : father;
        mother = mother == null ? "未登錄" : mother;
        mArrayList.add(String.format("父: %s", father));
        mArrayList.add(String.format("母: %s", mother));

        while(cursor.moveToNext()){
            extraPost = "";
            if(cursor.getString(cursor.getColumnIndex("TreeLevel")).equals("2")){
                child = cursor.getString(cursor.getColumnIndex("Child"));
                grandfather = cursor.getString(cursor.getColumnIndex("Father"));
                grandmother = cursor.getString(cursor.getColumnIndex("Mother"));

                if(child.equals(mother))
                    extraPost = "外";

                grandfather = grandfather == null ? "未登錄" : grandfather;
                grandmother = grandmother == null ? "未登錄" : grandmother;
                mArrayList.add(String.format("%s祖父: %s", extraPost,grandfather));
                mArrayList.add(String.format("%s祖母: %s", extraPost, grandmother));
            }

        }


        cursor.close();
        return mArrayList;
    }
}
