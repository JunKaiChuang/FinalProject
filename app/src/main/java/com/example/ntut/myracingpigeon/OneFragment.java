package com.example.ntut.myracingpigeon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Junkai on 2018/6/23.
 */

public class OneFragment extends Fragment {
    SendMessage SM;
    private EditText mSeacrhRing;
    private Spinner mSpinOwner;
    private ListView mResult;
    private DBHelper pimsDBHelper;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume(){
        super.onResume();
        init();
    }

    private void init(){
        //set owner list
        ArrayList<String> result = pimsDBHelper.getOwnerList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_owner_list, result);
        //設置下拉列表的風格
        adapter.setDropDownViewResource(R.layout.spinner_owner_list);
        mSpinOwner.setAdapter(adapter);
    }

    private void doSearchRing(){
        String ring = mSeacrhRing.getText().toString();
        String owner = mSpinOwner.getSelectedItem().toString();
        if(ring.length() >= 0){
            ArrayList<String> result = pimsDBHelper.getRingList(ring, owner);
            mResult.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.my_listview, result));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pimsDBHelper = new DBHelper(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_one, container, false);
        mSeacrhRing = mView.findViewById(R.id.searchRing);
        mResult = mView.findViewById(R.id.searchResult);
        mSpinOwner = mView.findViewById(R.id.spinOwner);

        mSeacrhRing.addTextChangedListener(new TextWatcher() {

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
                String ring = mResult.getItemAtPosition(position).toString().split(" - ")[0].trim();

                Intent intent = new Intent();
                intent.putExtra("ring", ring);

                SM.sendData(ring);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                TabLayout tabhost = (TabLayout) getActivity().findViewById(R.id.tabs);
                tabhost.getTabAt(1).select();
            }
        });

        mSpinOwner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        init();
        // Inflate the layout for this fragment
        return mView;
    }

    interface SendMessage {
        void sendData(String message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            SM = (SendMessage) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }
}
