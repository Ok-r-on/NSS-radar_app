package com.example.nss.senior;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nss.AboutDev;
import com.example.nss.R;
import com.example.nss.model.Schedule;
import com.example.nss.senior.attend.SrAttendance;
import com.example.nss.senior.home.SrHome;
import com.example.nss.senior.notice.SrNotice;
import com.example.nss.senior.sched.SrSchedule;
import com.example.nss.volunteer.Volunteer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Senior extends AppCompatActivity {
    EditText edtEventName,edtEventLoc;
    Spinner edtEventType;
    TextView txttimesched,txtdatesched;
    Button savebtn,datebtn,timebtn;
    DatabaseReference databaseReference;
    FirebaseDatabase db;
    private static final String PREFS_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior);

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        String UserName = sharedPref.getString("UserName","");
        Toolbar toolbar=findViewById(R.id.toolbarforsr);
        setSupportActionBar(toolbar);
        toolbar.setTitle(UserName);

        ViewPager viewPager = findViewById(R.id.view_pagerSr);
        SeniorVPAdapter adapter = new SeniorVPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new SrHome());
        adapter.addFragment(new SrSchedule());
        adapter.addFragment(new SrAttendance());
        adapter.addFragment(new SrNotice());
        viewPager.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBarSr);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Unused
            }
            @Override
            public void onPageSelected(int position) {
                // Update the selected item in the BottomNavigationView when the ViewPager's page changes
                if (position >= 2) {
                    bottomNavigationView.getMenu().getItem(position + 1).setChecked(true);
                } else {
                    bottomNavigationView.getMenu().getItem(position).setChecked(true);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                // Unused
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navSrHome) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.navSrSched) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (item.getItemId() == R.id.navSrAttend) {
                viewPager.setCurrentItem(2);
                return true;
            } else if(item.getItemId() == R.id.navSrNotice){
                viewPager.setCurrentItem(3);
                return true;
            } else if (item.getItemId() == R.id.navSrAdd) {
                showEditDialog();
                return true;
            }
            return false;
        });
    }
    private void showEditDialog(){
        Dialog dialog = new Dialog(Senior.this);
        dialog.setContentView(R.layout.dialog_edit_add_sched);

        db=FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Schedule");

        edtEventName = dialog.findViewById(R.id.edtEventName);
        edtEventType = dialog.findViewById(R.id.edtEventType);
        edtEventLoc = dialog.findViewById(R.id.edtEventLoc);
        txttimesched=dialog.findViewById(R.id.txttimesched);
        txtdatesched=dialog.findViewById(R.id.txtdatesched);
        timebtn=dialog.findViewById(R.id.timebtn);
        datebtn=dialog.findViewById(R.id.datebtn);
        savebtn=dialog.findViewById(R.id.savebtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.dropdown_options_for_type));

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        edtEventType.setAdapter(adapter);

        timebtn.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance TimePickerDialog and return it.
            TimePickerDialog tpd = new TimePickerDialog(Senior.this,android.R.style.Theme_Holo_Light_Dialog, (timePicker, hourOfDay, minute1) -> {
                int hour1 = hourOfDay % 12;
                String amPm = (hourOfDay < 12) ? "AM" : "PM";
                if (hour1 == 0) {
                    hour1 = 12; // Handle midnight (12 AM)
                }
                String timeText = String.format("%02d:%02d %s", hour1, minute1, amPm);
                txttimesched.setText(timeText);
            }, hour, minute, false);
            tpd.show();
        });
        datebtn.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it.
            DatePickerDialog dpd = new DatePickerDialog(Senior.this, (datePicker, year1, month1, day1) -> {
                String dateText = day1 + "|" + (month1 + 1) + "|" + year1;
                txtdatesched.setText(dateText);
            }, year, month, day);
            dpd.show();
        });
        final String[] etype = {""};
        edtEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                etype[0] =parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        savebtn.setOnClickListener(v -> {
            String ename=edtEventName.getText().toString();
            String edate=txtdatesched.getText().toString();
            String eloc=edtEventLoc.getText().toString();
            String etime=txttimesched.getText().toString();


            if (!ename.isEmpty() && !edate.contains("-") && !eloc.isEmpty() && !etime.contains("-") && !etype[0].isEmpty()) {
                // Check if the item already exists in the database
                databaseReference.child(edate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(Senior.this, "Event with this name already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            // Add the item to the database
                            Schedule schedule = new Schedule(edate, etime,ename, etype[0],eloc, conv(edate));
                            databaseReference.child(edate).child(ename).setValue(schedule).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Senior.this, "Successfully Added", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(Senior.this, "Failed to add schedule", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    private long conv(String edate) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd|MM|yyyy");
                        try {
                            Date date = dateFormat.parse(edate);
                            return date.getTime(); // Return Unix timestamp in milliseconds
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Senior.this, "Database Error", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getApplication(), "Please fill all details", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_menu_for_sr,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id == R.id.logoutfromsr){
            logout();
            return true;

        } else if(id == R.id.abouttheapplication){
            startActivity(new Intent(Senior.this, AboutDev.class));
        }
        return true;
    }
    private void logout() {
        Toast.makeText(this, "Logged out of Senior", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Senior.this, Volunteer.class));
        finish();
    }
}