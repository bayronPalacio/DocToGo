package com.example.doctogo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class cashier_transactions_view extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private DatabaseHelper dbh;
    private DatabaseHelper dbh2;
    private int paymentId;
    private int patientId;
    private String due_Date;
    private int amount;
    private int tempAmount;
    private Date currDate;
    private String date;
    private String creditNum;
    private String expiryDate;
    //private int selectOptionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier_transactions_view);
        final TextView transNumberTxt = (TextView)findViewById(R.id.cashier_view_TranscationNumberTxt);
        final TextView dueTxt = (TextView)findViewById(R.id.cashier_view_dueDateTxt);
        final TextView patientTxt = (TextView)findViewById(R.id.cashier_view_patientNameTxt);
        final TextView amountTxt = (TextView)findViewById(R.id.cashier_view_amountTxt);
        final TextView mspTxt = (TextView)findViewById(R.id.cashier_view_mspTxt);
        final TextView creditTxt = (TextView)findViewById(R.id.cashier_view_creditTxt);
        final TextView expiryTxt = (TextView)findViewById(R.id.cashier_view_expiryTxt);
        //Button btn_Edit = (Button)findViewById(R.id.cashier_view_Edit);
        Button btn_reminder = (Button)findViewById(R.id.cashier_view_send_Reminder);
        Intent intent = getIntent();
        if(intent!=null) {
            paymentId = intent.getIntExtra("payment", 0);
            dbh = new DatabaseHelper(this);
            Cursor c = dbh.viewPayment(paymentId);
            if (c.getCount() > 0) {
                while(c.moveToNext()) {
                    patientId = c.getInt(0);
                    due_Date = c.getString(1);
                    amount = c.getInt(3);
                    creditNum = c.getString(5);
                    expiryDate = c.getString(6);
                }
                transNumberTxt.setText(Integer.toString(patientId));
                dueTxt.setText(due_Date);
                amountTxt.setText("$"+ amount);
                creditTxt.setText(creditNum);
                expiryTxt.setText(expiryDate);
                Cursor u = dbh.getInformationUser(patientId);
                if(u.getCount()>0){
                    while(u.moveToNext()){
                        String patientName = u.getString(4) + " " + u.getString(5);
                        patientTxt.setText(patientName);
                        String msp = u.getString(15);
                        if(msp == ""){
                            mspTxt.setText("n/a");
                        }
                        else {
                            mspTxt.setText(msp);
                        }
                    }
                }
            }
            btn_reminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbh2 = new DatabaseHelper(getBaseContext());
                    dbh2.setReminder(paymentId,1);
                    Toast.makeText(getBaseContext(),"Reminder sent to patient", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    public void Popup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.popup_menu_cashier);
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pop_due_date:
                        //selectOptionNumber = 1;
                        pickDate();
                        break;
                    case R.id.pop_amount:
                        editAmount();
                        break;
                    case R.id.pop_closing_transaction:
                        //selectOptionNumber = 3;
                        closing();
                        break;
                    case R.id.pop_decline:
                        decline();
                        break;
                    case R.id.pop_MSP_reCheck:
                        dbh = new DatabaseHelper(getBaseContext());
                        currDate = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        date = df.format(currDate);
                        dbh.updatePaymentWithtransDate(paymentId,date);
                        dbh.updatePaymentWithAmount(paymentId,0);
                        onBackPressed();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String datePicker = month + 1 + "-" + dayOfMonth + "-" + year;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        try {
            //if(selectOptionNumber == 1) {
            Date tempDate = dateFormat.parse(datePicker);
                currDate = Calendar.getInstance().getTime();
                if (!(tempDate.compareTo(currDate) > 0)) {
                    Toast.makeText(getBaseContext(), "please select the a due date later than today", Toast.LENGTH_SHORT).show();
                } else {
                    dbh = new DatabaseHelper(this);
                    dbh.updatePaymentWithdueDate(paymentId, datePicker);
                    finish();
                    startActivity(getIntent());
                }
           // }
            /*
            else if(selectOptionNumber == 3){
                tempDate = dateFormat.parse(datePicker);
                currDate = Calendar.getInstance().getTime();
                if (tempDate.compareTo(currDate) > 0) {
                    Toast.makeText(getBaseContext(), "please select the a due date not later than today", Toast.LENGTH_SHORT).show();
                } else {
                    dbh = new DatabaseHelper(this);
                    dbh.updatePaymentWithtransDate(paymentId, datePicker);
                    onBackPressed();
                }
            }

             */
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    private void pickDate(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void closing(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date();
        String curr = dateFormat.format(date);
        dbh = new DatabaseHelper(this);
            dbh.updatePaymentWithtransDate(paymentId, curr);
            onBackPressed();
    }

    private void decline(){
        dbh = new DatabaseHelper(this);
        dbh.updatePaymentWithPending(paymentId,2);
        finish();
        startActivity(getIntent());
    }
    private void editAmount(){
        dbh = new DatabaseHelper(this);
        AlertDialog.Builder editlog = new AlertDialog.Builder(cashier_transactions_view.this);
        editlog.setTitle("Enter new Amount");
        final EditText amountInput = new EditText(cashier_transactions_view.this);
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        editlog.setView(amountInput);
        editlog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String temp =amountInput.getText().toString();
                if (temp == ""|| temp.isEmpty()) {
                    Toast.makeText(cashier_transactions_view.this, "Please Enter valid amount", Toast.LENGTH_SHORT).show();
                } else {
                    tempAmount = Integer.parseInt(amountInput.getText().toString());
                    if (tempAmount >= 0) {
                        dbh.updatePaymentWithAmount(paymentId, tempAmount);
                        finish();
                        startActivity(getIntent());
                    } else {
                        Toast.makeText(cashier_transactions_view.this, "Please Enter valid amount", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        editlog.show();

    }
}
