package v6.caique;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SubscribedFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener {

    public static MainActivity Instance;
    private SubscribedFragment Subs;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private HashMap<String, Integer> ToCancel = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Instance != null) {
            Instance.finish();
        }

        Instance = this;
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("420728598029-dt0td1a1a40javb5knfggd0m5crag15d.apps.googleusercontent.com")
                .build();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        //Close app
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 1);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_chats);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, Subs = new SubscribedFragment())
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                Log.d("", "Sending registration");

                try {
                    String token = acct.getIdToken();

                    if (token != null) {
                        FirebaseMessaging fm = FirebaseMessaging.getInstance();
                        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                                .addData("type", "reg")
                                .addData("text", token)
                                .build());

                        return;
                    }
                }
                catch (NullPointerException Ex)
                {
                }
            }

            Log.d("", "Failed sign in");
            this.finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_chats) {

            manager.beginTransaction()
                    .replace(R.id.mainframe, Subs = new SubscribedFragment())
                    .commit();

            drawer.closeDrawer(GravityCompat.START);

            FirebaseMessaging fm = FirebaseMessaging.getInstance();
            fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                    .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                    .addData("type", "reg")
                    .build());

        } else if (id == R.id.nav_explore) {

            manager.beginTransaction()
                .replace(R.id.mainframe, new ExploreFragment())
                .commit();

            drawer.closeDrawer(GravityCompat.START);
            Subs = null;

        } else if (id == R.id.nav_favorites) {

            manager.beginTransaction()
                    .replace(R.id.mainframe, new FavoritesFragment())
                    .commit();

            drawer.closeDrawer(GravityCompat.START);
            Subs = null;

        } else if (id == R.id.nav_invite_people) {

            Intent newActivity = new Intent(this, PictureActivity.class);
            startActivity(newActivity);

        } else if (id == R.id.nav_music_library) {

            Intent newActivity = new Intent(this, PictureActivity.class);
            startActivity(newActivity);

        } else if (id == R.id.nav_settings) {

            Intent newActivity = new Intent(this, PictureActivity.class);
            startActivity(newActivity);

        }

        return true;
    }

    public void CreateChat(View view) {
        Intent newActivity = new Intent(this, SendServerMessage.class);
        startActivity(newActivity);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void GetChatNames(final ArrayList<String> Chat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Subs != null)
                {
                    for (int i = 0; i < Chat.size(); i++) {
                        final String ID = Chat.get(i);
                        Subs.Adapter.add(ID);

                        if (ToCancel.containsKey(ID)) {
                            DatabaseCache.Cancel(ToCancel.get(ID));
                        }

                        ToCancel.put(ID, DatabaseCache.LoadChatData(ID, new Runnable() {
                            @Override
                            public void run() {
                                Subs.Adapter.notifyDataSetChanged();
                            }
                        }));
                    }
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
