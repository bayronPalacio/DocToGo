package com.example.doctogo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 7;
    //DATABASE TABLES & COLUMNS
    private static final String DATABASE_NAME = "DocToGoDatabase .db";
    /*
        Users Table: defines all users in the system.
        role (integer) column defines the roles the user is:
            1 = Admin
            2 = Patient
            3 = Doctor
            4 = Cashier
        Anything else other than specified is an error and must be addressed.
        Patients has the most info- all users too have these columns, which may be useless
        (ex: Cashier with age), so most of these columns can be null. However, accounts requiring
        data must be enforced programmatically.
    */
    //TABLE 1
    private final static String TABLE1_NAME = "Users";
    private final static String T1COL_1 = "ID";              //INTEGER PRIMARY KEY
    private final static String T1COL_2 = "Username";        //TEXT
    private final static String T1COL_3 = "Password";        //TEXT
    private final static String T1COL_4 = "Role";            //INTEGER
    private final static String T1COL_5 = "Firstname";       //TEXT
    private final static String T1COL_6 = "Lastname";        //TEXT
    private final static String T1COL_7 = "Address";         //TEXT
    private final static String T1COL_8 = "Email";           //TEXT
    private final static String T1COL_9 = "Phone";           //TEXT
    private final static String T1COL_10 = "Qualifications"; //TEXT        //Doctors Only REQUIRED
    private final static String T1COL_11 = "Weight";         //INTEGER
    private final static String T1COL_12 = "Gender";         //TEXT        //if Gender not selected, UNDEFINED
    private final static String T1COL_13 = "Age";            //INTEGER     //Age of 0 MUST become "N/A"
    private final static String T1COL_14 = "FirstLogin";     //INTEGER     //flag, turn to 1 if admin register->have to modify self.
    private final static String T1COL_15 = "City";           //TEXT
    private final static String T1COL_16 = "MSP";            //TEXT
    private final static String T1COL_17 = "Deactivated";    //INTEGER     //flag, 0 => active, 1 => deactivated

    /*
        Appointment table: Defines a patient (role 2) having a schedule with a doctor (role 3).
        Due to structure, the appointment's foreign keys referring to the actors will both refer to
        the same users table. Thus, when using inner join, use aliases to refer to these actors
        (eg: SELECT p.id as patient, d.id as doctor FROM Appointments
             INNER JOIN Users p ON Appointments.Patient_ID = p.id
             INNER JOIN Users d ON Appointments.Doctor_ID = p.id;)
     */
    //TABLE 2
    private final static String TABLE2_NAME = "Appointments";
    private final static String T2CONST_1 = "FK_patient";
    private final static String T2CONST_2 = "FK_doctor";
    private final static String T2COL_1 = "Appointment_ID";   //INTEGER PRIMARY KEY
    private final static String T2COL_2 = "Patient_ID";       //INTEGER FOREIGN KEY
    private final static String T2COL_3 = "Doctor_ID";        //INTEGER FOREIGN KEY
    private final static String T2COL_4 = "Schedule_Date";    //TEXT YYYY-MM-DD HH:MM:SS.SSS
    private final static String T2COL_5 = "Duration";         //INTEGER (duration in minutes)
    private final static String T2COL_6 = "Report_ID";        //INTEGER FOREIGN KEY

    /*
        Payments table: Defines payments made by patiens (role 2).
        Transdate, if not null, means that payment has been made at that date.
        If Transdate is null, payment has not been made.
     */
    //TABLE 3
    private final static String TABLE3_NAME = "Payments";
    private final static String T3CONST_1 = "FK_report";
    private final static String T3CONST_2 = "FK_patient";
    private final static String T3COL_1 = "Payment_ID";   //INTEGER PRIMARY KEY
    private final static String T3COL_2 = "Patient_ID";   //INTEGER FOREIGN KEY
    private final static String T3COL_3 = "Due_Date";     //TEXT YYYY-MM-DD HH:MM:SS.SSS
    private final static String T3COL_4 = "Trans_Date";   //TEXT YYYY-MM-DD HH:MM:SS.SSS
    private final static String T3COL_5 = "Amount";       //REAL
    private final static String T3COL_6 = "Report_ID";    //INTEGER FOREIGN KEY
    private final static String T3COL_7 = "Credit_Card_num"; //TEXT
    private final static String T3COL_8 = "Expiry_Date"; //TEXT
    private final static String T3COL_9 = "Pending"; //INTEGER
    private final static String T3COL_10 = "Reminder"; //INTEGER

    /*
        Messages table: Contains all Messages between patients (role 2) and doctors (role 3).
        The same two FK to same table method applies here (see Appointments table).
        Patient-Doctor_Posts are structured in a message chat, but doesn't have to be.
     */
    //TABLE 4
    private final static String TABLE4_NAME = "Messages";
    private final static String T4CONST_1 = "FK_appointment";
    private final static String T4CONST_2 = "FK_Sender_ID";
    private final static String T4COL_1 = "Message_ID";   //INTEGER PRIMARY KEY
    private final static String T4COL_2 = "Sender_ID";   //INTEGER FOREIGN KEY
    private final static String T4COL_3 = "Context"; //TEXT
    private final static String T4COL_4 = "Post_time"; //TEXT
    private final static String T4COL_5 = "Appointment_ID"; //INTEGER FOREIGN KEY


    /*
        Reports table: Made after a successful diagnosis, a report is made from the doctor (role 3)
        describing a patient's diagnosis case. Refers to users table for two FK (see Appointments table).
        Also has another FK, referring to a Payment. Patient's medical history can be derived from here.
     */
    //TABLE 5
    private final static String TABLE5_NAME = "Reports";
    private final static String T5CONST_1 = "FK_patient";
    private final static String T5CONST_2 = "FK_doctor";
    private final static String T5CONST_3 = "FK_Appointments";
    private final static String T5COL_1 = "Report_ID";     //INTEGER PRIMARY KEY
    private final static String T5COL_2 = "Patient_ID";    //INTEGER FOREIGN KEY
    private final static String T5COL_3 = "Doctor_ID";     //INTEGER FOREIGN KEY
    private final static String T5COL_4 = "Date";          //TEXT YYYY-MM-DD HH:MM:SS.SSS
    private final static String T5COL_5 = "Description";   //TEXT
    private final static String T5COL_6 = "Payment_ID";    //INTEGER FOREIGN KEY
    private final static String T5COL_7 = "Appointments_ID"; //INTEGER FOREIGN KEY

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQL Create tables
        //users
        String query = "CREATE TABLE " + TABLE1_NAME +
                "("+ T1COL_1 +" INTEGER PRIMARY KEY, " +
                T1COL_2 +" TEXT, " +
                T1COL_3+" TEXT, " +
                T1COL_4+" INTEGER, " +
                T1COL_5+" TEXT, " +
                T1COL_6+" TEXT, " +
                T1COL_7+" TEXT, " +
                T1COL_8+" TEXT, " +
                T1COL_9+" TEXT, " +
                T1COL_10+" TEXT, " +
                T1COL_11+" INTEGER, " +
                T1COL_12+" TEXT, " +
                T1COL_13+" INTEGER, " +
                T1COL_14+" INTEGER, " +
                T1COL_15+" TEXT, " +
                T1COL_16+" TEXT, " +
                T1COL_17 +" INTEGER DEFAULT 0)";
        db.execSQL(query);

        //appointments
        query = "CREATE TABLE " + TABLE2_NAME +
                "(" + T2COL_1 + " INTEGER PRIMARY KEY, " +
                T2COL_2 + " INTEGER, " +
                T2COL_3 + " INTEGER, " +
                T2COL_4 + " TEXT, " +
                T2COL_5 + " INTEGER, " +
                T2COL_6 + " INTEGER, " +
                "CONSTRAINT " + T2CONST_1 + " FOREIGN KEY (" + T2COL_2 + ") REFERENCES " + TABLE1_NAME + "(" + T1COL_1 + ")," +
                "CONSTRAINT " + T2CONST_2 + " FOREIGN KEY (" + T2COL_3 + ") REFERENCES " + TABLE1_NAME + "(" + T1COL_1 + "))";
        db.execSQL(query);

        //payments
        query = "CREATE TABLE " + TABLE3_NAME +
                "(" + T3COL_1 + " INTEGER PRIMARY KEY, " +
                T3COL_2 + " INTEGER, " +
                T3COL_3 + " TEXT, " +
                T3COL_4 + " TEXT, " +
                T3COL_5 + " REAL, " +
                T3COL_6 + " INTEGER, " +
                T3COL_7 + " TEXT, " +
                T3COL_8 + " TEXT, " +
                T3COL_9 + " INTEGER, " +
                T3COL_10 + " INTEGER, " +
                "CONSTRAINT " + T3CONST_1 + " FOREIGN KEY (" + T3COL_2 + ") REFERENCES " + TABLE1_NAME + "(" + T1COL_1 + ")," +
                "CONSTRAINT " + T3CONST_2 + " FOREIGN KEY (" + T3COL_6 + ") REFERENCES " + TABLE5_NAME + "(" + T5COL_1 + "))";
        db.execSQL(query);

        //
        // Messages
        query = "CREATE TABLE " + TABLE4_NAME +
                "(" + T4COL_1 + " INTEGER PRIMARY KEY, " +
                T4COL_2 + " INTEGER, " +
                T4COL_3 + " TEXT, " +
                T4COL_4 + " INTEGER, " +
                T4COL_5 + " INTEGER, " +
                "CONSTRAINT " + T4CONST_1 + " FOREIGN KEY (" + T4COL_5 + ") REFERENCES " + TABLE2_NAME + "(" + T2COL_1 + ")," +
                "CONSTRAINT " + T4CONST_2 + " FOREIGN KEY (" + T4COL_2 + ") REFERENCES " + TABLE1_NAME + "(" + T1COL_1 + "))";
        db.execSQL(query);

        //reports
        query = "CREATE TABLE " + TABLE5_NAME +
                "(" + T5COL_1 + " INTEGER PRIMARY KEY, " +
                T5COL_2 + " INTEGER, " +
                T5COL_3 + " INTEGER, " +
                T5COL_4 + " TEXT, " +
                T5COL_5 + " TEXT, " +
                T5COL_6 + " INTEGER, " +
                T5COL_7 + " INTEGER, " +
                "CONSTRAINT " + T5CONST_1 + " FOREIGN KEY (" + T5COL_2 + ") REFERENCES " + TABLE1_NAME + "(" + T1COL_1 + ")," +
                "CONSTRAINT " + T5CONST_2 + " FOREIGN KEY (" + T5COL_3 + ") REFERENCES " + TABLE1_NAME + "(" + T1COL_1 + ")" +
                "," + "CONSTRAINT " + T5CONST_3 + " FOREIGN KEY (" + T5COL_7 + ") REFERENCES " + TABLE2_NAME + "(" + T2COL_1 + ")" +
                ")";
        db.execSQL(query);

        //insert a single admin account for access
        ContentValues cv = new ContentValues();
        cv.put(T1COL_2, "root");
        cv.put(T1COL_3, "pass");
        cv.put(T1COL_8, "localhost");
        cv.put(T1COL_4, 1);
        db.insert(TABLE1_NAME, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //Drop tables if exists
        //ORDER IS IMPORTANT! Watch constraints.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE4_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE3_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE5_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME);
        onCreate(db);
    }

    //login user function: find matching username + password.
    public Cursor loginUser(String user, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT "+T1COL_1+", "+T1COL_4+", "+T1COL_17 +", "+T1COL_14 +" FROM "+TABLE1_NAME+
                " WHERE "+T1COL_2+"=?  AND "+T1COL_3+"= ? ";
        return db.rawQuery(query,new String[]{user,pass});
    }


    public boolean adminRegister(String accName, String accPass, int accRole, String accEmail) {
        SQLiteDatabase db = this.getWritableDatabase();

        //validation: ROLE MUST BE 1-4!
        if (accRole > 0 && accRole < 5) {
            //accRole acceptable, insert into db
            ContentValues cv = new ContentValues();
            cv.put(T1COL_2, accName.trim());
            cv.put(T1COL_3, accPass.trim());
            cv.put(T1COL_4, accRole);
            cv.put(T1COL_8, accEmail.trim());
            cv.put(T1COL_14, 1); //First time login, user HAS to modify account.
            long reply = db.insert(TABLE1_NAME, null, cv);

            //return results
            return reply > 0;
        }
        return false;
    }

    public boolean normalRegister(String accName, String accPass, int accRole, String accEmail, String accFirstName, String accLastName, String accAddress, String accCity, String accPhone, int accWeight, String accGender, int accAge, String accMSP) {
    //public boolean normalRegister(String accName, String accPass,  String accEmail, String accFirstName, String accLastName, String accAddress, String accCity, String accPhone, int accWeight, String accGender, int accAge, int accMSP) {
        accCity = (accCity.trim().toLowerCase()).substring(0, 1).toUpperCase() + (accCity.trim().toLowerCase()).substring(1);
        SQLiteDatabase db = this.getWritableDatabase();
        //insert into db
        ContentValues cv = new ContentValues();
        cv.put(T1COL_2, accName.trim());
        cv.put(T1COL_3, accPass.trim());
        cv.put(T1COL_4, accRole);
        cv.put(T1COL_5, accFirstName.trim());
        cv.put(T1COL_6, accLastName.trim());
        cv.put(T1COL_7, accAddress.trim());
        cv.put(T1COL_8, accEmail);
        cv.put(T1COL_9, accPhone.trim());
        cv.put(T1COL_11, accWeight);
        cv.put(T1COL_12, accGender);
        cv.put(T1COL_13, accAge);
        cv.put(T1COL_15, accCity);
        cv.put(T1COL_16, accMSP);
        cv.put(T1COL_14, 0);
        long reply = db.insert(TABLE1_NAME, null, cv);

        //return results
        return reply > 0;
    }

    //get all accounts (id, name, pass, role, deactivated) WHERE Role & Username & deactivated MATCH filter.
    public Cursor viewAllAccounts(String usernameFilter, String roleFilter, String deactFilter)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT "+T1COL_1+", "+T1COL_2+", "+T1COL_5+", "+T1COL_6+", "+T1COL_4+", "+ T1COL_17 +" FROM "+TABLE1_NAME
                +" WHERE " + T1COL_4 + roleFilter
                +" AND " + T1COL_2 + usernameFilter
                +" AND " + T1COL_17 + deactFilter;
        Log.e("DbAdminViewAll ", query);
        return db.rawQuery(query, null);
    }

    //Get information from TABLE1_NAME to show in patient activities
    public Cursor getInformationUser(int userId) {

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE1_NAME + " WHERE " + T1COL_1 + " = " + userId;
            Log.d("DbgetInformationUser ", query);
            return db.rawQuery(query, null);
        } catch (Exception msg) {
            Log.e("DbgetInformationUser ", msg.getMessage());
            return null;
        }
    }

    //update an account from admin perspective
    public boolean adminUpdate(int accID, String accName, String accPass, String accEmail, String accFirstName, String accLastName, String accAddress, String accCity, String accPhone, int accWeight, String accQualifications, String accGender, int accAge, String accMSP) {
        try {
            accCity = (accCity.trim().toLowerCase()).substring(0, 1).toUpperCase() + (accCity.trim().toLowerCase()).substring(1);
            SQLiteDatabase db = this.getWritableDatabase();
            //insert into db
            ContentValues cv = new ContentValues();
            cv.put(T1COL_2, accName.trim());
            cv.put(T1COL_3, accPass.trim());
            cv.put(T1COL_5, accFirstName.trim());
            cv.put(T1COL_6, accLastName.trim());
            cv.put(T1COL_7, accAddress.trim());
            cv.put(T1COL_8, accEmail.trim());
            cv.put(T1COL_9, accPhone.trim());
            cv.put(T1COL_11, accWeight);
            cv.put(T1COL_12, accGender);
            cv.put(T1COL_13, accAge);
            cv.put(T1COL_15, accCity);
            cv.put(T1COL_16, accMSP);
            int reply = db.update(TABLE1_NAME, cv, T1COL_1 + "=" + accID, null);

            //return results
            return reply > 0;
        } catch (Exception msg) {
            Log.e("DbAdminupdInfoUser ", msg.getMessage());
            return false;
        }
    }

    //delete account from admin
    public boolean adminDeactivate(int accID)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(T1COL_17,1);
            int reply = db.update(TABLE1_NAME,cv,T1COL_1+"="+accID,null);
            //return results
            if(reply > 0)
            {return true;}
            else
            {return false;}
        } catch (Exception msg) {
            Log.e("DbAdminDeactivateUser: "+accID, msg.getMessage());
            return false;
        }
    }
    //admin reactivate account
    public boolean adminReactivate(int accID)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(T1COL_17,0);
            int reply = db.update(TABLE1_NAME,cv,T1COL_1+"="+accID,null);
            //return results
            if(reply > 0)
            {return true;}
            else
            {return false;}
        } catch (Exception msg) {
            Log.e("DbAdminReactivateUser: "+accID, msg.getMessage());
            return false;
        }
    }

    //Update first login Patient Information
    public boolean updateFirstLoginUser(int accID, String accPass, String accFirstName, String accLastName, String accAddress, String accCity, String accPhone, int accWeight,  String accGender, int accAge, String accMSP) {
        try {
            accCity = (accCity.trim().toLowerCase()).substring(0, 1).toUpperCase() + (accCity.trim().toLowerCase()).substring(1);
            SQLiteDatabase db = this.getWritableDatabase();
            //update into db
            ContentValues cv = new ContentValues();
            cv.put(T1COL_3, accPass.trim());
            cv.put(T1COL_5, accFirstName.trim());
            cv.put(T1COL_6, accLastName.trim());
            cv.put(T1COL_7, accAddress.trim());
            cv.put(T1COL_9, accPhone.trim());
            cv.put(T1COL_11, accWeight);
            cv.put(T1COL_12, accGender);
            cv.put(T1COL_13, accAge);
            cv.put(T1COL_15, accCity);
            cv.put(T1COL_16, accMSP);
            cv.put(T1COL_14,0);
            int reply = db.update(TABLE1_NAME, cv, T1COL_1 + "=" + accID, null);

            //return results
            return reply > 0;
        } catch (Exception msg) {
            Log.e("DbfirstlogupdInfoUser ", msg.getMessage());
            return false;
        }
    }
    //Update Patient Information
    public boolean updateInformationUser(int userId, String address, String city, String email, String phone, int weight, String msp) {
        try {
            city = (city.trim().toLowerCase()).substring(0, 1).toUpperCase() + (city.trim().toLowerCase()).substring(1);
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(T1COL_7, address.trim());
            cv.put(T1COL_9, city.trim());
            cv.put(T1COL_8, email);
            cv.put(T1COL_9, phone);
            cv.put(T1COL_11, weight);
            cv.put(T1COL_16, msp);
            cv.put(T1COL_14,0);
            int reply = db.update(TABLE1_NAME, cv, T1COL_1 + "=" + userId, null);

            //return results
            return reply > 0;
        } catch (Exception msg) {
            Log.e("DbupdInfoUser ", msg.getMessage());
            return false;
        }
    }

    //Search for Doctor
    public Cursor searchDoctor(String city) {
        try {
            city = (city.trim().toLowerCase()).substring(0, 1).toUpperCase() + (city.trim().toLowerCase()).substring(1);
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE1_NAME + " WHERE " + T1COL_4 + "=3" + " AND " + T1COL_15 + " LIKE '%" + city + "%'";
            Log.d("DbgetDocInfo ", query);
            return db.rawQuery(query, null);
        } catch (Exception msg) {
            Log.e("DbgetDocInfo ", msg.getMessage());
            return null;
        }
    }

    //Insert info into appointments table
    public boolean bookAppointment(int patientID, int doctorID, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        //Insert info into Appointments Table
        ContentValues cv = new ContentValues();
        cv.put(T2COL_2, patientID);
        cv.put(T2COL_3, doctorID);
        cv.put(T2COL_4, date.trim());
        cv.put(T2COL_5, 30);
        long reply = db.insert(TABLE2_NAME, null, cv);

        //return results
        return reply > 0;
    }


    //Get appointments by doctorID for Doctor
    public Cursor getAppointmentDoctor(int doctorID) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE2_NAME + " WHERE " + T2COL_3 + "=" + doctorID + " AND " + T2COL_6 + " IS NULL";
            Log.d("DbgetAppDoctorIDInfo ", query);
            return db.rawQuery(query, null);
        } catch (Exception msg) {
            Log.e("DbgetAppDoctorIDInfo ", msg.getMessage());
            return null;
        }
    }

    //Get appointments by doctorID for Doctor
    public Cursor getAppointmentPatient(int patiID) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE2_NAME + " WHERE " + T2COL_2 + "=" + patiID + " AND " + T2COL_6 + " IS NULL";
            Log.d("DbgetAppPatiIDInfo ", query);
            return db.rawQuery(query, null);
        } catch (Exception msg) {
            Log.e("DbgetAppPatiIDInfo ", msg.getMessage());
            return null;
        }
    }

    //Get appointments by appointment_ID
    public Cursor getAppointmentByID(int appointID) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE2_NAME + " WHERE " + T2COL_1 + "=" + appointID;
            Log.d("DbgetAppInfo ", query);
            return db.rawQuery(query, null);
        } catch (Exception msg) {
            Log.e("DbgetAppInfo ", msg.getMessage());
            return null;
        }
    }

    //Delete appointment
    public void cancelAppointment(int appointID) {
        SQLiteDatabase db = this.getWritableDatabase();

        long reply = db.delete(TABLE2_NAME, T2COL_1 + " = " + appointID, null);
        //return results
    }

    //Create message row
    public void newMessages(int sender_ID, String context, int appointID) {
        SQLiteDatabase db = this.getWritableDatabase();
        //insert into db
        ContentValues cv = new ContentValues();
        cv.put(T4COL_2, sender_ID);
        cv.put(T4COL_3, context);
        cv.put(T4COL_4, "strftime('%s','now')");
        cv.put(T4COL_5, appointID);
        long reply = db.insert(TABLE4_NAME, null, cv);
        //return results

    }

    //Get messages by appointment_ID
    public Cursor viewMessagesByAppointID(int appointID) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE4_NAME + " WHERE " + T4COL_5 + "=" + appointID + " ORDER BY " + T4COL_4 + " ASC ";
            Log.d("DbgetMessageInfo ", query);
            return db.rawQuery(query, null);
        } catch (Exception msg) {
            Log.e("DbgetMessageInfo ", msg.getMessage());
            return null;
        }
    }

    //set ReportID to appointment
    public void setReportIDtoAppointment(int appointID, int reportID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(T2COL_6, reportID);
        int reply = db.update(TABLE2_NAME, cv, T2COL_1 + "=" + appointID, null);

        //return results
    }

    //delete row from Messages
    public void messagesDel(int appointId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE4_NAME, T4COL_5 + " = " + appointId, null);
    }

    //insert row to report
    public long reportInsert(int patiId, int docId, String date, String desc, int appointId) {
        SQLiteDatabase db = this.getWritableDatabase();
        //insert into db
        ContentValues cv = new ContentValues();
        cv.put(T5COL_2, patiId);
        cv.put(T5COL_3, docId);
        cv.put(T5COL_4, date);
        cv.put(T5COL_5, desc);
        cv.put(T5COL_7, appointId);
        long reply = db.insert(TABLE5_NAME, null, cv);
        //return results
        if (reply > 0) {
            return reply;
        } else {
            return 0;
        }

    }

    //Get all rows from the table Report with paymentID-filter
    public Cursor viewReportWithoutPaymentId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + T5COL_1 + ", " + T5COL_2 + ", " + T5COL_3 + ", " + T5COL_4 + ", " + T5COL_5 + " FROM " + TABLE5_NAME
                + " WHERE " + T5COL_6 + " IS NULL ";
        Log.e("DbviewReportWithoutP ", query);
        return db.rawQuery(query, null);
    }

    //Get row from the table Report with reportID-filter
    public Cursor viewReport(int x) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + T5COL_2 + ", " + T5COL_3 + ", " + T5COL_4 + ", " + T5COL_5 + ", " + T5COL_6 + " FROM " + TABLE5_NAME
                + " WHERE " + T5COL_1 + "=" + x;
        Log.e("DbviewReport ", query);
        return db.rawQuery(query, null);
    }

    //Get row from the table Report with patientID-filter
    public Cursor viewReportwithPatient(int x) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE5_NAME
                + " WHERE " + T5COL_2 + "=" + x;
        Log.e("DbviewReportPati ", query);
        return db.rawQuery(query, null);
    }

    //Get all rows from payment will transDate-fliter
    public Cursor viewPaymentPayedOrNot(String n) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + T3COL_1 + ", " + T3COL_2 + ", " + T3COL_3 + ", " + T3COL_5 + " FROM " + TABLE3_NAME
                + " WHERE " + T3COL_4 + n;
        Log.e("Dbpaymentpayed ", query);
        return db.rawQuery(query, null);
    }

    //Find payment row with paymentID
    public Cursor viewPayment(int x) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + T3COL_2 + ", " + T3COL_3 + ", " + T3COL_4 + ", " + T3COL_5 + ", " + T3COL_6 + ", " + T3COL_7 + ", " + T3COL_8 + ", " + T3COL_9 + " FROM " + TABLE3_NAME
                + " WHERE " + T3COL_1 + "=" + x;
        Log.e("DbviewPayment ", query);
        return db.rawQuery(query, null);
    }

    //get unpayed payment with patient id
    public Cursor viewPaymentByPatient(int x) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE3_NAME
                + " WHERE " + T3COL_2 + "=" + x + " AND " + T3COL_4 + " IS NULL";
        Log.e("DbviewPaymentPati ", query);
        return db.rawQuery(query, null);
    }

    //Insert a new row to payment table
    public long paymentInsert(int patiId, String dueDate, int amount_num, int reportID) {
        SQLiteDatabase db = this.getWritableDatabase();
        //insert into db
        ContentValues cv = new ContentValues();
        cv.put(T3COL_2, patiId);
        cv.put(T3COL_3, dueDate);
        cv.put(T3COL_5, amount_num);
        cv.put(T3COL_6, reportID);
        cv.put(T3COL_9, 0);
        cv.put(T3COL_10, 0);
        long reply = db.insert(TABLE3_NAME, null, cv);
        //return results
        if (reply > 0) {
            return reply;
        } else {
            return 0;
        }

    }

    //Update dueDate to Payment table
    public void updatePaymentWithdueDate(int payID, String duedate) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T3COL_3, duedate);
            int reply = db.update(TABLE3_NAME, cv, T3COL_1 + "=" + payID, null);

            //return results
        } catch (Exception msg) {
            Log.e("updatePaymentWithdue ", msg.getMessage());
        }
    }

    //Update TransDate to Payment table
    public void updatePaymentWithtransDate(int payID, String transDate) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T3COL_4, transDate);
            int reply = db.update(TABLE3_NAME, cv, T3COL_1 + "=" + payID, null);

            //return results
        } catch (Exception msg) {
            Log.e("updatePaymentWithtrans ", msg.getMessage());
        }
    }

    //Update pendingStatus to Payment table
    public void updatePaymentWithPending(int payID, int pending) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T3COL_9, pending);
            int reply = db.update(TABLE3_NAME, cv, T3COL_1 + "=" + payID, null);

            //return results
        } catch (Exception msg) {
            Log.e("updatePaymentWithPend ", msg.getMessage());
        }
    }

    //Update amount to Payment table
    public void updatePaymentWithAmount(int payID, int amount) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T3COL_5, amount);
            int reply = db.update(TABLE3_NAME, cv, T3COL_1 + "=" + payID, null);

            //return results
        } catch (Exception msg) {
            Log.e("updatePaymentWithAmou ", msg.getMessage());
        }
    }

    //Update expiry and credit number to Payment table
    public void updatePaymentWithCredit(int payID, String credit, String expiry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T3COL_7, credit);
            cv.put(T3COL_8, expiry);
            cv.put(T3COL_9, 1);
            int reply = db.update(TABLE3_NAME, cv, T3COL_1 + "=" + payID, null);
            //return results
        } catch (Exception msg) {
            Log.e("updatePaymentWithCred ", msg.getMessage());
        }
    }

    //Update paymentID to Report
    public void updateReportWithPaymentID(int payID, int reportID) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T5COL_6, payID);
            int reply = db.update(TABLE5_NAME, cv, T5COL_1 + "=" + reportID, null);

            //return results
        } catch (Exception msg) {
            Log.e("updateReportWithP ", msg.getMessage());
        }
    }

    //set reminder
    public void setReminder(int paymentId, int x) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(T3COL_10, x);
            int reply = db.update(TABLE3_NAME, cv, T3COL_1 + "=" + paymentId, null);

            //return results
        } catch (Exception msg) {
            Log.e("setReminder ", msg.getMessage());
        }
    }

    //check reminder
    public Cursor checkReminder(int patiId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+ T3COL_1 + ", " + T3COL_3 +" FROM " + TABLE3_NAME
                + " WHERE " + T3COL_2 + "=" + patiId + " AND " + T3COL_10 + "= 1";
        Log.e("checkReminder ", query);
        return db.rawQuery(query, null);
    }
}