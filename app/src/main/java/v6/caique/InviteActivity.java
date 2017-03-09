package v6.caique;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class InviteActivity extends AppCompatActivity {

    private InviteAdapter Adapter;
    public static ArrayList<String> ContactNumbers = new ArrayList<>();
    public static ArrayList<String> ContactNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Adapter = new InviteAdapter(this);
        ((ListView) findViewById(R.id.ContactsList)).setAdapter(Adapter);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 1);
        }
        else
        {
            onRequestPermissionsResult(1, null, new int[] { PackageManager.PERMISSION_GRANTED });
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
        {
            this.finish();
            return;
        }

        ContentResolver cr = this.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, "display_name");
        int NameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int NumberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String Number = cursor.getString(NumberColumn).replace(" ", "").replace("-", "");
            if (!ContactNumbers.contains(Number))
            {
                ContactNumbers.add(Number);
                ContactNames.add(cursor.getString(NameColumn) + " (" + Number + ")");
            }
        }

        cursor.close();
        Adapter.notifyDataSetChanged();
    }
}
