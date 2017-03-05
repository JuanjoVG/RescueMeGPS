package hack.rescue.me.rescuemegps;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Sergio on 05/03/2017.
 */

public class AsyncLocation extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... messages) {
        URL url = null;
        try {
            url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("content-type", "application/json");
            urlConnection.setRequestProperty("authorization", "key=AIzaSyBslnK_hud9i0lbBFYoew-GtAiNlvIuQ_8");
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

            JSONObject dataObj = new JSONObject();
            dataObj.put("message", messages[0]);

            JSONObject obj = new JSONObject();
            obj.put("data", dataObj);
            obj.put("to", "eBdnXUuzNsw:APA91bEPcvYfHmxe72stvSHstDM-lKKkOljY1mZ0tsbGzUxms_aAGJato0iUk9R-NXPE-EZhUVDjd8EJHBth69nzfqEAp6pK6g1qiwpknlmoDdt-XQDyvzWrFATscCQJE6c55xEEGnu7");
            Log.d("JSON", obj.toString());
            out.write(obj.toString().getBytes());
            out.flush();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(in).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            Log.v("RESPONSE", urlConnection.getResponseCode() + "\n" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
