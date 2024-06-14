package com.example.nss.volunteer.sched;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.nss.R;
import com.example.nss.model.Schedule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//TODO set up a filter date wise.
public class VolSchedule extends Fragment {

    RecyclerView recyclerView;
    ArrayList<Schedule> list;
    SwipeRefreshLayout swipetorefreshsched_vol;
    VolScheduleAdapter volScheduleAdapter;
    DatabaseReference databaseReferenceSched;
    ImageButton setDateSched;
    FirebaseDatabase db;
    public VolSchedule() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vol_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDateSched = view.findViewById(R.id.setDateSched_vol);
        setDateSched.setOnClickListener(v -> showDatePickerDialog());

        recyclerView = view.findViewById(R.id.ScheduleListVol);
        db= FirebaseDatabase.getInstance();
        databaseReferenceSched = db.getReference("Schedule");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list=new ArrayList<>();
        volScheduleAdapter = new VolScheduleAdapter(getContext(),list);
        recyclerView.setAdapter(volScheduleAdapter);
        swipetorefreshsched_vol=view.findViewById(R.id.swipetorefreshsched_vol);

        swipetorefreshsched_vol.setOnRefreshListener(() -> {
            databaseReferenceSched.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<Schedule> fetchedschedule = new ArrayList<>();
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if(snapshot.exists() && snapshot.hasChildren()){
                                for(DataSnapshot shot : snapshot.getChildren()){
                                    if(shot.exists()){
                                        Schedule s = shot.getValue(Schedule.class);

                                        boolean isSchedExists = false;
                                        for (Schedule schedule : list) {
                                            if (schedule.getEventDate().equals(s.getEventDate()) &&
                                                    schedule.getEventLoc().equals(s.getEventLoc()) &&
                                                    schedule.getEventTime().equals(s.getEventTime()) &&
                                                    schedule.getEventType().equals(s.getEventType()) &&
                                                    schedule.getEventName().equals(s.getEventName())) {
                                                isSchedExists = true;
                                                break;
                                            }
                                        }
                                        if (!isSchedExists) {
                                            fetchedschedule.add(s);
                                        }
                                    }
                                }
                            }
                        }
                        list.addAll(fetchedschedule);
                    } else {
                        list.clear();
                    }
                    volScheduleAdapter.notifyDataSetChanged();

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            swipetorefreshsched_vol.setRefreshing(false);
        });

        databaseReferenceSched.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Schedule> fetchedschedule = new ArrayList<>();
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    list.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(snapshot.exists() && snapshot.hasChildren()){
                            for(DataSnapshot shot : snapshot.getChildren()){
                                if(shot.exists()){
                                    Schedule s = shot.getValue(Schedule.class);

                                    boolean isSchedExists = false;
                                    for (Schedule schedule : list) {
                                        if (schedule.getEventDate().equals(s.getEventDate()) &&
                                                schedule.getEventLoc().equals(s.getEventLoc()) &&
                                                schedule.getEventTime().equals(s.getEventTime()) &&
                                                schedule.getEventType().equals(s.getEventType()) &&
                                                schedule.getEventName().equals(s.getEventName())) {
                                            isSchedExists = true;
                                            break;
                                        }
                                    }
                                    if (!isSchedExists) {
                                        fetchedschedule.add(s);
                                    }
                                }
                            }
                        }
                    }
                    list.addAll(fetchedschedule);
                } else {
                    list.clear();
                }
                volScheduleAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();
            volScheduleAdapter.filterByDate(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}