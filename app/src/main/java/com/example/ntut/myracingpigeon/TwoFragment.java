package com.example.ntut.myracingpigeon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Junkai on 2018/6/23.
 */

public class TwoFragment extends Fragment {
    private TextView mTextRing, mTextGender, mTextOwner, mTextBlood, mTextColor;
    private String ring;
    private ListView mListAncestor;
    private DBHelper pimsDBHelper;

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pimsDBHelper = new DBHelper(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_two, container, false);
        mTextRing = mView.findViewById(R.id.textRing);
        mTextGender = mView.findViewById(R.id.textGender);
        mTextOwner = mView.findViewById(R.id.textOwner);
        mTextBlood = mView.findViewById(R.id.textBlood);
        mTextColor = mView.findViewById(R.id.textColor);
        mListAncestor = mView.findViewById(R.id.listAncestor);

        mListAncestor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strParent = mListAncestor.getItemAtPosition(position).toString().split(":")[0].trim();
                String parentRing = mListAncestor.getItemAtPosition(position).toString().split(": ")[1].trim();
                //只找父母的其他子女
                if(!(strParent.equals("母") || strParent.equals("父")) || strParent.equals("未登錄")){
                    return;
                }

                Integer intParent = strParent.equals("母") ? 0 : 1;

                //父母腳環未登錄
                if(pimsDBHelper.getRingList(parentRing, "").size() == 0){
                    Toast.makeText(getActivity(), strParent + parentRing + "未登錄", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<String> result = pimsDBHelper.getSibling(intParent, ring);
                //無兄弟姊妹
                if(result.size() == 0){
                    Toast.makeText(getActivity(), strParent + parentRing + "無其他子女", Toast.LENGTH_SHORT).show();
                    return;
                }

                CharSequence[] siblings = result.toArray(new CharSequence[result.size()]);
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(TwoFragment.this.getContext());

                dialog_list.setTitle(mListAncestor.getItemAtPosition(position).toString() + "其他子女");
                dialog_list.setItems(siblings, new DialogInterface.OnClickListener(){
                    @Override
                    //只要你在onClick處理事件內，使用which參數，就可以知道按下陣列裡的哪一個了
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog_list.show();
            }
        });

        ClearText();
        // Inflate the layout for this fragment
        return mView;
    }

    private void ClearText(){
        mTextGender.setText("");
        mTextOwner.setText("");
        mTextBlood.setText("");
        mTextColor.setText("");
        mListAncestor.setAdapter(null);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser){
            //when user select this page
            View view = getView();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void updateDate(String message)
    {
        ClearText();
        ring = message;
        mTextRing.setText(ring);
        Toast.makeText(getActivity(), "選擇腳環" + ring, Toast.LENGTH_SHORT).show();

        Cursor cursor = pimsDBHelper.getPigeonInfo(ring);
        String gender = cursor.getString(cursor.getColumnIndex("Gender")).equals("0") ? "母" : "公";
        String owner = cursor.getString(cursor.getColumnIndex("Owner"));
        String blood = cursor.getString(cursor.getColumnIndex("Blood"));
        String color = cursor.getString(cursor.getColumnIndex("Color"));

        mTextGender.setText(gender);
        mTextOwner.setText(owner);
        mTextBlood.setText(blood);
        mTextColor.setText(color);

        ArrayList<String> ancestorList = pimsDBHelper.getPigeonAncestors(ring);
        mListAncestor.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.my_listview_ancestors, ancestorList));
    }
}
