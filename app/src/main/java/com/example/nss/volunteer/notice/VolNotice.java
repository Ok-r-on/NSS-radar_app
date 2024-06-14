package com.example.nss.volunteer.notice;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nss.R;
import com.example.nss.model.Notice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VolNotice extends Fragment {

    SwipeRefreshLayout swipetorefersh_vol;
    TextView recnottit,recnotdescr;
    RecyclerView recyclerView;
    ArrayList<Notice> list;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    VolNotAdapter volNotAdapter;
    public VolNotice() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vol_notice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.NoticeListVol);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list=new ArrayList<>();
        volNotAdapter = new VolNotAdapter(getContext(),list);
        recyclerView.setAdapter(volNotAdapter);
        db= FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Notice");
        recnottit=view.findViewById(R.id.recnottit_vol);
        recnotdescr=view.findViewById(R.id.recnotdescr_vol);
        swipetorefersh_vol=view.findViewById(R.id.swipetorefersh_vol);

        swipetorefersh_vol.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Notice> fetchedNotices = new ArrayList<>();

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Notice n = dataSnapshot.getValue(Notice.class);

                            boolean isNoticeExists = false;
                            for (Notice notice : list) {
                                if (notice.getTitle().equals(n.getTitle()) &&
                                        notice.getDescription().equals(n.getDescription())) {
                                    isNoticeExists = true;
                                    break;
                                }
                            }
                            if (!isNoticeExists) {
                                fetchedNotices.add(n);
                            }
                        }
                        list.addAll(fetchedNotices);
                    } else {
                        list.clear();
                    }
                    volNotAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            swipetorefersh_vol.setRefreshing(false);
        },1500));

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Notice> fetchedNotices = new ArrayList<>();

                if (snapshot.exists() && snapshot.hasChildren()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Notice n = dataSnapshot.getValue(Notice.class);

                        boolean isNoticeExists = false;
                        for (Notice notice : list) {
                            if (notice.getTitle().equals(n.getTitle()) &&
                                    notice.getDescription().equals(n.getDescription())) {
                                isNoticeExists = true;
                                break;
                            }
                        }
                        if (!isNoticeExists) {
                            fetchedNotices.add(n);
                        }
                    }
                    list.addAll(fetchedNotices);
                 } else {
                    list.clear();
                }
                volNotAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}