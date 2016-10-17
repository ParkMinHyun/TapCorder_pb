package com.example.osm.appdesign21;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    ArrayList<PhoneBook> contactList;
    ArrayList<PhoneBook> existList;

    private ListView mlvContact;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        contactList = new ArrayList<>();
        pref = new SharedPreferences(this);

        mlvContact = (ListView)findViewById(R.id.contactList);

        final List<PhoneBook> listPhoneContact = new ArrayList<PhoneBook>();

        // 핸드폰 연락처 동기화
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Log.i("Names", name);
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Query call_user here. Covered next
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i("Number", phoneNumber);
                        listPhoneContact.add(new PhoneBook(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), name, phoneNumber, ""));
                    }
                    phones.close();
                }

            }
        }

        PhoneBookAdapter adapter = new PhoneBookAdapter(this, listPhoneContact);
        mlvContact.setAdapter(adapter);

        mlvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int itemPosition = i;

                PhoneBook person = (PhoneBook) mlvContact.getItemAtPosition(i);
                String name = person.getmName();
                String phoneNumber = person.getmPhone();

                for(int j = 0; j < 5 ; j++){
                    if(pref.getValue(Integer.toString(j), "no", "name") == "no"){
                        pref.putValue(Integer.toString(j), name, "name");
                        pref.putValue(Integer.toString(j), phoneNumber, "phoneNum");
                        break;
                    }
                }
                Toast.makeText(ContactActivity.this, name + "님이 보호자 연락망에\n추가되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed(){
        finish();
        Intent intent = new Intent(ContactActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
