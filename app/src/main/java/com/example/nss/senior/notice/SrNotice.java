package com.example.nss.senior.notice;

import android.app.AlertDialog;
import android.app.Dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.example.nss.model.Notice;
import com.example.nss.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SrNotice extends Fragment {
    FloatingActionButton addNotesFabBtn;
    SwipeRefreshLayout swipetorefersh;
    TextView recnottit,recnotdescr;
    SearchView search_notes;
    RecyclerView recyclerView;
    ArrayList<Notice> list;
    NotRecAdapter notRecAdapter;
    FirebaseDatabase db;
    DatabaseReference databaseReference;
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[.#$\\[\\]]");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sr_notice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.NoticeListSenior);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list=new ArrayList<>();
        notRecAdapter = new NotRecAdapter(getContext(),list);
        recyclerView.setAdapter(notRecAdapter);
        db=FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Notice");
        recnottit=view.findViewById(R.id.recnottit);
        recnotdescr=view.findViewById(R.id.recnotdescr);
        search_notes=view.findViewById(R.id.search_notes);
        addNotesFabBtn=view.findViewById(R.id.addNotesFabBtn);
        swipetorefersh=view.findViewById(R.id.swipetorefersh);

        swipetorefersh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
                    notRecAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            swipetorefersh.setRefreshing(false);
        },1500));

        search_notes.clearFocus();

        search_notes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        NotRecAdapter.setOnClickListener(new NotRecAdapter.NoticeOnitemClickListener() {
            @Override
            public void DeleteNotClick(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to delete this?")
                        .setPositiveButton("Yes", (dialog, id) -> {
                            String title = list.get(position).getTitle();
                            String NodeTitle=SPECIAL_CHARS_PATTERN.matcher(title).replaceAll("_");
                            databaseReference.child(NodeTitle).removeValue().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    notRecAdapter.notifyItemRemoved(position);
                                    Toast.makeText(getContext(),"Successfully Deleted",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getContext(),"Failed",Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", (dialog, id) -> Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show());
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void EditNotClick(int position, String title, String description) {
                showEditDialog(title, description,true);
            }
        });

        addNotesFabBtn.setOnClickListener(v -> showEditDialog("","",false));

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
                notRecAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void showEditDialog(String title, String desc, boolean isEditing) {

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_add_notes);

        EditText edtTitle = dialog.findViewById(R.id.edttitle);
        EditText edtDescription= dialog.findViewById(R.id.edtdescription);

        edtTitle.setText(title);
        edtDescription.setText(desc);

        if (!title.isEmpty()){
            edtTitle.setInputType(InputType.TYPE_NULL);
            edtTitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        edtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Intentially left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Intentially left blank
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (SPECIAL_CHARS_PATTERN.matcher(edtTitle.getText().toString()).find()) {
                    // If it matches, display an error message
                    edtTitle.setError("Title should not contain special characters like .#$[]");
                } else {
                    // If it doesn't match, clear any previous error message
                    edtTitle.setError(null);
                }
            }
        });

        ImageButton savebtnfornotes=dialog.findViewById(R.id.savebtnfornotes);

        savebtnfornotes.setOnClickListener(v -> {
            String StTitle = edtTitle.getText().toString();
            String StDescription = edtDescription.getText().toString();
            String NodeTitle= title.isEmpty() ? StTitle : title;

            if (!StTitle.isEmpty() && !StDescription.isEmpty()) {
                // Check if the item already exists in the database

                databaseReference.child(NodeTitle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (isEditing && dataSnapshot.exists()) {
                            // Update the item in the database
                            dataSnapshot.getRef().child("title").setValue(StTitle);
                            dataSnapshot.getRef().child("description").setValue(StDescription).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Successfully Updated", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Failed to Update note", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (!dataSnapshot.exists()) {
                            // Add the item to the database
                            Notice notices = new Notice(StTitle, StDescription);
                            databaseReference.child(NodeTitle).setValue(notices).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Failed to Add note", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Note with this name already exists", Toast.LENGTH_SHORT).show();
                        }
                        notRecAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please fill both the fields", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void filterlist(String newText) {
        ArrayList<Notice> filteredlist = new ArrayList<>();
        for(Notice notice: list){
            if(notice.getTitle().toLowerCase().contains(newText.toLowerCase())){
                filteredlist.add(notice);
            }
        }
        if(!filteredlist.isEmpty()){
            notRecAdapter.setFilteredList(filteredlist);
        }
    }


}
