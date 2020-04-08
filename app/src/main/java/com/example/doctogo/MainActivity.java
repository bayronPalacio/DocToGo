package com.example.doctogo;

/*
    Login functions by saving an eligible user to the storage.
    Then, start new page activity. New activity can then get the username of
    the user, then consult database to acquire data.
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final DatabaseHelper dbh = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Register button- go to registration activity
        Button registerButton = findViewById(R.id.btn_ToRegister);
        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(MainActivity.this, register.class);
                i.putExtra("register", 0);
                startActivity(i);
            }
        });

        Button loginButton = findViewById(R.id.btn_LogIn);
        loginButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //login function:
                //Check login info (username, password)
                EditText loginPass = findViewById(R.id.txt_LogInPassword);
                EditText loginUser = findViewById(R.id.txt_LogInUserName);
                String username = loginUser.getText().toString();
                String password = loginPass.getText().toString();

                //can find match?
                Cursor user = dbh.loginUser(username,password);
                if(user.getCount() == 1)
                {

                    user.moveToFirst();

                    //check if deactivated
                    if(user.getInt(2) == 1)
                    {
                        //if true, Toast + cancel
                        Toast.makeText(getBaseContext(),"Account deactivated. Cannot log in.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    //Save user id to preferences
                    SharedPreferences storage = getApplicationContext().getSharedPreferences("DOCTOGOSESSION",MODE_PRIVATE);
                    SharedPreferences.Editor memoryMaster = storage.edit();
                    memoryMaster.putInt("USERID",user.getInt(0));
                    memoryMaster.putString("USERNAME",username);
                    memoryMaster.putInt("USERROLE",user.getInt(1));
                    memoryMaster.commit();

                    //Start new activity, based on cursor role
                    int targetRole = user.getInt(1);
                    switch(targetRole)
                    {
                        case 1:
                            startActivity(new Intent(MainActivity.this, admin_main.class));
                            break;
                        case 2:
                            if(user.getInt(3)==1)
                            {
                                Intent i = new Intent(MainActivity.this, register.class);
                                i.putExtra("register", 1);
                                startActivity(i);
                            }
                            else {
                                startActivity(new Intent(MainActivity.this, patient_main.class));
                            }
                            break;
                        case 3:
                            startActivity(new Intent(MainActivity.this, doctor_main.class));
                            break;
                        case 4:
                            startActivity(new Intent(MainActivity.this, cashier_main.class));
                            break;
                        default:
                            break;
                    }
                }
                else if (user.getCount() > 1)
                { Toast.makeText(getBaseContext(), "ERROR: More than 1 user found.", Toast.LENGTH_LONG).show(); }
                else
                { Toast.makeText(getBaseContext(), "ERROR: Match not found.", Toast.LENGTH_LONG).show(); }

                //reset inputs
                loginPass.setText("");
                loginUser.setText("");

            }
        });
    }

}
