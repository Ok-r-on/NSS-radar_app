package com.example.nss.senior.home;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nss.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SrHome extends Fragment {
    DatabaseReference scheduleRef;
    FirebaseDatabase db;
    TextView totalhrs,AB1percSr,AB2percSr,ColpercSr,UnipercSr;
    ProgressBar prBarAB1_Sr,prBarAB2_Sr,prBarCol_Sr,prBarUni_Sr;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sr_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db= FirebaseDatabase.getInstance();
        scheduleRef = db.getReference("Schedule");

        PieChart pieChart= view.findViewById(R.id.piechartforsr);

        totalhrs=view.findViewById(R.id.totalhrs);
        AB1percSr=view.findViewById(R.id.AB1percSr);
        AB2percSr=view.findViewById(R.id.AB2percSr);
        ColpercSr=view.findViewById(R.id.ColpercSr);
        UnipercSr=view.findViewById(R.id.UnipercSr);

        prBarAB1_Sr=view.findViewById(R.id.prBarAB1_Sr);
        prBarAB2_Sr=view.findViewById(R.id.prBarAB2_Sr);
        prBarCol_Sr=view.findViewById(R.id.prBarCol_Sr);
        prBarUni_Sr=view.findViewById(R.id.prBarUni_Sr);

        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                updateChart(totalHours -> {
                    piechartfilling(pieChart,totalHours);
                    // You can now safely use the totalHours array here
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private interface ChartUpdateCallback {
        void onUpdate(double[] totalHours);
    }

    public void updateChart(final ChartUpdateCallback callback) {
        final double[] totalHours = new double[]{0, 0, 0, 0};
        scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventDateSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot eventNameSnapshot : eventDateSnapshot.getChildren()) {
                        String currentEventType = eventNameSnapshot.child("eventType").getValue(String.class);
                        if(!eventNameSnapshot.child("hours").exists()){
                            continue;
                        }
                        Double hours = Double.valueOf(eventNameSnapshot.child("hours").getValue(String.class));
                        if (currentEventType != null) {
                            switch (currentEventType) {
                                case "University":
                                    totalHours[0] += hours;
                                    break;
                                case "Area Base 1":
                                    totalHours[1] += hours;
                                    break;
                                case "Area Base 2":
                                    totalHours[2] += hours;
                                    break;
                                case "College":
                                    totalHours[3] += hours;
                                    break;
                            }
                        }
                    }
                }
                callback.onUpdate(totalHours);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }


    private void piechartfilling(PieChart pieChart, double[] totalHours) {
        List<PieEntry> entries = new ArrayList<>();
        setPercentageText(totalHours[0],UnipercSr,20,prBarUni_Sr);
        setPercentageText(totalHours[1],AB1percSr,40, prBarAB1_Sr);
        setPercentageText(totalHours[2],AB2percSr,40,prBarAB2_Sr);
        setPercentageText(totalHours[3],ColpercSr,20,prBarCol_Sr);

        int tot= (int) (totalHours[0] + totalHours[1]+ totalHours[2] + totalHours[3]);

        totalhrs.setText(String.valueOf(tot));

        if (totalHours[0] > 0) {
            entries.add(new PieEntry((float) totalHours[0], "University"));
        }
        if (totalHours[1] > 0) {
            entries.add(new PieEntry((float) totalHours[1], "AB 1"));
        }
        if (totalHours[2] > 0) {
            entries.add(new PieEntry((float) totalHours[2], "AB 2"));
        }
        if (totalHours[3] > 0) {
            entries.add(new PieEntry((float) totalHours[3], "College"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        };

        dataSet.setValueFormatter(valueFormatter);

        int[] customColors = new int[entries.size()];
        int colorIndex = 0;
        if (totalHours[0] > 0) {
            customColors[colorIndex++] = Color.argb(255, 102, 178, 255); // Soft blue for Uni
        }
        if (totalHours[1] > 0) {
            customColors[colorIndex++] = Color.argb(255, 204, 102, 102); // Light blue for AB1
        }
        if (totalHours[2] > 0) {
            customColors[colorIndex++] = Color.argb(255, 213, 166, 189); // Peach for AB2
        }
        if (totalHours[3] > 0) {
            customColors[colorIndex] = Color.argb(255, 153, 204, 204); // Light teal for College
        }
        dataSet.setColors(customColors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setHoleRadius(40f);
        pieChart.animateXY(1000,500);
        pieChart.setCenterText(tot+" hours completed");
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
}