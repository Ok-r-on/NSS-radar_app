package com.example.nss.senior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nss.R;
import com.example.nss.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


//TODO: search names

public class SrAttendActivity extends AppCompatActivity {
    TextView attendActpres,attendActED,attendActEN;
    EditText attendActhrs;
    RecyclerView recyclerView;
    FirebaseDatabase db;
    FloatingActionButton submitattend;
    ArrayList<User> list;
    int attendanceCount;
    SearchView search_names;
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
        search_names=findViewById(R.id.search_names);

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

        if (fr_att != null && fr_att.equals("true")) {
            DatabaseReference scheduleReference = null;
            if (EDate != null && EveName != null) {
                scheduleReference = db.getReference("Schedule").child(EDate).child(EveName);
            }

            if (scheduleReference != null) {
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
            }
            if (scheduleReference != null) {
                scheduleReference.child("Present").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Iterate through each eventDate
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String username = snapshot.getKey();
                            // Update the status of the user in the "User" database to 1
                            if (username != null) {
                                databaseReferenceUser.child(username).child("status").setValue(1);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
            }
        }

        search_names.clearFocus();

        search_names.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterlist(newText);
                return true;
            }
        });

        submitattend.setOnClickListener(v -> {
            String hrs= attendActhrs.getText().toString().trim();
            if(Double.parseDouble(hrs) <= 6.0 && !hrs.isEmpty()){
                if (EDate != null && EveName != null) {
                    scheduleReference = db.getReference("Schedule").child(EDate).child(EveName);
                }
                scheduleReference.child("hours").setValue(hrs);
                scheduleReference.child("count").setValue(String.valueOf(attendanceCount));
                scheduleReference = scheduleReference.child("Present");
                for(User user : list){
                    if(user.getStatus() == 1){
                        String userName = user.getName();

                        scheduleReference.child(userName).setValue(userName).addOnSuccessListener(unused -> databaseReferenceUser.child(userName).child("status").setValue(0).addOnCompleteListener(Task::isSuccessful));
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
                            if (user != null && u.getName().equals(user.getName())) {
                                isNameExists = true;
                                break;
                            }
                        }
                        if (!isNameExists) {
                            fetcheduser.add(user);
                        }
                        if (user != null && user.getStatus() == 1) {
                            attendanceCount++;
                        }
                    }
                    list.addAll(fetcheduser);
                } else {
                    list.clear();
                }
                srAttendActivityAdapter.notifyDataSetChanged();
                attendActpres.setText(String.valueOf(attendanceCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void filterlist(String newText) {
        ArrayList<User> filteredlist = new ArrayList<>();
        for(User user: list){
            if(user.getName().toLowerCase().contains(newText.toLowerCase())){
                filteredlist.add(user);
            }
        }
        if(!filteredlist.isEmpty()){
            srAttendActivityAdapter.setFilteredList(filteredlist);
        }
    }

    public void updateUserStatus(int position, int status){
        String user = list.get(position).getName();
        databaseReferenceUser.child(user).child("status").setValue(status).addOnCompleteListener(task -> {
        });
    }
}