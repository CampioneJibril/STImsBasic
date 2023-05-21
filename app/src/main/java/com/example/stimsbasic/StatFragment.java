package com.example.stimsbasic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StatFragment extends Fragment {

    private final FirebaseDatabase studentDatabase = FirebaseDatabase.getInstance("https://stims-ef107-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private final DatabaseReference root = studentDatabase.getReference();
    private final DatabaseReference attendanceRef = root.child("Attendance");
    private final DatabaseReference everySubjectsRef = root.child("Subjects");
    private final DatabaseReference everySectionsRef = root.child("Sections");

    Spinner spinner_subject_stat_fragment, spinnerStatFragSection;

    private MyAdapter adapter;
    private ArrayList<Model> list;
    ArrayList<String> subjectList;
    ArrayList<String> sectionList;

    DatabaseReference searchRootRef = FirebaseDatabase.getInstance("https://stims-ef107-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Attendance");

    String selectedSubject, selectedSection,dateRef, selectedSuggestion;

    MaterialButton btnStatFragEveryStudent,  btnCalendarViewHideCalendarScanFrag;
    SearchView search_view;
    CalendarView calendarView;
    Calendar calendar = Calendar.getInstance();

    TextView editTextInstructionStatFrag;


    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stat, container, false);

        spinner_subject_stat_fragment = v.findViewById(R.id.spinner_subject_stat_fragment);
        spinnerStatFragSection = v.findViewById(R.id.spinnerStatFragSection);

        calendarView = v.findViewById(R.id.calendarView);
        recyclerView = v.findViewById(R.id.recycler_view_);
        search_view = v.findViewById(R.id.search_view);

        btnCalendarViewHideCalendarScanFrag = v.findViewById(R.id.btnCalendarViewHideCalendarScanFrag);
        editTextInstructionStatFrag = v.findViewById(R.id.editTextInstructionStatFrag);

        list = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), list);
        subjectList = new ArrayList<>();
        sectionList = new ArrayList<>();

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        updateSubjectSpinner();
        updateSectionSpinner();


        btnCalendarViewHideCalendarScanFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideCalendarView();
            }
        });


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                long date = calendar.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy, MMMM, d,EEEE");
                dateRef = sdf.format(new Date(date));
                hideCalendarView();

                showRecyclerView();

            }

        });


        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(TextUtils.isEmpty(s)){
                    showRecyclerView();
                }

                MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "student_name"});
                DatabaseReference studentNameDatabase = FirebaseDatabase.getInstance("https://stims-ef107-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Suggestions");
                studentNameDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int i = 0;

                        if(dataSnapshot.exists()){
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String studentName = snapshot.getValue(String.class);
                                if (studentName.toLowerCase().startsWith(s.toLowerCase())) {
                                    cursor.addRow(new Object[]{i, studentName});
                                    Log.d("DataSnapshot", "Data added: " + studentName );
                                    i++;
                                }
                            }
                        }

                        SimpleCursorAdapter adapter3 = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, cursor,
                                new String[]{"student_name"}, new int[]{android.R.id.text1}, 0);
                        search_view.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                            @Override
                            public boolean onSuggestionSelect(int position) {
                                return false;
                            }
                            @Override
                            public boolean onSuggestionClick(int position) {
                                Cursor cursor = (Cursor) search_view.getSuggestionsAdapter().getItem(position);

                                selectedSuggestion = cursor.getString(cursor.getColumnIndex("student_name"));


                                DatabaseReference attendanceRef = searchRootRef.child(selectedSection).child(selectedSubject).child(dateRef).child(selectedSuggestion);

                                if(!TextUtils.isEmpty(dateRef)) {
                                    attendanceRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                list.clear();
                                                Model model = snapshot.getValue(Model.class);
                                                list.add(model);
                                                adapter.notifyDataSetChanged();
                                            }else{
                                                Toast.makeText(getActivity(), "NO DATA STORED", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {  }  });
                                }

                                //Column must not be 0
                                @SuppressLint("Range") String suggestion = cursor.getString(cursor.getColumnIndex("student_name"));
                                search_view.setQuery(suggestion, false);

                                return false;
                            }
                        });
                        search_view.setSuggestionsAdapter(adapter3);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                return false;
            }
        });
        return v;
    }

    public void showRecyclerView(){
        DatabaseReference attendanceRef = searchRootRef.child(selectedSection).child(selectedSubject).child(dateRef);
        if(!dateRef.isEmpty()){
            attendanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Model model = dataSnapshot.getValue(Model.class);
                        list.add(model);
                    }
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void hideBtnForCalendarFragment() {
        spinner_subject_stat_fragment.setVisibility(View.GONE);
        spinnerStatFragSection.setVisibility(View.GONE);
        search_view.setVisibility(View.GONE);
        btnStatFragEveryStudent.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        calendarView.setVisibility(View.GONE);
        editTextInstructionStatFrag.setVisibility(View.GONE);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void hideCalendarView(){
        int visibility = calendarView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            calendarView.setVisibility(View.VISIBLE);
            btnCalendarViewHideCalendarScanFrag.setText("HIDE CALENDAR");
            editTextInstructionStatFrag.setVisibility(View.VISIBLE);
        } else {
            calendarView.setVisibility(View.GONE);
            btnCalendarViewHideCalendarScanFrag.setText("SHOW CALENDAR");
            editTextInstructionStatFrag.setVisibility(View.GONE);
        }
    }

    public void hideBtnForSearchView(){
        btnCalendarViewHideCalendarScanFrag.setVisibility(View.GONE);
    }



    private void updateSubjectSpinner() {
        everySubjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String value = childSnapshot.getValue(String.class);
                    subjectList.add(value);
                }
                adapter.notifyDataSetChanged();
                if(isAdded()) {
                    ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, subjectList);
                    spinner_subject_stat_fragment.setAdapter(subjectAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {   }
        });
        spinner_subject_stat_fragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = spinner_subject_stat_fragment.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {   } });

    }

    private void updateSectionSpinner() {
        everySectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sectionList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String value = childSnapshot.getValue(String.class);
                    sectionList.add(value);
                }
                adapter.notifyDataSetChanged();
                if(isAdded()) {
                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, sectionList);
                    spinnerStatFragSection.setAdapter(sectionAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {   }
        });
        spinnerStatFragSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSection = spinnerStatFragSection.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {   } });
    }

    public void setSearchView(String searchInput){
        DatabaseReference searchRootRef = FirebaseDatabase.getInstance("https://stims-ef107-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Attendance");
        if(!TextUtils.isEmpty(dateRef)) {
            DatabaseReference attendanceRef = searchRootRef.child(selectedSection).child(selectedSubject).child(dateRef).child(searchInput);
            attendanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    Model model = snapshot.getValue(Model.class);
                    list.add(model);
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }}




}
