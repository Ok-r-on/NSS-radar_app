package com.example.nss.senior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nss.R;
import com.example.nss.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


//TODO: if opted for editing (SHOULD WORK)

public class SrAttendActivity extends AppCompatActivity {
    TextView attendActpres,attendActED,attendActEN;
    EditText attendActhrs;
    RecyclerView recyclerView;
    FirebaseDatabase db;
    FloatingActionButton submitattend;
    ArrayList<User> list;
    int attendanceCount;
    DatabaseReference databaseReferenceUser,scheduleReference;
    SrAttendActivityAdapter srAttendActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sr_attend);

        recyclerView=findViewById(R.id.attendactrec);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        list=new ArrayList<>();
        srAttendActivityAdapter = new SrAttendActivityAdapter(this,list);
        recyclerView.setAdapter(srAttendActivityAdapter);
        recyclerView.setHasFixedSize(true);
        db=FirebaseDatabase.getInstance();
        databaseReferenceUser=db.getReference("User");

        Intent intent=getIntent();
        String EveName=intent.getStringExtra("EventName");
        String EDate=intent.getStringExtra("EventDate");
        String fr_att= intent.getStringExtra("fr_att");
        //meaning is it from attendance frag

        attendActpres=findViewById(R.id.attendActpres);
        attendActED=findViewById(R.id.attendActED);
        attendActEN=findViewById(R.id.attendActEN);
        attendActhrs=findViewById(R.id.attendActhrs);
        submitattend=findViewById(R.id.submitattend);

        attendActED.setText(EDate);
        attendActEN.setText(EveName);

        if(fr_att.equals("true")){
            DatabaseReference scheduleReference = db.getReference("Schedule").child(EDate).child(EveName);

            scheduleReference.child("hours").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String hrs = dataSnapshot.getValue(String.class);
                        attendActhrs.setText(hrs);
                    } else {
                        attendActhrs.setText("--");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            scheduleReference.child("Present").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Iterate through each eventDate
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String username = snapshot.getKey();
                        // Update the status of the user in the "User" database to 1
                        databaseReferenceUser.child(username).child("status").setValue(1);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }


        submitattend.setOnClickListener(v -> {
            String hrs= attendActhrs.getText().toString().trim();
            if(Double.parseDouble(hrs) <= 6.0 && !hrs.isEmpty()){
                scheduleReference = db.getReference("Schedule").child(EDate).child(EveName);
                scheduleReference.child("hours").setValue(hrs);
                scheduleReference.child("count").setValue(String.valueOf(attendanceCount));
                scheduleReference = scheduleReference.child("Present");
                for(User user : list){
                    if(user.getStatus() == 1){
                        String userName = user.getName();

                        scheduleReference.child(userName).setValue(userName).addOnSuccessListener(unused -> databaseReferenceUser.child(userName).child("status").setValue(0).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                            }
                        }));
                    }
                }
                finish();
            }
            else if (hrs.isEmpty()){
                attendActhrs.setError("Empty Field");
            } else{
                attendActhrs.setError("Cannot be greater than 6");
            }
        });

        databaseReferenceUser=db.getReference("User");
        databaseReferenceUser.addValueEventListener(new ValueEventListener()  {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> fetcheduser = new ArrayList<>();
                attendanceCount = 0; // Initialize the count
                if (dataSnapshot.exists()) {
                    list.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        boolean isNameExists = false;
                        for (User u : list) {
                            if (u.getName().equals(user.getName())) {
                                isNameExists = true;
                                break;
                            }
                        }
                        if (!isNameExists) {
                            fetcheduser.add(user);
                        }
                        if (user.getStatus() == 1) {
                            attendanceCount++;
                        }
                    }
                    list.addAll(fetcheduser);
                    srAttendActivityAdapter.notifyDataSetChanged();
                } else {
                    list.clear();
                    srAttendActivityAdapter.notifyDataSetChanged();
                }
                attendActpres.setText(String.valueOf(attendanceCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void updateUserStatus(int position, int status){
        String user = list.get(position).getName();
        databaseReferenceUser.child(user).child("status").setValue(status).addOnCompleteListener(task -> {
        });
    }
}