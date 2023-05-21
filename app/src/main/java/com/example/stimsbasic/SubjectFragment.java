package com.example.stimsbasic;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SubjectFragment extends Fragment {

    private final FirebaseDatabase studentDatabase = FirebaseDatabase.getInstance("https://stims-ef107-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference root = studentDatabase.getReference();
    DatabaseReference subjectsRef = studentDatabase.getReference("Subjects");
    DatabaseReference sectionsRef = root.child("Sections");

    EditText edit_text_subject_add, editTextSubjectAddSection;
    MaterialButton btn_add_subjects, btnSubjectAddSection, btn_delete_subjects, btnSubjectDeleteSection,btn_exit_subject;
    Spinner spinner_subjects, spinnerSubjectSections;

    ArrayList<String> subjectList = new ArrayList<>();
    ArrayList <String> violationList = new ArrayList<>();

    String selectedSubject, selectedSections;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subject, container, false);

        edit_text_subject_add = v.findViewById(R.id.edit_text_subject_add);
        editTextSubjectAddSection = v.findViewById(R.id.editTextSubjectAddSection);

        btn_exit_subject = v.findViewById(R.id.btn_exit_subject);
        btn_add_subjects = v.findViewById(R.id.btn_add_subjects);
        btnSubjectAddSection = v.findViewById(R.id.btnSubjectAddSection);
        btn_delete_subjects = v.findViewById(R.id.btn_delete_subjects);
        btnSubjectDeleteSection = v.findViewById(R.id.btnSubjectDeleteSection);


        btn_exit_subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFragment(new ScanFragment());
            }
        });
        btn_add_subjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSubject(edit_text_subject_add);
            }
        });
        btnSubjectAddSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSection(editTextSubjectAddSection);
            }
        });
        btn_delete_subjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSubject(edit_text_subject_add);
            }
        });
        btnSubjectDeleteSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSection(editTextSubjectAddSection);
            }
        });

        spinner_subjects = v.findViewById(R.id.spinner_subjects);
        spinnerSubjectSections = v.findViewById(R.id.spinnerSubjectSections);




        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateSubjectSpinner(dataSnapshot);

                if(isAdded()) {
                    ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, subjectList);
                    spinner_subjects.setAdapter(subjectAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {   }});

        spinner_subjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = spinner_subjects.getSelectedItem().toString();
                edit_text_subject_add.setText(selectedSubject);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }});

        sectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateViolationSpinner(dataSnapshot);

                if(isAdded()) {
                    ArrayAdapter<String> violationAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, violationList);
                    spinnerSubjectSections.setAdapter(violationAdapter);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {   }
        });

        spinnerSubjectSections.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSections = spinnerSubjectSections.getSelectedItem().toString();
                editTextSubjectAddSection.setText(selectedSections);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        return v;
    }

    public void removeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(this);
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

    public void addSection(EditText editText) {
        String sectionToAdd = editText.getText().toString();
        if(!sectionToAdd.isEmpty()) {
            sectionsRef.child(sectionToAdd).setValue(sectionToAdd).addOnSuccessListener(unused -> Toast.makeText(getActivity(), "Section Added",
                    Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "Please Enter Section", Toast.LENGTH_SHORT).show();
        }
    }
    public void addSubject(EditText editText) {
        String subjectsToAdd = editText.getText().toString();
        if(!subjectsToAdd.isEmpty()) {
            subjectsRef.child(subjectsToAdd).setValue(subjectsToAdd).addOnSuccessListener(unused -> Toast.makeText(getActivity(), "Subject Added",
                    Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "Please Enter Subject", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSubject(EditText editText) {
        String subjectToDelete = editText.getText().toString();
        DatabaseReference subjectsRootRef = root.child("Subjects");
        DatabaseReference selectedSubjectsRef = subjectsRootRef.child(subjectToDelete);
        selectedSubjectsRef.removeValue().addOnSuccessListener(unused -> Toast.makeText(getActivity(), "Subject Deleted",
                Toast.LENGTH_SHORT).show());
    }
    public void deleteSection(EditText editText) {
        String sectionToDelete = editText.getText().toString();
        DatabaseReference selectedSectionRef = sectionsRef.child(sectionToDelete);
        selectedSectionRef.removeValue().addOnSuccessListener(unused -> Toast.makeText(getActivity(), "Section Removed",
                Toast.LENGTH_SHORT).show());
    }

    public void updateSubjectSpinner(DataSnapshot dataSnapshot) {
        subjectList.clear();
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            String value = childSnapshot.getValue(String.class);
            subjectList.add(value);
        }
    }
    public void updateViolationSpinner(DataSnapshot dataSnapshot) {
        violationList.clear();
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            String value = childSnapshot.getValue(String.class);
            violationList.add(value);
        }
    }
}