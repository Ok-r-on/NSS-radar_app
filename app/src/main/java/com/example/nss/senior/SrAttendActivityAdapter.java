package com.example.nss.senior;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nss.R;
import com.example.nss.model.User;

import java.util.ArrayList;

public class SrAttendActivityAdapter extends RecyclerView.Adapter<SrAttendActivityAdapter.MyViewHolder> {
    Context context;
    ArrayList<User> list;

    public SrAttendActivityAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SrAttendActivityAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v =LayoutInflater.from(context).inflate(R.layout.recycler_sr_listofstudents_act,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SrAttendActivityAdapter.MyViewHolder holder, int position) {

        User user =list.get(position);
        holder.nameofuser.setText(user.getName());
        holder.usercardview_sr.setCardBackgroundColor(getColor(user));

    }

    private int getColor(User user){

        int status = user.getStatus();
        if(status==1){
            return Color.parseColor("#"+ Integer.toHexString(ContextCompat.getColor(context,R.color.green)));
        } else{
            return Color.parseColor("#"+ Integer.toHexString(ContextCompat.getColor(context,R.color.white)));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nameofuser;
        CardView usercardview_sr;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameofuser=itemView.findViewById(R.id.nameofuser);
            usercardview_sr=itemView.findViewById(R.id.usercardview_sr);

            usercardview_sr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        int color = usercardview_sr.getCardBackgroundColor().getDefaultColor();
                        if (color == ContextCompat.getColor(itemView.getContext(), R.color.white)) {
                            usercardview_sr.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
                            // Update status in database
                            updateStatus(position, 1);
                        } else {
                            usercardview_sr.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                            // Update status in database
                            updateStatus(position, 0);
                        }
                    }
                }
                private void updateStatus(int position, int status) {
                     ((SrAttendActivity) itemView.getContext()).updateUserStatus(position, status);
                }
            });
        }
    }
}
