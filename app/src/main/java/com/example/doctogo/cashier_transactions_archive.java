package com.example.doctogo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class cashier_transactions_archive extends AppCompatActivity {
    private String transactionDate;
    private int patientId;
    private int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier_transactions_archive);
        final TextView transactionTxt = (TextView)findViewById(R.id.cashier_archive_transIDTxt);
        final TextView transDateTxt = (TextView)findViewById(R.id.cashier_archive_transDateTxt);
        final TextView patientTxt = (TextView)findViewById(R.id.cashier_archive_patientTxt);
        final TextView addressTxt = (TextView)findViewById(R.id.cashier_archive_addressTxt);
        final TextView amountTxt = (TextView)findViewById(R.id.cashier_archive_amountTxt);
        final TextView mspTxt = (TextView)findViewById(R.id.cashier_archive_mspTxt);
        Intent intent = getIntent();
        if(intent!=null) {
            int transactionId = intent.getIntExtra("payment", 0);
            transactionTxt.setText(Integer.toString(transactionId));
            DatabaseHelper dbh = new DatabaseHelper(this);
            Cursor c = dbh.viewPayment(transactionId);
            if (c.getCount() > 0) {
                while(c.moveToNext()) {
                    patientId = c.getInt(0);
                    transactionDate = c.getString(2);
                    amount = c.getInt(3);
                }
                transDateTxt.setText(transactionDate);
                amountTxt.setText("$"+ amount);
                Cursor u = dbh.getInformationUser(patientId);
                if(u.getCount()>0){
                    while(u.moveToNext()){
                        String patientName = u.getString(4) + " " + u.getString(5);
                        patientTxt.setText(patientName);
                        String address = u.getString(6) + ", " + u.getString(14);
                        patientTxt.setText(patientName);
                        addressTxt.setText(address);
                        String msp = u.getString(15);
                        if(msp == ""||msp.isEmpty() ){
                            mspTxt.setText("n/a");
                        }
                        else {
                            mspTxt.setText(msp);
                        }
                    }
                }
            }
        }
    }
}
