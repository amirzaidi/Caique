package v6.caique;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SubscribedFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener {

    public static MainActivity Instance;
    private SubscribedFragment Subs;
    private HashMap<String, Integer> ToCancel = new HashMap<>();
    //private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Instance != null) {
            Instance.finish();
        }

        Instance = this;

        new SettingsActivity().GetSettings(this);
        setTheme(R.style.AppTheme_NoActionBar);
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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("420728598029-dt0td1a1a40javb5knfggd0m5crag15d.apps.googleusercontent.com")
                .build();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        //Close app
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 1);

        //mAuth = FirebaseAuth.getInstance();
    }

    private void SetSubscribedFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, Subs = new SubscribedFragment())
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode != 1 || !result.isSuccess()) {
            this.finish();
        }

        try {
            CloudMessageService.RegToken = result.getSignInAccount().getIdToken();

            if (CloudMessageService.RegToken != null) {
                SetSubscribedFragment();
            }
        }
        catch (NullPointerException Ex)
        {
            Log.d("GSO", "Failed getting token");
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
            SetSubscribedFragment();
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (id == R.id.nav_explore) {
            manager.beginTransaction()
                .replace(R.id.mainframe, new ExploreFragment())
                .commit();
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (id == R.id.nav_favorites) {
            manager.beginTransaction()
                    .replace(R.id.mainframe, new FavoritesFragment())
                    .commit();
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (id == R.id.nav_invite_people) {
            Intent newActivity = new Intent(this, PictureActivity.class);
            startActivity(newActivity);
        }
        else if (id == R.id.nav_music_library) {
            Intent newActivity = new Intent(this, PictureActivity.class);
            startActivity(newActivity);
        }
        else if (id == R.id.nav_settings) {
            Intent newActivity = new Intent(this, SettingsActivity.class);
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
        /*FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mAuth.signInAnonymously().getResult();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void GetChatNames(final ArrayList<String> Chat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Subs != null && Subs.isAdded())
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
