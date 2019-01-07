package cit.jauc.lib;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;


public class HttpHandler {
    String jsonResponse = "";
    HttpURLConnection connection = null;
    InputStream is = null;

    public String makeHttpGetRequest(String requestUrl, String TAG) throws IOException {
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                jsonResponse = readFromStream(is);
            }
        } catch (IOException e) {
            Log.w(TAG, "readingReview:failure", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                is.close();
            }
        }
        return jsonResponse;
    }

    public String makeHttpPostRequest(String query, String requestUrl, String TAG) throws IOException {

        try {
            URL url = new URL(requestUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();
            //connection.setDoOutput(true); DO NOT DO THIS

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(query);
            wr.flush();
            wr.close();

            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                jsonResponse = readFromStream(is);
            }
        } catch (IOException e) {
            Log.w(TAG, "HTTPRequest:failure", e);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                is.close();
            }
        }
        return jsonResponse;
    }

    public String makeHttpPatchRequest(String query, String requestUrl, String TAG) throws IOException {

        try {
            URL url = new URL(requestUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("PATCH");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();
            //connection.setDoOutput(true); DO NOT DO THIS

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(query);
            wr.flush();
            wr.close();

            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                jsonResponse = readFromStream(is);
            }
        } catch (IOException e) {
            Log.w(TAG, "HTTPRequest:failure", e);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                is.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream is) throws IOException {
        StringBuilder output = new StringBuilder();
        if (is != null) {
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                output.append(line);
                line = br.readLine();
            }
        }
        return output.toString();
    }
}
