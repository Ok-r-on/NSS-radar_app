package com.example.nss.volunteer.sched;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Comparator;
import java.util.Date;

public class VolSchedule extends Fragment {
    RecyclerView recyclerView;
    ArrayList<Schedule> list;
    VolScheduleAdapter volScheduleAdapter;
    DatabaseReference databaseReferenceSched;
    ImageButton setDateSched;
    FirebaseDatabase db;
    long selectedTimestamp;
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
        setDateSched.setOnClickListener(v -> {
            ArrayList<Schedule> fetchedschedule = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                Date selectedDate = calendar.getTime();
                selectedTimestamp = selectedDate.getTime();
                for (Schedule schedule : list) {
                    long eventTimestamp = schedule.getTmpstmp();
                    if (eventTimestamp >= selectedTimestamp) {
                        fetchedschedule.add(schedule);
                    }
                }
                //used list.sort instead of collection.sort and
                fetchedschedule.sort(Comparator.comparingLong(Schedule::getTmpstmp));
                // Clear existing items from list
                list.clear();
                list.addAll(fetchedschedule);
                volScheduleAdapter.notifyDataSetChanged();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

        });

        recyclerView = view.findViewById(R.id.ScheduleListVol);
        db= FirebaseDatabase.getInstance();
        databaseReferenceSched = db.getReference("Schedule");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list=new ArrayList<>();
        volScheduleAdapter = new VolScheduleAdapter(getContext(),list);
        recyclerView.setAdapter(volScheduleAdapter);

        databaseReferenceSched.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Schedule> fetchedschedule = new ArrayList<>();
                if (snapshot.exists() && snapshot.hasChildren()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(dataSnapshot.exists() && dataSnapshot.hasChildren()){
                            for(DataSnapshot shot : dataSnapshot.getChildren()){
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
                    fetchedschedule.sort((s1, s2) -> Long.compare(s2.getTmpstmp(), s1.getTmpstmp()));
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
}