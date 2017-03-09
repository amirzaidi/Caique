package v6.caique;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SubscribedFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener {

    public static MainActivity Instance;
    public SubscribedFragment Subs;
    public ExploreFragment Explore;
    public SharedPreferences sharedPref;
    public static boolean Music = true;
    public static boolean Typing = true;

    private static void RelogFirebase()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        mAuth.signInWithEmailAndPassword("v6@inf.com", "caique");
    }

    static
    {
        RelogFirebase();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (final ChatActivity act : ChatActivity.Instances.values())
                {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                act.ReloadChatViews(false, true);
                            }
                            catch (Exception e)
                            {
                                Log.d("AutoTypingRemover", e.getMessage());
                            }
                        }
                    });
                }
            }
        }, 0, 2500);
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

        Explore = new ExploreFragment();

        RelogFirebase();

        sharedPref = getSharedPreferences("caique", Context.MODE_PRIVATE);
        Music = sharedPref.getBoolean("music", true);
        Typing = sharedPref.getBoolean("typing", true);

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
            startActivityForResult(signInIntent, 1010);
        }

        DownloadPicture();
        ReloadNavbar();
    }

    public void ReloadNavbar()
    {
        NavigationView NavView = (NavigationView) findViewById(R.id.nav_view);
        TextView Name = (TextView) NavView.getHeaderView(0).findViewById(R.id.nav_username);
        if (Name != null && sharedPref.contains("gid"))
        {
            Name.setText(CacheChats.Name(sharedPref.getString("gid", null)));
            ((TextView) NavView.getHeaderView(0).findViewById(R.id.nav_email)).setText(sharedPref.getString("mail", ""));
        }
    }

    public void DownloadPicture() {

        NavigationView NavView = (NavigationView) findViewById(R.id.nav_view);
        final ImageView Picture = (ImageView) NavView.getHeaderView(0).findViewById(R.id.userdp);
        Picture.setImageDrawable(null);

        final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("users/" + MainActivity.Instance.sharedPref.getString("gid", null));
        try {
            storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    try {
                        Glide.with(MainActivity.Instance)
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .centerCrop()
                                .bitmapTransform(new CropCircleTransformation(MainActivity.Instance))
                                .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                                .into(Picture);
                    }
                    catch (Exception x)
                    {
                        Log.d("GlideChatAdapter", "Glide: " + x.getMessage());
                    }
                }
            });
        }
        catch(Exception e){
        }

        Picture.requestLayout();
    }

    public void ChooseDP(View view) {
        startActivityForResult(Intent.createChooser(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT), "Select Picture"), 420);
    }

    public void changeName(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(CacheChats.Name(sharedPref.getString("gid", null)));
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("type", "name")
                        .addData("text", input.getText().toString())
                        .build());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
        if (!Subs.Active){
            SetSubscribedFragment();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_chats);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 420)
        {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpeg")
                        .build();
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("users");
                final UploadTask uploadTask = storageRef.child(MainActivity.Instance.sharedPref.getString("gid", "")).putFile(selectedImageUri, metadata);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        DownloadPicture();
                    }
                });
            }
        }
        else if (requestCode == 1010)
        {
            try {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result == null || !result.isSuccess()) {
                    this.finish();
                }

                GoogleSignInAccount Acc = result.getSignInAccount();

                if (Acc.getIdToken() != null) {
                    FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                            .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                            .addData("type", "reg")
                            .addData("text", Acc.getIdToken())
                            .build());

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("gid", Acc.getId());
                    editor.putString("gidtoken", Acc.getIdToken());
                    editor.putString("mail", Acc.getEmail());
                    editor.commit();

                    CacheChats.Restart(Acc.getId());
                    DownloadPicture();
                    ReloadNavbar();
                }
            }
            catch (NullPointerException Ex)
            {
                Log.d("GSO", "Failed getting token");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Subs.Active = false;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_chats) {
            SetSubscribedFragment();
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (id == R.id.nav_explore) {
            manager.beginTransaction()
                .replace(R.id.mainframe, Explore)
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
            Intent newActivity = new Intent(this, InviteActivity.class);
            startActivity(newActivity);
        }
        else if (id == R.id.nav_settings) {
            Intent newActivity = new Intent(this, SettingsActivity.class);
            startActivity(newActivity);
        }

        return true;
    }

    public void CreateChat(View view) {
        Intent newActivity = new Intent(this, NewChatActivity.class);
        startActivity(newActivity);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
