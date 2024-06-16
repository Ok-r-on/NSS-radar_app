package com.example.nss.senior.sched;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nss.R;
import com.example.nss.model.Schedule;

import java.util.ArrayList;

public class SrScheduleAdapter extends RecyclerView.Adapter<SrScheduleAdapter.MyViewHolder> {
    Context context;
    ArrayList<Schedule> list;
    static OnitemClickListener listener;
    //ArrayList<Schedule> filteredList;
    public interface OnitemClickListener{
        void DeleteClick(int position);
        void EditClick(int position,String EveName, String ETime, String EDate, String EveLoc,String EveType);
        void RadClick(int position, String EveName, String EDate);
    }
    public static void setOnClickListener(OnitemClickListener clicklistener){
        listener = clicklistener;
    }
    public SrScheduleAdapter(Context context, ArrayList<Schedule> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SrScheduleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_sr_sched,parent,false);

        return new MyViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SrScheduleAdapter.MyViewHolder holder, int position) {
        Schedule schedule =list.get(position);

        holder.EventName.setText(schedule.getEventName());
        holder.Date.setText(schedule.getEventDate());
        holder.Time.setText(schedule.getEventTime());
        holder.EventType.setText(schedule.getEventType());
        holder.EventLoc.setText(schedule.getEventLoc());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private Boolean isExpanded=false;
        TextView EventName,EventType,EventLoc,Time,Date;
        LinearLayout schedulelayoutexpandable;
        RelativeLayout relativeschedulelayoutexpandble;
        ImageButton edtschedbtn, delschedbtn, radschedbtn;

        public MyViewHolder(@NonNull View itemView, OnitemClickListener listener) {
            super(itemView);

            Time=itemView.findViewById(R.id.rectimesched);
            Date=itemView.findViewById(R.id.recdatesched);
            EventName = itemView.findViewById(R.id.rec_en_sched);
            EventType = itemView.findViewById(R.id.rec_et_sched);
            EventLoc = itemView.findViewById(R.id.rec_el_sched);
            schedulelayoutexpandable=itemView.findViewById(R.id.schedulelayoutexpandble);
            relativeschedulelayoutexpandble=itemView.findViewById(R.id.relativeschedulelayoutexpandble);
            relativeschedulelayoutexpandble.setOnClickListener(v -> toggleDescription());

            edtschedbtn = itemView.findViewById(R.id.edtschedbtn);
            edtschedbtn.setOnClickListener(v -> listener.EditClick(getAdapterPosition(), EventName.getText().toString().trim(),
                    Time.getText().toString().trim()
            ,Date.getText().toString().trim(),EventLoc.getText().toString().trim(),EventType.getText().toString().trim()));
            delschedbtn = itemView.findViewById(R.id.delschedbtn);
            delschedbtn.setOnClickListener(v -> listener.DeleteClick(getAdapterPosition()));
            radschedbtn=itemView.findViewById(R.id.radschedbtn);
            radschedbtn.setOnClickListener( v -> listener.RadClick(getAdapterPosition(),EventName.getText().toString().trim(),
                    Date.getText().toString().trim()));
        }
        private void toggleDescription() {
            int state = isExpanded ? View.VISIBLE : View.GONE ;
            isExpanded = !isExpanded;
            schedulelayoutexpandable.setVisibility(state);
            delschedbtn.setVisibility(state);
            edtschedbtn.setVisibility(state);
            radschedbtn.setVisibility(state);
        }
    }
}