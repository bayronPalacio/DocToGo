package com.example.doctogo;

/*
*   This is the registration page, accessed from from mainactivity login.
*   currently, user can create ALL accounts (including admin), which is a security risk.
*   Doctor's specialization can be set once they log in as doctor -> edit self.
*/

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class register extends AppCompatActivity
{
    private DatabaseHelper dbh;
    private int register;
    private int userID;
    private SharedPreferences storage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        if(intent!=null) {
            register = intent.getIntExtra("register", 0);
        }
        //input fields
        final EditText ETUserName = findViewById(R.id.txt_RegisterUsername);
        final EditText ETPassword = findViewById(R.id.txt_RegisterPassword);
        final EditText ETEmail = findViewById(R.id.txt_RegisterEmail);
        final EditText ETFirstName = findViewById(R.id.txt_RegisterFirstname);
        final EditText ETLastName = findViewById(R.id.txt_RegisterLastname);
        final EditText ETPhone = findViewById(R.id.txt_RegisterPhone);
        final EditText ETAddress = findViewById(R.id.txt_RegisterAddress);
        final EditText ETCity = findViewById(R.id.txt_RegisterCity);
        final EditText ETAge = findViewById(R.id.txt_RegisterAge);
        final EditText ETWeight = findViewById(R.id.txt_RegisterWeight);
        final EditText ETMSP = findViewById(R.id.txt_RegisterMSP);
        final Spinner SPRole = findViewById(R.id.spn_RegisterRole);
        final Spinner SPRoleFirstlog = findViewById(R.id.spn_RegisterRoleFirstlogin);
        final Spinner SPGender = findViewById(R.id.spn_RegisterGender);

        if(register == 1){
            storage = getSharedPreferences("DOCTOGOSESSION", Context.MODE_PRIVATE);
            userID = storage.getInt("USERID", 0);
            dbh = new DatabaseHelper(this);
            Cursor c = dbh.getInformationUser(userID);
            if(c.getCount()>0){
                while(c.moveToNext()) {
                    SPRole.setVisibility(View.GONE);
                    SPRoleFirstlog.setVisibility(View.VISIBLE);
                    SPRoleFirstlog.setEnabled(false);
                    SPRoleFirstlog.setClickable(false);

                    ETUserName.setText(c.getString(1));
                    ETUserName.setFocusable(false);
                    ETUserName.setEnabled(false);
                    ETUserName.setCursorVisible(false);
                    ETUserName.setKeyListener(null);
                    ETUserName.setBackgroundColor(Color.TRANSPARENT);
                    ETPassword.setText(c.getString(2));
                    ETEmail.setText(c.getString(7));
                    SPRoleFirstlog.setSelection(c.getInt(3)-1);
                }

            }
        }

        //cancel button: return to login
        Button btn_RegisterCancel = findViewById(R.id.btn_RegisterCancel);
        btn_RegisterCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        //register button
        Button btn_Register = findViewById(R.id.btn_Register);
        btn_Register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //validation on required entries
                String username = ETUserName.getText().toString();
                String password = ETPassword.getText().toString();
                String email = ETEmail.getText().toString();
                if(username.isEmpty() || password.isEmpty() || email.isEmpty())
                {
                    //if one of the req fields are empty, end the op + toast message
                    Toast.makeText(getBaseContext(),"One or more required fields are empty.",Toast.LENGTH_LONG).show();
                    return;
                }

                //get the rest of the entries
                String firstname = ETFirstName.getText().toString();
                String lastname = ETLastName.getText().toString();
                String phone = ETPhone.getText().toString();
                String address = ETAddress.getText().toString();
                String city = ETCity.getText().toString();
                String strAge = (ETAge.getText().toString());
                String strWeight = (ETWeight.getText().toString());
                String strMSP = (ETMSP.getText().toString());
                int weight = 0;
                if(!strWeight.isEmpty())
                {weight = Integer.parseInt(strWeight);}
                int age = 0;
                if(!strAge.isEmpty())
                {age = Integer.parseInt(strAge);}
                int role;

                switch (SPRole.getSelectedItem().toString())
                {
                    case "Admin":
                        role = 1;
                        break;
                    case "Patient":
                        role = 2;
                        break;
                    case "Doctor":
                        role = 3;
                        break;
                    case "Cashier":
                        role = 4;
                        break;
                    default:
                        Toast.makeText(getBaseContext(), "Error in roles: invalid value", Toast.LENGTH_LONG).show();
                        return;
                }


                String gender = SPGender.getSelectedItem().toString();
                boolean success;
                if(register == 1){
                    success = dbh.updateFirstLoginUser(userID,password, firstname, lastname,address, city, phone, weight, gender, age, strMSP);
                }
                else {
                    //make database insert operation
                    success = dbh.normalRegister(username, password, role, email, firstname, lastname, address, city, phone, weight, gender, age, strMSP);
                }
                //return to main login
                if(success)
                {finish();}
                else
                {Toast.makeText(getBaseContext(),"Error in database insertion.",Toast.LENGTH_LONG).show();}
            }
        });
    }
}
