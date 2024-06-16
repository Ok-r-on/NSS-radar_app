package com.example.nss.volunteer.sched;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nss.R;
import com.example.nss.model.Schedule;

import java.util.ArrayList;


public class VolScheduleAdapter extends RecyclerView.Adapter<VolScheduleAdapter.MyViewHolder>{
    Context context;
    ArrayList<Schedule> list;

    public VolScheduleAdapter(Context context, ArrayList<Schedule> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_vol_sched, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
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
        LinearLayout schedulelayoutexpandble_vol;
        LinearLayout LLschedulelayoutexpandble_vol;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            Time=itemView.findViewById(R.id.rectimeschedvol);
            Date=itemView.findViewById(R.id.recdateschedvol);
            EventName = itemView.findViewById(R.id.rec_en_sched_vol);
            EventType = itemView.findViewById(R.id.rec_et_sched_vol);
            EventLoc = itemView.findViewById(R.id.rec_el_sched_vol);
            schedulelayoutexpandble_vol=itemView.findViewById(R.id.schedulelayoutexpandble_vol);
            LLschedulelayoutexpandble_vol=itemView.findViewById(R.id.LLschedulelayoutexpandble_vol);
            LLschedulelayoutexpandble_vol.setOnClickListener(v -> toggleDescription());
        }
        private void toggleDescription() {
            int state = isExpanded ? View.VISIBLE : View.GONE ;
            isExpanded = !isExpanded;
            schedulelayoutexpandble_vol.setVisibility(state);
        }
    }
}
