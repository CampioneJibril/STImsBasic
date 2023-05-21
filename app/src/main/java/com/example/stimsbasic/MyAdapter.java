package com.example.stimsbasic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    ArrayList<Model> mList;
    Context context;

    public MyAdapter(Context context, ArrayList<Model> mList) {

        this.mList = mList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Model model = mList.get(position);
        holder.check_in.setText(model.getCheck_in());
        holder.check_out.setText(model.getCheck_out());
        holder.name.setText(model.getName());
        holder.date.setText(model.getDate());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        TextView check_in, check_out, name, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            check_in = itemView.findViewById(R.id.text_view_check_in);
            check_out = itemView.findViewById(R.id.text_view_check_out);
            name = itemView.findViewById(R.id.text_view_name);
            date = itemView.findViewById(R.id.text_view_date);
        }
    }
}
