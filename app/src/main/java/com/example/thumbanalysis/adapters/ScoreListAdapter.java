package com.example.thumbanalysis.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thumbanalysis.models.ListModel;
import com.example.thumbanalysis.R;

import java.util.ArrayList;

public class ScoreListAdapter extends RecyclerView.Adapter<ScoreListAdapter.ViewHolder> {

    private ArrayList<ListModel> listModelArrayList;

    public ScoreListAdapter(ArrayList<ListModel> listModelArrayList) {
        this.listModelArrayList = listModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.score_list_item, parent, false);
        ScoreListAdapter.ViewHolder viewHolder = new ScoreListAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ListModel listModel = listModelArrayList.get(position);
        String tempScore = listModel.getScore();
        if(tempScore.equals("Not Found")){
            holder.socre.setTextColor(Color.parseColor("#FF0000"));
        }else if(Integer.parseInt(tempScore) >0){
            holder.socre.setTextColor(Color.parseColor("#00FF00"));

        }
        else{
            holder.socre.setTextColor(Color.parseColor("#000000"));
        }
        holder.socre.setText(String.valueOf(listModel.getScore())); ;
        holder.fileName.setText(listModel.getFileName()); ;


    }

    @Override
    public int getItemCount() {
        return listModelArrayList.size();
    }


    public  static  class  ViewHolder extends RecyclerView.ViewHolder{
        public TextView fileName, socre;
        public ViewHolder( View itemView) {
            super(itemView);
            this.socre = itemView.findViewById(R.id.scoreTextView);
            this.fileName = itemView.findViewById(R.id.fileNameTextView);
        }
    }
}
