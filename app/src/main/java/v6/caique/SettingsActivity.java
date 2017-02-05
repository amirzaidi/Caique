package v6.caique;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    private Context CurrentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Switch MusicSwitch = (Switch)findViewById(R.id.playMusic);

        if(CurrentSettings.MusicInChats == true){
            MusicSwitch.setChecked(true);
        }
        else{
            MusicSwitch.setChecked(false);
        }

        MusicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SetJSONSettings(MusicSwitch.isChecked(), CurrentContext);
            }
        });
    }

    private void SetJSONSettings(boolean NewSwitchValue, Context context){
        try {
            CurrentSettings.JSONSettings.put("PlayMusic", NewSwitchValue);
            CreateSettingsFile(CurrentSettings.JSONSettings, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void GetSettings(Context context){

        String json = new String();
        StringBuilder text = new StringBuilder();
        File f = new File(context.getCacheDir()+ "/Settings.json");

        if(f.isFile()){
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                json = text.toString();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            CreateSettingsFile(null, context);
        }

        try {
            CurrentSettings.JSONSettings = new JSONObject(json);
            boolean PlayMusic = CurrentSettings.JSONSettings.getBoolean("PlayMusic");
            CurrentSettings.MusicInChats = PlayMusic;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void CreateSettingsFile(JSONObject JSONSettings, Context context){
        try {
            File file = new File(context.getCacheDir(), "Settings.json");
            FileWriter writer = new FileWriter(file);
            if(JSONSettings != null){
                writer.append(JSONSettings.toString());
            }
            else {
                writer.append(
                        "{\n" +
                                "\"PlayMusic\": true\n" +
                                "}");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Context getContext(){
        return this;
    }
}
