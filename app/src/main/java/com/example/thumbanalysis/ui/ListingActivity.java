package com.example.thumbanalysis.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.thumbanalysis.utils.CommonObjects;
import com.example.thumbanalysis.R;
import com.example.thumbanalysis.adapters.ScoreListAdapter;


public class ListingActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        recyclerView = findViewById(R.id.recyclerView);

//        ArrayList<ListModel> list = new ArrayList<ListModel>();
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
//        list.add(new ListModel("FileName", "3"));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ScoreListAdapter(CommonObjects.LIST_OF_SCORES));

    }
}