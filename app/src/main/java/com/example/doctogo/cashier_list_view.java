package com.example.doctogo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class cashier_list_view extends AppCompatActivity {
    private final ArrayList<Integer> paymentIdList = new ArrayList<>();
    private final ArrayList<Integer> patiIdList = new ArrayList<>();
    private final ArrayList<String> dueDateList = new ArrayList<>();
    private int[] payIdArr;
    private String patientNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier_list_view);
        DatabaseHelper dbh = new DatabaseHelper(this);
        Cursor c = dbh.viewPaymentPayedOrNot(" IS NULL ");
        if(c.getCount()>0) {
            while (c.moveToNext()) {
                paymentIdList.add(c.getInt(0));
                patiIdList.add(c.getInt(1));
                dueDateList.add(c.getString(2));
            }
            payIdArr = arrListtoarr(paymentIdList);
            int[] patientsIdArr = arrListtoarr(patiIdList);
            String[] dueDateArr = arrStringListtoarr(dueDateList);
            List<HashMap<String, String>> newList = new ArrayList<>();

            for (int i = 0; i < payIdArr.length; i++) {
                HashMap<String, String> hm = new HashMap<>();
                //use ID find patient's name
                Cursor u = dbh.getInformationUser(patientsIdArr[i]);
                if(u.getCount()>0){
                    while (u.moveToNext()){
                        patientNames = payIdArr[i]+ "   " +u.getString(4) +" "+ u.getString(5);
                    }
                }
                hm.put("txt1", patientNames);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
                try {
                    Date due = dateFormat.parse(dueDateArr[i]);
                    Date curr = Calendar.getInstance().getTime();
                    long daydiff= daysDiff(due, curr);
                    String dueDays = Long.toString(daydiff);
                    if(daydiff > 1) {
                        hm.put("txt2", dueDays +" Days");
                    }
                    else if(daydiff > 0 && daydiff <= 1){
                        hm.put("txt2", dueDays +" Day");
                    }
                    else{
                        hm.put("txt2", "Over Due");
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                newList.add(hm);
            }
            String[] from = {"txt1", "txt2"};
            int[] to = {R.id.cashier_listview2_patient, R.id.cashier_listview2_due};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), newList, R.layout.listview_cashier2, from, to);
            ListView listView = (ListView) findViewById(R.id.cashier_view_list);
            listView.setAdapter(adapter);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(cashier_list_view.this, cashier_transactions_view.class);
                    int p = payIdArr[position];
                    i.putExtra("payment", p);
                    startActivity(i);
                }
            });

        }
        else{
            TextView title = (TextView) findViewById(R.id.cashier_viewTitle);
            title.setText("Empty, all transactions have been paid");
        }

    }

    private static int[] arrListtoarr(List<Integer> arrList)
    {
        int[] arr = new int[arrList.size()];
        for (int i=0; i < arr.length; i++)
        {
            arr[i] = arrList.get(i);
        }
        return arr;
    }
    private static String[] arrStringListtoarr(List<String> arrList)
    {
        String[] arr2 = new String[arrList.size()];
        for (int i=0; i < arr2.length; i++)
        {
            arr2[i] = arrList.get(i);
        }
        return arr2;
    }
    private static long daysDiff(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        Date temp1;
        Date temp2;
        cal1.setTime(date1);
        cal2.setTime(date2);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal2.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        temp1 = cal1.getTime();
        temp2 = cal2.getTime();
        return (temp1.getTime() - temp2.getTime()) / 1000 / 60 / 60 / 24;
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }
}
