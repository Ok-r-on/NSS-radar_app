package com.example.nss.volunteer.home;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nss.R;
import com.example.nss.model.Locat;
import com.example.nss.senior.SrAttendActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//TODO radar testing remaining
public class VolHome extends Fragment {
    private static final int REQUEST_CODE = 99;
    Button mark;
    ImageButton closeradarbtn_vol;
    DatabaseReference locatref,scheduleRef,databaseReferenceUser;
    FirebaseDatabase db;
    PieChart pieChart;
    CardView radarcard;
    String UserName;
    TextView totalhrsofuser,AB1perc,AB2perc,Colperc,Uniperc;
    ProgressBar prBarAB1,prBarAB2,prBarCol,prBarUni;
    Dialog dialog;
    private static final float VICINITY_RADIUS = 10.0f; // Vicinity radius in meters

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final String PREFS_NAME = "MyPrefs";
    TextView marksuc;
    public VolHome() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SharedPreferences sharedPref = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        UserName = sharedPref.getString("UserName","");
        return inflater.inflate(R.layout.fragment_vol_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(3000);

        db= FirebaseDatabase.getInstance();
        databaseReferenceUser = db.getReference("User");
        locatref = db.getReference("Locate");


        pieChart= view.findViewById(R.id.piechartforvol);

        totalhrsofuser=view.findViewById(R.id.totalhrsofuser);
        AB1perc=view.findViewById(R.id.AB1perc);
        AB2perc=view.findViewById(R.id.AB2perc);
        Colperc=view.findViewById(R.id.Colperc);
        Uniperc=view.findViewById(R.id.Uniperc);

        prBarAB1=view.findViewById(R.id.prBarAB1);
        prBarAB2=view.findViewById(R.id.prBarAB2);
        prBarCol=view.findViewById(R.id.prBarCol);
        prBarUni=view.findViewById(R.id.prBarUni);

        radarcard=view.findViewById(R.id.radarcard);
        locatref.child("Root").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    radarcard.setVisibility(View.VISIBLE);
                    radarcard.setOnClickListener(v -> {
                        showmarkingdialog();
                    });
                }
                else {
                    radarcard.setVisibility(View.GONE);
                    if(fusedLocationProviderClient != null && locationCallback != null){
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                    if(dialog != null){
                        dialog.dismiss();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        scheduleRef = db.getReference("Schedule");

        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateChart();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showmarkingdialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_mark_radar);

        closeradarbtn_vol=dialog.findViewById(R.id.closeradarbtn_vol);
        mark=dialog.findViewById(R.id.markbtn);
        marksuc=dialog.findViewById(R.id.marksuc);

        closeradarbtn_vol.setOnClickListener(v -> dialog.dismiss());

        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mark.setVisibility(View.GONE);
                marksuc.setText("Marking...");
                getandsetlocation();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    public void updateChart(){
        final double[] totalHours = new double[]{0, 0, 0, 0, 0};
        //For University
        scheduleRef.orderByChild("eventType").equalTo("University").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot eventDateSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot eventNameSnapshot : eventDateSnapshot.getChildren()) {

                        for (DataSnapshot presentSnapshot : eventNameSnapshot.child("Present").getChildren()) {
                            String presentUser = presentSnapshot.getKey();

                            if (presentUser.equals(UserName)) {
                                double hours = eventNameSnapshot.child("hours").getValue(Double.class);
                                totalHours[0] += hours;
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
        //for Area Base 1
        scheduleRef.orderByChild("eventType").equalTo("Area Base 1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot eventDateSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot eventNameSnapshot : eventDateSnapshot.getChildren()) {

                        for (DataSnapshot presentSnapshot : eventNameSnapshot.child("Present").getChildren()) {
                            String presentUser = presentSnapshot.getKey();

                            if (presentUser.equals(UserName)) {
                                double hours = eventNameSnapshot.child("hours").getValue(Double.class);
                                totalHours[1] += hours;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
        //for Area Base 2
        scheduleRef.orderByChild("eventType").equalTo("Area Base 2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot eventDateSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot eventNameSnapshot : eventDateSnapshot.getChildren()) {

                        for (DataSnapshot presentSnapshot : eventNameSnapshot.child("Present").getChildren()) {
                            String presentUser = presentSnapshot.getKey();

                            if (presentUser.equals(UserName)) {
                                double hours = eventNameSnapshot.child("hours").getValue(Double.class);
                                totalHours[2] += hours;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
        //for College
        scheduleRef.orderByChild("eventType").equalTo("College").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot eventDateSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot eventNameSnapshot : eventDateSnapshot.getChildren()) {

                        for (DataSnapshot presentSnapshot : eventNameSnapshot.child("Present").getChildren()) {
                            String presentUser = presentSnapshot.getKey();

                            if (presentUser.equals(UserName)) {
                                double hours = eventNameSnapshot.child("hours").getValue(Double.class);
                                totalHours[3] += hours;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });

        piechartfilling(pieChart,totalHours);
    }

    private void piechartfilling(PieChart pieChart, double[] totalHours) {
        List<PieEntry> entries = new ArrayList<>();

        setPercentageText(totalHours[0],AB1perc,40, prBarAB1);
        setPercentageText(totalHours[1],AB2perc,40,prBarAB2);
        setPercentageText(totalHours[2],Colperc,20,prBarCol);
        setPercentageText(totalHours[3],Uniperc,20,prBarUni);

        totalHours[4]= (gettotalcount(totalHours[0],40) + gettotalcount(totalHours[1],40)+
                gettotalcount(totalHours[2],20)+ gettotalcount(totalHours[3],20));

        totalhrsofuser.setText(String.valueOf((int) totalHours[4]));

        entries.add(new PieEntry((float) totalHours[0], "Area Base 1"));
        entries.add(new PieEntry((float) totalHours[1], "Area Base 2"));
        entries.add(new PieEntry((float) totalHours[2], "College"));
        entries.add(new PieEntry((float) totalHours[3], "University"));
        entries.add(new PieEntry((float) (120 - totalHours[4]), ""));

        PieDataSet dataSet = new PieDataSet(entries, "");

        int[] customColors = new int[entries.size()];
        customColors[0] = Color.argb(45,204,112,255);
        customColors[1] = Color.argb(232,76,61,255);
        customColors[2] = Color.argb(241,196,15,255);
        customColors[3] = Color.argb(53,152,219,255);
        customColors[4] = Color.TRANSPARENT;
        dataSet.setColors(customColors);

        dataSet.setValueTextColor(Color.TRANSPARENT);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);


        pieChart.setHoleRadius(40f);
        pieChart.animateXY(1000,500);
        pieChart.setCenterText("Out of 120 hours");
        pieChart.setDrawRoundedSlices(true);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.getLegend().setYOffset(10f);
        pieChart.setEntryLabelColor(Color.TRANSPARENT);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        pieChart.getLegend().setXEntrySpace(30f);
        pieChart.getLegend().setXOffset(25f);
        pieChart.getLegend().setTextSize(15f);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate(); // refresh
    }

    private void setPercentageText(double totalHours, TextView perc, int divisor, ProgressBar prBar) {
        int percentage = totalHours <= divisor ? (int) (totalHours / divisor * 100) : 100;
        perc.setText(percentage + "%");
        prBar.setProgress(percentage);
    }
    public int gettotalcount(double totalHours, int divisor){
        return totalHours < divisor ? (int) totalHours : divisor;
    }

    private float calculateDistance(double userLat, double userLng, double rootLat, double rootLng) {
        Location locationUser = new Location("");
        locationUser.setLatitude(userLat);
        locationUser.setLongitude(userLng);

        Location locationRoot = new Location("");
        locationRoot.setLatitude(rootLat);
        locationRoot.setLongitude(rootLng);

        return locationUser.distanceTo(locationRoot);
    }

    public void getandsetlocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();

                    locatref.child("Root").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Locat rootLocation = snapshot.getValue(Locat.class);
                            if (rootLocation != null) {
                                float distance = calculateDistance(location.getLatitude(), location.getLongitude(),
                                        rootLocation.getlatitude(), rootLocation.getlongitude());

                                if (distance <= VICINITY_RADIUS) {
                                    databaseReferenceUser.child(UserName).child("status").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getContext(), "Attendance marked", Toast.LENGTH_SHORT).show();
                                            marksuc.setText("Mark Successfully");
                                        }
                                    });
                                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                }
                                else {
                                    marksuc.setText("Not In Range");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle database error
                        }
                    });
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getandsetlocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied, pleas allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}