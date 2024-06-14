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
        /*this.filteredList = new ArrayList<>(); // Initialize filteredList
        this.filteredList.addAll(list);*/
    }
   /* public void filterByDate(Date selectedDate) {
        if (filteredList == null) {
            filteredList = new ArrayList<>();
        } else {
            filteredList.clear();
        }
        for (Schedule schedule : list) {
            // Convert event date string to Date object for comparison
            Date eventDate = parseDate(schedule.getEventDate());
            if (eventDate != null && !eventDate.before(selectedDate)) {
                filteredList.add(schedule);
            }
        }

        // Sort the filteredList in ascending order based on event dates
        // Sort the filteredList in ascending order based on event dates
        filteredList.sort((schedule1, schedule2) -> {
            Date date1 = parseDate(schedule1.getEventDate());
            Date date2 = parseDate(schedule2.getEventDate());
            return date2.compareTo(date1);
        });

        // Reverse the sorted list to get the latest upcoming event at the top
        Collections.reverse(filteredList);
        log.d("TAG","Reaching this point");
        notifyDataSetChanged(); // Notify adapter of data change
    }
    private Date parseDate(String eventDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd|MM|yyyy");
        // Parse the string date into a Date object
        try {
            return dateFormat.parse(eventDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }*/

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