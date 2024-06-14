package com.example.nss.senior.attend;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nss.R;
import com.example.nss.model.Attendance;
import com.example.nss.senior.SrAttendActivity;

import java.util.ArrayList;

public class SrAttendAdapter extends RecyclerView.Adapter<SrAttendAdapter.MyViewHolder> {

    Context context;
    ArrayList<Attendance> list;
    static SrAttendAdapter.OnitemClickListener listener;

    public interface OnitemClickListener{
        void PDFClick(int position, String Edate, String Ename, String count);
    }
    public static void setOnClickListener(SrAttendAdapter.OnitemClickListener clicklistener){
        listener = clicklistener;
    }
    public SrAttendAdapter(Context context, ArrayList<Attendance> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SrAttendAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_sr_attend,parent,false);

        return new MyViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SrAttendAdapter.MyViewHolder holder, int position) {
        Attendance attendance =list.get(position);
        holder.recattEN.setText(attendance.getEventName());
        holder.recattED.setText(attendance.getEventDate());
        holder.recattPRE.setText(attendance.getCount());

        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(context, SrAttendActivity.class);
            intent.putExtra("EventName",attendance.getEventName());
            intent.putExtra("EventDate",attendance.getEventDate());
            intent.putExtra("fr_att","true");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView recattEN,recattED,recattPRE;
        ImageButton pdfattbtn;

        public MyViewHolder(@NonNull View itemView, OnitemClickListener listener) {
            super(itemView);

            recattEN=itemView.findViewById(R.id.recattEN);
            recattED=itemView.findViewById(R.id.recattED);
            recattPRE=itemView.findViewById(R.id.recattPRE);

            pdfattbtn = itemView.findViewById(R.id.pdfattbtn);

            pdfattbtn.setOnClickListener(v -> listener.PDFClick(getAdapterPosition(),recattED.getText().toString().trim(),recattEN.getText().toString().trim(),recattPRE.getText().toString().trim()));
        }
    }
}
