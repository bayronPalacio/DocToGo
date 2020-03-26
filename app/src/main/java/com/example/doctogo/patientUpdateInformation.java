package com.example.doctogo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class patientUpdateInformation extends AppCompatActivity {

    DatabaseHelper db = new DatabaseHelper(this);
    PatientInformationFragment updateFragment;
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_update_information);

        updateFragment = PatientInformationFragment.newInstance();
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment, updateFragment).commit();

        final EditText updateAddress = findViewById(R.id.upInfInputAddress);
        final EditText updateEmail = findViewById(R.id.upInfInputEmail);
        final EditText updatePhone = findViewById(R.id.upInfInputPhone);
        final EditText updateWeight = findViewById(R.id.upInfInputWeight);
        Button btnUpdateInfo = findViewById(R.id.btnClickUpdateInfo);

        SharedPreferences storage = getApplicationContext().getSharedPreferences("DOCTOGOSESSION", Context.MODE_PRIVATE);
        final int userID = storage.getInt("USERID",0);

        Cursor getInformation = db.getInformationUser(userID);
        try{

            if(getInformation.getCount() == 1){
                getInformation.moveToNext();
                updateAddress.setText(getInformation.getString(6));
                updateEmail.setText(getInformation.getString(7));
                updatePhone.setText(getInformation.getString(8));
                updateWeight.setText(getInformation.getString(10));
            }
        }catch (Exception e){
            Log.e("Query Get Info",e.getMessage());
        }

        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Query to update information
                Cursor updateInformation = db.updateInformationUser(userID,updateAddress.getText().toString(),updateEmail.getText().toString(),
                        updatePhone.getText().toString(),Integer.parseInt(updateWeight.getText().toString()));

                try{
                    if(updateInformation.getCount() == 1){
                        updateInformation.moveToNext();
                        updateAddress.setText(updateInformation.getString(6));
                        updateEmail.setText(updateInformation.getString(7));
                        updatePhone.setText(updateInformation.getString(8));
                        updateWeight.setText(updateInformation.getString(10));

                    }

                    PatientInformationFragment oldFragment = (PatientInformationFragment) manager.findFragmentById(R.id.fragment);
                        if(oldFragment != null){
                            FragmentTransaction trans = manager.beginTransaction();
                            trans.remove(updateFragment).commit();
                        }

                        updateFragment = PatientInformationFragment.newInstance();
                        FragmentTransaction trans1 = manager.beginTransaction();
                        trans1.add(R.id.fragment,updateFragment).commit();

                }catch (Exception e){
                    Log.e("Query Update",e.getMessage());
                }
            }
        });
    }
}
