package com.example.nss.senior.sched;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nss.R;
import com.example.nss.model.Locat;
import com.example.nss.model.Schedule;
import com.example.nss.senior.SrAttendActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Pattern;

//TODO: optional(maybe set up a reciever to send notification to start the radar)

public class SrSchedule extends Fragment {

    private static final int REQUEST_CODE = 99;
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[.#$\\[\\]]");

    RecyclerView recyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    ArrayList<Schedule> list;
    ImageButton closeradarbtn,setDateSched;
    EditText edtEventName,edtEventLoc;
    Spinner edtEventType;
    SrScheduleAdapter srScheduleAdapter;
    DatabaseReference databaseReferenceSched, databaseLocateReference;
    Button savebtn,datebtn,timebtn, radStart, radStop;
    TextView eveName,eveDate,txttimesched,txtdatesched, markinSr;
    FirebaseDatabase db;
    String ename,eloc,etime,edate;
    long tmpstmp,selectedTimestamp;

    public SrSchedule() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sr_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.ScheduleListSenior);
        db=FirebaseDatabase.getInstance();
        databaseReferenceSched = db.getReference("Schedule");
        databaseLocateReference= db.getReference("Locate");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list=new ArrayList<>();
        srScheduleAdapter = new SrScheduleAdapter(getContext(),list);
        recyclerView.setAdapter(srScheduleAdapter);

        eveName = view.findViewById(R.id.rec_en_sched);
        eveDate = view.findViewById(R.id.recdatesched);
        setDateSched=view.findViewById(R.id.setDateSched);
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
                fetchedschedule.sort(Comparator.comparingLong(Schedule::getTmpstmp));
                // Clear existing items from list
                list.clear();
                list.addAll(fetchedschedule);
                srScheduleAdapter.notifyDataSetChanged();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

        });

        SrScheduleAdapter.setOnClickListener(new SrScheduleAdapter.OnitemClickListener() {
            @Override
            public void DeleteClick(int position) {
                String eventName = list.get(position).getEventName();
                String eventDate = list.get(position).getEventDate();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to delete this?")
                        .setPositiveButton("Yes", (dialog, id) -> databaseReferenceSched.child(eventDate).child(eventName).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                srScheduleAdapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "Successfully Deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }))
                        .setNegativeButton("No", (dialog, id) -> Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show());
                AlertDialog dialog = builder.create();
                dialog.show();

            }
            @Override
            public void EditClick(int position,String EveName, String ETime, String EDate, String EveLoc,String EveType ) {
                showEditDialog(EveName,ETime,EDate,EveLoc,EveType);
            }

            @Override
            public void RadClick(int position, String EveName, String EDate) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_start_stop_radar);

                radStart=dialog.findViewById(R.id.radStart);
                radStop=dialog.findViewById(R.id.radStop);
                markinSr=dialog.findViewById(R.id.markinSr);

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

                closeradarbtn=dialog.findViewById(R.id.closeradarbtn);

                closeradarbtn.setOnClickListener(v -> dialog.dismiss());

                radStart.setOnClickListener(v -> {
                    markinSr.setText("Detecting....");
                    getLocationofRoot();
                    radStop.setVisibility(View.VISIBLE);
                    radStart.setVisibility(View.GONE);
                });

                radStop.setOnClickListener(v -> databaseLocateReference.child("Root").removeValue().addOnCompleteListener(task ->
                {
                    Intent intent=new Intent(getContext(), SrAttendActivity.class);
                    intent.putExtra("EventName",EveName);
                    intent.putExtra("EventDate",EDate);
                    intent.putExtra("fr_att","false");
                    startActivity(intent);
                    radStart.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }));

                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }

            private void showEditDialog(String EveName, String ETime, String EDate, String EveLoc,String EveType){
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_edit_add_sched);

                edtEventName = dialog.findViewById(R.id.edtEventName);
                edtEventType = dialog.findViewById(R.id.edtEventType);
                edtEventLoc = dialog.findViewById(R.id.edtEventLoc);
                txttimesched=dialog.findViewById(R.id.txttimesched);
                txtdatesched=dialog.findViewById(R.id.txtdatesched);

                ArrayAdapter<String> adapter;
                adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.dropdown_options_for_type));
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                edtEventType.setAdapter(adapter);

                timebtn=dialog.findViewById(R.id.timebtn);
                datebtn=dialog.findViewById(R.id.datebtn);
                savebtn=dialog.findViewById(R.id.savebtn);

                edtEventName.setText(EveName);
                int position = adapter.getPosition(EveType);
                if (position != -1) {
                    edtEventType.setSelection(position);
                }
                edtEventLoc.setText(EveLoc);
                txttimesched.setText(ETime);
                txtdatesched.setText(EDate);

                timebtn.setOnClickListener(v -> {
                    final Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);
                    TimePickerDialog tpd = new TimePickerDialog(getContext(),android.R.style.Theme_Holo_Light_Dialog, (timePicker, hourOfDay, minute1) -> {
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
                    DatePickerDialog dpd = new DatePickerDialog(getContext(), (datePicker, year1, month1, day1) -> {
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

                edtEventName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (SPECIAL_CHARS_PATTERN.matcher(edtEventName.getText().toString()).find()) {
                            // If it matches, display an error message
                            edtEventName.setError("Title should not contain special characters like .#$[]");
                        } else {
                            // If it doesn't match, clear any previous error message
                            edtEventName.setError(null);
                        }
                    }
                });

                savebtn.setOnClickListener(v -> {
                    ename=edtEventName.getText().toString().trim();
                    edate=txtdatesched.getText().toString().trim();
                    eloc=edtEventLoc.getText().toString().trim();
                    etime=txttimesched.getText().toString().trim();
                    etype[0] =edtEventType.getSelectedItem().toString();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd|MM|yyyy");
                    try {
                        Date date = dateFormat.parse(edate);
                        if (date != null) {
                            tmpstmp=date.getTime(); // Return Unix timestamp in milliseconds
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }


                    if (!ename.isEmpty() && !edate.isEmpty()) {

                        databaseReferenceSched.child(edate).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().child(ename).child("eventTime").setValue(etime);
                                snapshot.getRef().child(ename).child("eventType").setValue(etype[0]);
                                snapshot.getRef().child(ename).child("eventLoc").setValue(eloc);
                                snapshot.getRef().child(ename).child("eventDate").setValue(edate);
                                snapshot.getRef().child(ename).child("tmpstmp").setValue(tmpstmp);
                                snapshot.getRef().child(ename).child("eventName").setValue(ename).addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getContext(), "Successfully Updated", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                    else {
                                        Toast.makeText(getContext(), "Failed to Update note", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                srScheduleAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Please enter both event name and date", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
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
                    fetchedschedule.sort((s1, s2) -> Long.compare(s2.getTmpstmp(), s1.getTmpstmp()));
                    list.addAll(fetchedschedule);
                } else {
                    list.clear();
                }
                srScheduleAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void getLocationofRoot(){
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Locat locate=new Locat(latitude,longitude);
                databaseLocateReference.child("Root").setValue(locate).addOnCompleteListener(task ->
                        Toast.makeText(getContext(),"Successfully Passed. Kindly await...",Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationofRoot();
            } else {
                Toast.makeText(getContext(), "Location permission denied, pleas allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}