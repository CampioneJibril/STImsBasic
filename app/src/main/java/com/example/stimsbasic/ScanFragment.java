package com.example.stimsbasic;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ScanFragment extends Fragment {

    //Initialize variable
    String date = new SimpleDateFormat("yyyy, MMMM, d,EEEE", Locale.getDefault()).format(new Date());
    String time = new SimpleDateFormat("h:mm:a", Locale.getDefault()).format(new Date());
    String nameModel, selectedSubject, selectedSection, userDataName;
    Button btn_scan, btn_subjects;
    Spinner spinner_subjects, spinnerScanFragSection;
    TextView text_view_subject_selected, textViewScanFragSectionSelected;

    ArrayList<String> subjectList = new ArrayList<>();

    ArrayList<String> sectionList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();

    //Initialize FirebaseDatabase
    private final FirebaseDatabase studentDatabase = FirebaseDatabase.getInstance("https://stims-ef107-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private final DatabaseReference root = studentDatabase.getReference();

    DatabaseReference everySubjectRef = root.child("Subjects");
    DatabaseReference everySectionRef = root.child("Sections");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan, container, false);


        text_view_subject_selected = v.findViewById(R.id.text_view_subject_selected);
        textViewScanFragSectionSelected = v.findViewById(R.id.textViewScanFragSectionSelected);

        spinner_subjects = v.findViewById(R.id.spinner_subjects);
        spinnerScanFragSection = v.findViewById(R.id.spinnerScanFragSection);

        btn_subjects = v.findViewById(R.id.btn_subjects);
        btn_scan = v.findViewById(R.id.btn_scan);


        everySubjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateSubjectSpinner(dataSnapshot);
                if (isAdded()) {
                    ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, subjectList);
                    spinner_subjects.setAdapter(subjectAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        spinner_subjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = spinner_subjects.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        everySectionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateSectionSpinner(dataSnapshot);
                if (isAdded()) {
                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, sectionList);
                    spinnerScanFragSection.setAdapter(sectionAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        spinnerScanFragSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSection = spinnerScanFragSection.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
                result -> {
                    result.getClass();
                    String scanResult = result.getContents();
                    if (TextUtils.isEmpty(scanResult)) {
                        Toast.makeText(getActivity(), "NOTHING SCANNED", Toast.LENGTH_SHORT).show();
                    } else {

                        openAlertDialogAfterScan(scanResult);
                        checkUserOnDatabase(scanResult);
                    }

                });

        btn_scan.setOnClickListener(view -> {
            barcodeLauncher.launch(new ScanOptions());
            startScan();
        });

        btn_subjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new SubjectFragment());
                hideButtons();
            }
        });

        return v;
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void hideButtons() {
        btn_scan.setVisibility(View.GONE);
        btn_subjects.setVisibility(View.GONE);
        spinnerScanFragSection.setVisibility(View.GONE);
        spinner_subjects.setVisibility(View.GONE);
        text_view_subject_selected.setVisibility(View.GONE);
        textViewScanFragSectionSelected.setVisibility(View.GONE);
    }

    public void updateSubjectSpinner(DataSnapshot dataSnapshot) {
        subjectList.clear();
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            String value = childSnapshot.getValue(String.class);
            subjectList.add(value);
        }
    }

    public void updateSectionSpinner(DataSnapshot dataSnapshot) {
        sectionList.clear();
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            String value = childSnapshot.getValue(String.class);
            sectionList.add(value);
        }
    }

    public void checkInData(DatabaseReference databaseReferenceRef, String date, String check_in, String name) {
        databaseReferenceRef.child("Date").setValue(date);
        databaseReferenceRef.child("Check_In").setValue(check_in);
        databaseReferenceRef.child("Name").setValue(name);
        Toast.makeText(getActivity(), "Checked In", Toast.LENGTH_SHORT).show();

    }

    public void checkOutData(DatabaseReference databaseReferenceRef, String time) {
        databaseReferenceRef.child("Check_Out").setValue(time).addOnSuccessListener(unused -> Toast.makeText(getActivity(), "Check Out Successfully", Toast.LENGTH_SHORT).show());
        Toast.makeText(getActivity(), "Checked Out ", Toast.LENGTH_SHORT).show();

    }

    public void startScan() {
        Toast.makeText(getActivity(), "For Flash use Volume up Key", Toast.LENGTH_SHORT).show();
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(Capture.class);
    }

    public void checkStudentInAndOut(String scanResult) {
        DatabaseReference attendanceRootRef = root.child("Attendance").child(selectedSection).child(selectedSubject).child(date).child(scanResult);
        attendanceRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Check_In").exists()) {
                    checkOutData(attendanceRootRef, time);
                } else {
                    checkInData(attendanceRootRef, date, time, scanResult);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void openAlertDialogAfterScan(String scanResult){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(scanResult);
        builder.setCancelable(false);
        builder.setMessage("Selected Section: " + selectedSection + "\n" + "Selected Subject: " + selectedSubject);
        builder.setPositiveButton("CANCEL", (dialogInterface, i) -> {
            Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
            dialogInterface.dismiss();
        });

        builder.setNegativeButton("CHECK IN/OUT", (dialogInterface, i) -> {
            checkStudentInAndOut(scanResult);
        });
        builder.show();
    }

    public void checkUserOnDatabase(String name){
        DatabaseReference suggestionsRef = root.child("Suggestions");
        suggestionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                nameList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String nameStored = snapshot.getValue(String.class);
                    nameList.add(nameStored);
                }

                if(!nameList.contains(name)){
                    suggestionsRef.push().setValue(name);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}