package v6.caique;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SubscribedFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener {

    public static MainActivity Instance;
    private SubscribedFragment Subs;
    public SharedPreferences sharedPref;

    private static void RelogFirebase()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        mAuth.signInWithEmailAndPassword("v6@inf.com", "caique");
    }

    static
    {
        RelogFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Instance != null) {
            Instance.finish();
        }

        Instance = this;

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

        Subs = new SubscribedFragment();
        SetSubscribedFragment();

        RelogFirebase();

        sharedPref = this.getSharedPreferences("caique", Context.MODE_PRIVATE);
        if (sharedPref.contains("gid"))
        {
            CacheChats.Restart(sharedPref.getString("gid", null));
        }
        else
        {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken("420728598029-dt0td1a1a40javb5knfggd0m5crag15d.apps.googleusercontent.com")
                    .build();

            final Activity end = this;
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            end.finish();
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, 1);
        }

    }

    public void ReloadViews()
    {
        Subs.Adapter.notifyDataSetChanged();
    }

    private void SetSubscribedFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainframe, Subs)
                .commit();

        Subs.Active = true;
        CacheChats.FilterSubs();
    }

    @Override
    public void onBackPressed(){
        if(!Subs.Active){
            Subs.Active = true;

            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.mainframe, Subs)
                    .commit();

            CacheChats.FilterSubs();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_chats);
        }
        else{
            super.onBackPressed();
        }
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
            GoogleSignInAccount Acc = result.getSignInAccount();

            if (Acc.getIdToken() != null) {
                FirebaseMessaging fm = FirebaseMessaging.getInstance();

                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("type", "reg")
                        .addData("text", Acc.getIdToken())
                        .build());

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("gid", Acc.getId());
                editor.putString("gidtoken", Acc.getIdToken());
                editor.commit();

                CacheChats.Restart(Acc.getId());
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

        if(id != R.id.nav_chats){
            Subs.Active = false;
        }

        return true;
    }

    public void CreateChat(View view) {
        //Intent newActivity = new Intent(this, SendServerMessage.class);
        Intent newActivity = new Intent(this, NewChatActivity.class);
        startActivity(newActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new SettingsActivity().GetSettings(this);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public void GoToPicture(View view) {
        Intent GoToPicture = new Intent (this, PictureActivity.class);
        startActivity(GoToPicture);
    }
}
