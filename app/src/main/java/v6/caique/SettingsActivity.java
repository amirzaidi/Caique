package v6.caique;

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
    private JSONObject JSONSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        GetJSONSettings();
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
                SetJSONSettings(MusicSwitch.isChecked());
            }
        });
    }

    private void SetJSONSettings(boolean NewSwitchValue){
        try {
            JSONSettings.put("PlayMusic", NewSwitchValue);
            CreateSettingsFile(JSONSettings);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void GetJSONSettings(){

        String json = new String();
        StringBuilder text = new StringBuilder();
        File f = new File(getCacheDir()+ "/Settings.json");

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
            CreateSettingsFile(null);
        }

        try {
            JSONSettings = new JSONObject(json);
            boolean PlayMusic = JSONSettings.getBoolean("PlayMusic");
            CurrentSettings.MusicInChats = PlayMusic;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void CreateSettingsFile(JSONObject JSONSettings){
        try {
            File file = new File(getCacheDir(), "Settings.json");
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
}
