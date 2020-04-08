package com.example.doctogo;

import androidx.appcompat.app.AppCompatActivity;
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

public class patientUpdateInformation extends AppCompatActivity {

    private DatabaseHelper db;
    private patientInformationFragment updateFragment;
    private FragmentManager manager;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_update_information);

        updateFragment = patientInformationFragment.newInstance();
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment, updateFragment).commit();

        final EditText updateAddress = findViewById(R.id.upInfInputAddress);
        final EditText updateCity = findViewById(R.id.upInfInputCity);
        final EditText updateEmail = findViewById(R.id.upInfInputEmail);
        final EditText updatePhone = findViewById(R.id.upInfInputPhone);
        final EditText updateWeight = findViewById(R.id.upInfInputWeight);
        final EditText updateMSP = findViewById(R.id.upInfInputMSP);
        Button btnUpdateInfo = findViewById(R.id.btnClickUpdateInfo);

        SharedPreferences storage = getApplicationContext().getSharedPreferences("DOCTOGOSESSION", Context.MODE_PRIVATE);
        userID = storage.getInt("USERID",0);
        db = new DatabaseHelper(this);
        Cursor getInformation = db.getInformationUser(userID);
        try{

            if(getInformation.getCount() == 1){
                getInformation.moveToNext();
                updateAddress.setText(getInformation.getString(6));
                updateEmail.setText(getInformation.getString(7));
                updateCity.setText(getInformation.getString(14));
                updatePhone.setText(getInformation.getString(8));
                updateWeight.setText(getInformation.getString(10));
                String x = getInformation.getString(15);
                if(getInformation.getString(15) == ""||getInformation.getString(15).isEmpty()){
                    updateMSP.setText("");
                }else {
                    updateMSP.setText(getInformation.getString(15));
                }
            }
        }catch (Exception e){
            Log.e("Query Get Info",e.getMessage());
        }

        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Query to update information

                String msp = updateMSP.getText().toString();
                db = new DatabaseHelper(getBaseContext());
                db.updateInformationUser(userID, updateAddress.getText().toString(),updateCity.getText().toString(),updateEmail.getText().toString(),
                        updatePhone.getText().toString(),Integer.parseInt(updateWeight.getText().toString()),msp);

                try{
                    patientInformationFragment oldFragment = (patientInformationFragment) manager.findFragmentById(R.id.fragment);
                        if(oldFragment != null){
                            FragmentTransaction trans = manager.beginTransaction();
                            trans.remove(updateFragment).commit();
                        }

                        updateFragment = patientInformationFragment.newInstance();
                        FragmentTransaction trans1 = manager.beginTransaction();
                        trans1.add(R.id.fragment,updateFragment).commit();

                }catch (Exception e){
                    Log.e("Query Update",e.getMessage());
                }
                onBackPressed();
            }
        });
    }
}
