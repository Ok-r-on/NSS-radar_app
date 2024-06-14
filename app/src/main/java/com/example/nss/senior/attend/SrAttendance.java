package com.example.nss.senior.attend;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nss.R;
import com.example.nss.model.Attendance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

//TODO: done with display, pdf remaining

public class SrAttendance extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 99;
    private static final Font FONT_EVENT     = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font FONT_DATE      = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);

    private static final Font FONT_CELL      = new Font(Font.FontFamily.TIMES_ROMAN,  12, Font.NORMAL);
    private static final Font FONT_COLUMN    = new Font(Font.FontFamily.TIMES_ROMAN,  14, Font.BOLDITALIC);
    RecyclerView recyclerView;
    ArrayList<Attendance> list;
    SwipeRefreshLayout swipetorefreshattendance;
    SrAttendAdapter srAttendAdapter;
    DatabaseReference databaseReferenceSched;
    FirebaseDatabase db;
    String[] userNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sr_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.AttendanceListSenior);
        db= FirebaseDatabase.getInstance();
        databaseReferenceSched = db.getReference("Schedule");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list=new ArrayList<>();
        srAttendAdapter = new SrAttendAdapter(getContext(),list);
        recyclerView.setAdapter(srAttendAdapter);
        swipetorefreshattendance=view.findViewById(R.id.swipetorefreshattendance);

        swipetorefreshattendance.setOnRefreshListener(() -> {
            databaseReferenceSched.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<Attendance> fetchedattend = new ArrayList<>();
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if(snapshot.exists() && snapshot.hasChildren()){
                                for(DataSnapshot shot : snapshot.getChildren()){
                                    if(shot.exists()){
                                        Attendance a = shot.getValue(Attendance.class);
                                        boolean isAttendExists = false;
                                        for (Attendance attendance : list) {
                                            if(attendance.getCount().equals("") && a.getCount().equals("")){
                                                attendance.setCount("0");
                                                a.setCount("0");
                                            }
                                            if (attendance.getEventDate().equals(a.getEventDate()) &&
                                                    attendance.getEventName().equals(a.getEventName()) &&
                                                    attendance.getCount().equals(a.getCount())) {
                                                isAttendExists = true;
                                                break;
                                            }
                                        }
                                        if (!isAttendExists) {
                                            fetchedattend.add(a);
                                        }
                                    }
                                }
                            }
                        }
                        list.addAll(fetchedattend);
                    } else {
                        list.clear();
                    }
                    srAttendAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            swipetorefreshattendance.setRefreshing(false);
        });

        SrAttendAdapter.setOnClickListener((position, Edate, Ename, count) -> databaseReferenceSched.child(Edate).child(Ename).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!checkPermission()) {
                    requestPermission();
                }
                if(snapshot.hasChild("count")){
                    DataSnapshot presentSnapshot = snapshot.child("Present");
                    if (presentSnapshot.exists()) {
                        int size = (int) presentSnapshot.getChildrenCount();
                        userNames = new String[size];
                        int i = 0;
                        for (DataSnapshot userSnapshot : presentSnapshot.getChildren()) {
                            userNames[i] = userSnapshot.getKey(); // Assuming user name is the key
                            i++;
                        }
                    }
                    if(userNames != null){
                        try {
                            generatePDF(Ename, Edate, userNames);
                        } catch (DocumentException e) {
                            Toast.makeText(getContext(), "Unsuccessful In Generating", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No Present Volunteers", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "Volunteers haven't been marked", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        databaseReferenceSched.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Attendance> fetchedattend = new ArrayList<>();
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(snapshot.exists() && snapshot.hasChildren()){
                            for(DataSnapshot shot : snapshot.getChildren()){
                                if(shot.exists()){
                                    Attendance a = shot.getValue(Attendance.class);
                                    boolean isAttendExists = false;
                                    for (Attendance attendance : list) {
                                        if(attendance.getCount() == null && a.getCount()==null){
                                            attendance.setCount("0");
                                            a.setCount("0");
                                        }
                                        if (attendance.getEventDate().equals(a.getEventDate()) &&
                                                 attendance.getEventName().equals(a.getEventName()) &&
                                                attendance.getCount().equals(a.getCount())) {
                                            isAttendExists = true;
                                            break;
                                        }
                                    }
                                    if (!isAttendExists) {
                                        fetchedattend.add(a);
                                    }
                                }
                            }
                        }
                    }
                    list.addAll(fetchedattend);
                } else {
                    list.clear();
                }
                srAttendAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        
    }

    private void generatePDF(String ename, String edate, String[] userNames) throws DocumentException {
        Document document=new Document();
        document.setMargins(24f,24f,32f,32f);
        document.setPageSize(PageSize.A4);
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{7,2});
        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;

        {
            /*LEFT TEXT*/
            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(8f);
            cell.setUseAscender(true);

            Paragraph temp = new Paragraph(ename ,FONT_EVENT);
            temp.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(temp);

            table.addCell(cell);
        }
        {
            /*MIDDLE TEXT*/
            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(8f);
            cell.setUseAscender(true);

            Paragraph temp = new Paragraph(edate ,FONT_DATE);
            temp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(temp);

            table.addCell(cell);
        }

        Paragraph paragraph=new Paragraph("",new Font(Font.FontFamily.TIMES_ROMAN, 2.0f, Font.NORMAL, BaseColor.BLACK));
        paragraph.add(table);

        PdfPTable table1 = new PdfPTable(2);
        table1.setWidths(new float[]{1f,2f});
        table1.setHeaderRows(1);
        table1.setHorizontalAlignment(Element.ALIGN_LEFT);
        {
            cell = new PdfPCell(new Phrase("Serial Number", FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase("Names", FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);
        }

        boolean alternate = false;

        BaseColor lt_gray = new BaseColor(221,221,221); //#DDDDDD
        BaseColor cell_color;

        int size;
        size = userNames.length;

        for (int i = 0; i < size; i++)
        {
            cell_color = alternate ? lt_gray : BaseColor.WHITE;
            String temp = userNames[i];

            cell = new PdfPCell(new Phrase(String.valueOf((i+1)), FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(cell_color);
            cell.setPadding(4f);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp, FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(cell_color);
            cell.setPadding(4f);
            table1.addCell(cell);

            alternate = !alternate;
        }

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(getFilePath(generateFileName(ename,edate))));
            writer.setFullCompression();
            document.open();
            document.add(paragraph);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(table1);
            document.close();
            writer.close();
            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public String generateFileName(String Ename, String Edate) {
        return Ename.split("\\s+")[0] + Edate.replaceAll("\\|", "-");
    }
    private String getFilePath(String Filename) {
        String folder = "NSS";
        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folder);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        // Create the file path
        File file = new File(downloadsDir, Filename);
        return file.getAbsolutePath();
    }

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(getContext(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}