package com.example.nss.senior.notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nss.model.Notice;
import com.example.nss.R;

import java.util.ArrayList;

public class NotRecAdapter extends RecyclerView.Adapter<NotRecAdapter.MyViewHolder> {

    Context context;
    ArrayList<Notice> list;
    static NotRecAdapter.NoticeOnitemClickListener listener;
    public interface NoticeOnitemClickListener{
        void DeleteNotClick(int position);
        void EditNotClick(int position,String title, String description);
    }
    public static void setOnClickListener(NotRecAdapter.NoticeOnitemClickListener clicklistener){
        listener = clicklistener;
    }
    public NotRecAdapter(Context context, ArrayList<Notice> list) {
        this.context = context;
        this.list = list;
    }

    public void setFilteredList(ArrayList<Notice> filteredList){
        this.list = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotRecAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_sr_notice,parent,false);

        return new MyViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotRecAdapter.MyViewHolder holder, int position) {
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
        ImageButton edtnotbtn, delnotbtn;

        public MyViewHolder(@NonNull View itemView, NoticeOnitemClickListener listener) {
            super(itemView);

            title=itemView.findViewById(R.id.recnottit);
            descrp=itemView.findViewById(R.id.recnotdescr);
            descrp.setOnClickListener(v -> toggleDescription());

            edtnotbtn = itemView.findViewById(R.id.edtnotbtn);
            edtnotbtn.setOnClickListener(v -> listener.EditNotClick(getAdapterPosition(),title.getText().toString().trim(),descrp.getText().toString().trim()));
            delnotbtn = itemView.findViewById(R.id.delnotbtn);
            delnotbtn.setOnClickListener(v -> listener.DeleteNotClick(getAdapterPosition()));

        }
        private void toggleDescription() {
            int maxLines = isExpanded ? 1 : Integer.MAX_VALUE ;
            isExpanded = !isExpanded;
            descrp.setMaxLines(maxLines);
        }
    }
}
