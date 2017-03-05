package v6.caique;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class InviteActivity extends AppCompatActivity {

    private InviteAdapter Adapter;

    public static ArrayList<String> ContactNumbers = new ArrayList<>();
    public static ArrayList<String> ContactNames = new ArrayList<>();
    public static HashMap<String, String> Contacts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        if(ContactNumbers.size() == 0) {

            Integer Iteration = 0;
            ArrayList<String> ContactNamesTemp = new ArrayList<>();
            ArrayList<String> ContactNumbersTemp = new ArrayList<>();

            RequestPermission();

            for (String s : ContactNumbers) {
                if (Iteration > 0) {
                    if (!ContactNumbers.get(Iteration).equals(ContactNumbers.get(Iteration - 1))) {
                        ContactNamesTemp.add(ContactNames.get(Iteration));
                        ContactNumbersTemp.add(ContactNumbers.get(Iteration));
                    }
                } else {
                    ContactNamesTemp.add(ContactNames.get(Iteration));
                    ContactNumbersTemp.add(ContactNumbers.get(Iteration));
                }

                Iteration++;
            }

            for (Integer i = 0; i < ContactNamesTemp.size(); i++) {
                Contacts.put(ContactNumbersTemp.get(i), ContactNamesTemp.get(i));
            }

            ContactNumbers.clear();

            Object[] obj = Contacts.entrySet().toArray();
            Arrays.sort(obj, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((Map.Entry<String, String>) o1).getValue()
                            .compareTo(((Map.Entry<String, String>) o2).getValue());
                }
            });

            for (Object o : obj) {
                ContactNumbers.add(((Map.Entry<String, String>) o).getKey());
            }

            ContactNames = ContactNumbers;

        }

        ListView ContactList = (ListView) findViewById(R.id.ContactsList);
        Adapter = new InviteAdapter(this);
        ContactList.setAdapter(Adapter);


    }


    private void RequestPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            getContacts();
        }
        else{
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, 1);
            RequestPermission();
        }
    }

    private void getContacts(){
        ContentResolver cr = this.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {

            do {

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    if(pCur.moveToFirst()) {
                        while (pCur.moveToNext()) {

                            String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            ContactNumbers.add(contactNumber.replace(" ", ""));
                            ContactNames.add(contactName);
                        }
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext());
        }
    }

}
