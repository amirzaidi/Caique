package v6.caique;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

class UnsubscribeTask extends AsyncTask<Integer, Integer, Long> {
    protected Long doInBackground(Integer... topics) {
        FirebaseMessaging Instance = FirebaseMessaging.getInstance();
        int SubTopics = 32;

        for (int topic : topics)
        {
            for (int i = 0; i < SubTopics; i++)
            {
                Log.d("Sub", "To chat-" + topic + "-" + i);
                Instance.unsubscribeFromTopic("chat-" + topic + "-" + i);
            }
        }

        return (long)SubTopics * topics.length;
    }
}
