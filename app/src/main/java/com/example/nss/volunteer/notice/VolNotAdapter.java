package com.example.nss.volunteer.notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nss.R;
import com.example.nss.model.Notice;

import java.util.ArrayList;


public class VolNotAdapter extends RecyclerView.Adapter<VolNotAdapter.MyViewHolder>{
    Context context;
    ArrayList<Notice> list;

    public VolNotAdapter(Context context, ArrayList<Notice> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VolNotAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_vol_notice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VolNotAdapter.MyViewHolder holder, int position) {
        Notice notice =list.get(position);
        holder.title.setText(notice.getTitle());
        holder.descrp.setText(notice.getDescription());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private boolean isExpanded = false;
        TextView title,descrp;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.recnottit_vol);
            descrp=itemView.findViewById(R.id.recnotdescr_vol);
            descrp.setOnClickListener(v -> toggleDescription_forvol());
        }
        private void toggleDescription_forvol() {
            int maxLines = isExpanded ? 1 : Integer.MAX_VALUE ;
            isExpanded = !isExpanded;
            descrp.setMaxLines(maxLines);
        }
    }
}
