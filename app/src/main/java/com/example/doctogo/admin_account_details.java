package com.example.doctogo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class admin_account_details extends AppCompatActivity
{
    DatabaseHelper dbh = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__account_details);

        //unpack extras, get account ID.
        int targetID = getIntent().getExtras().getInt("targetID",0);
        boolean targetDeactive = getIntent().getExtras().getBoolean("targetDeactive",false);

        //put id into function to get data
        getAccountData(targetID);

        //button functions
        Button btn_Save = findViewById(R.id.btn_AdminDetailAccountSave);
        Button btn_Revert = findViewById(R.id.btn_AdminDetailAccountRevert);
        Button btn_Quit = findViewById(R.id.btn_AdminDetailAccountExit);
        Button btn_Delete = findViewById(R.id.btn_AdminDetailAccountDelete);

        //button revert: reload data
        btn_Revert.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int targetID = getIntent().getExtras().getInt("targetID",0);
                getAccountData(targetID);
            }
        });

        //button quit: does not save changes + finish
        btn_Quit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        //button save: update database with info.
        btn_Save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //get info
                int targetID = getIntent().getExtras().getInt("targetID",0);
                TextView accUsername = findViewById(R.id.txt_AdminDetailAccountUsername);
                TextView accEmail = findViewById(R.id.txt_AdminDetailAccountEmail);
                TextView accPassword = findViewById(R.id.txt_AdminDetailAccountPassword);
                TextView accFirstName = findViewById(R.id.txt_AdminDetailAccountFirstname);
                TextView accLastName = findViewById(R.id.txt_AdminDetailAccountLastname);
                TextView accAddress = findViewById(R.id.txt_AdminDetailAccountAddress);
                TextView accCity = findViewById(R.id.txt_AdminDetailAccountCity);
                TextView accPhone = findViewById(R.id.txt_AdminDetailAccountPhone);
                Spinner accGender = findViewById(R.id.spn_AdminDetailAccountGender);
                TextView accAge = findViewById(R.id.txt_AdminDetailAccountAge);
                TextView accWeight = findViewById(R.id.txt_AdminDetailAccountWeight);
                TextView accMSP = findViewById(R.id.txt_AdminDetailAccountMSP);
                TextView accQualifications = findViewById(R.id.txt_AdminDetailAccountQualifications);

                //validation on required entries
                String username = accUsername.getText().toString();
                String password = accPassword.getText().toString();
                String email = accEmail.getText().toString();
                if(username.isEmpty() || password.isEmpty() || email.isEmpty())
                {
                    //if one of the req fields are empty, end the op + toast message
                    Toast.makeText(getBaseContext(),"One or more required fields are empty.",Toast.LENGTH_LONG).show();
                    return;
                }
                String firstname = accFirstName.getText().toString();
                String lastname = accLastName.getText().toString();
                String address = accAddress.getText().toString();
                String city = accCity.getText().toString();
                String phone = accPhone.getText().toString();
                String gender = accGender.getSelectedItem().toString();
                String age = accAge.getText().toString();
                String weight = accWeight.getText().toString();
                String qualifications = accQualifications.getText().toString();
                String MSP = accMSP.getText().toString();
                if(MSP.isEmpty()){
                    MSP = "";
                }
                //send info to db
                boolean success = dbh.adminUpdate(targetID,username,password,email,firstname,lastname,address,city,phone,Integer.parseInt(weight),qualifications,gender,Integer.parseInt(age),MSP);

                //success/fail messages
                if(success)
                {Toast.makeText(getBaseContext(), "Account updated.",Toast.LENGTH_LONG).show();}
                else
                {
                    Toast.makeText(getBaseContext(), "Account failed to update.",Toast.LENGTH_LONG).show();
                    getAccountData(targetID);
                }
            }
        });

        //if deactivated, interface has difference
        if(targetDeactive)
        {
            //border color => red
            NestedScrollView scrl = findViewById(R.id.scl_AdminDetailAccount);
            scrl.setBackgroundColor(Color.RED);
            //button => reactivate
            btn_Delete.setText("Reactivate Account");
            btn_Delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //get ID
                    int targetID = getIntent().getExtras().getInt("targetID",0);
                    //deactivate ID account
                    dbh.adminReactivate(targetID);
                    //finish
                    finish();
                }
            });
        }
        else
        {
            //button delete: remove account
            btn_Delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //get ID
                    int targetID = getIntent().getExtras().getInt("targetID",0);
                    //deactivate ID account
                    dbh.adminDeactivate(targetID);
                    //finish
                    finish();
                }
            });
        }


    }

    private void getAccountData(int targetID)
    {
        //get account data from db with ID.
        Cursor data = dbh.getInformationUser(targetID);

        //get controls
        TextView accID = findViewById(R.id.lbl_AdminDetailAccountID);
        TextView accRole = findViewById(R.id.lbl_AdminDetailAccountRole);
        TextView accUsername = findViewById(R.id.txt_AdminDetailAccountUsername);
        TextView accEmail = findViewById(R.id.txt_AdminDetailAccountEmail);
        TextView accPassword = findViewById(R.id.txt_AdminDetailAccountPassword);
        TextView accFirstName = findViewById(R.id.txt_AdminDetailAccountFirstname);
        TextView accLastName = findViewById(R.id.txt_AdminDetailAccountLastname);
        TextView accAddress = findViewById(R.id.txt_AdminDetailAccountAddress);
        TextView accCity = findViewById(R.id.txt_AdminDetailAccountCity);
        TextView accPhone = findViewById(R.id.txt_AdminDetailAccountPhone);
        Spinner accGender = findViewById(R.id.spn_AdminDetailAccountGender);
        TextView accAge = findViewById(R.id.txt_AdminDetailAccountAge);
        TextView accWeight = findViewById(R.id.txt_AdminDetailAccountWeight);
        TextView accMSP = findViewById(R.id.txt_AdminDetailAccountMSP);
        TableRow rowQualifications = findViewById(R.id.row_AdminDetailAccountQualifications);
        TextView accQualifications = findViewById(R.id.txt_AdminDetailAccountQualifications);

        //display data (if any) to controls
        if(data.getCount()>0)
        {
            data.moveToFirst();
            accID.setText(data.getString(0));
            switch (data.getInt(3))
            {
                case 1:
                    accRole.setText("Admin");
                    break;
                case 2:
                    accRole.setText("Patient");
                    break;
                case 3:
                    accRole.setText("Doctor");
                    //enable qualifications visual
                    rowQualifications.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    accRole.setText("Cashier");
                    break;
            }
            accUsername.setText(data.getString(1));
            accPassword.setText(data.getString(2));
            accFirstName.setText(data.getString(4));
            accLastName.setText(data.getString(5));
            accAddress.setText(data.getString(6));
            accCity.setText(data.getString(14));
            accEmail.setText(data.getString(7));
            accPhone.setText(data.getString(8));
            accQualifications.setText(data.getString(9));
            accWeight.setText( String.valueOf(data.getInt(10)));
            accMSP.setText(data.getString(15));

            if(data.getString(11) != null)
            {
                switch (data.getString(11)) {
                    case "Male":
                        accGender.setSelection(0);
                        break;
                    case "Female":
                        accGender.setSelection(1);
                        break;
                    case "Other":
                        accGender.setSelection(2);
                        break;
                    default:
                        accGender.setSelection(3);
                        break;
                }
            }
            else
            {accGender.setSelection(3);}
            accAge.setText(String.valueOf(data.getInt(12)));
        }
        else
        {
            //error in db getdata.
            Toast.makeText(getBaseContext(), "Error in getting DB account data.", Toast.LENGTH_LONG).show();
        }
    }
}
