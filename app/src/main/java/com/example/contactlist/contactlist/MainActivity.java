package com.example.contactlist.contactlist;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import static android.provider.CalendarContract.CalendarCache.URI;

public class MainActivity extends AppCompatActivity {

    ArrayList<JSONObject> Contacts;
    ScrollView scrollContainer;
    LinearLayout contactsContainer;
    String[] paramsForContacts;
    TextView downloadingText;
    Integer textSize;
    Display display;
    DisplayMetrics metrics;
    AppCompatActivity main;
    String imgSize;
    Resources res;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        new createContacts().execute("https://randomuser.me/api/?inc=gender,name,picture&results=40");
    }

    private void init() {
        main = this;
        res = getResources();

        display = getWindowManager().getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        Contacts = new ArrayList<JSONObject>();
        imgSize = "large";
        paramsForContacts = new String[]{"gender", "title", "first", "last", imgSize};
        textSize = metrics.heightPixels/50;
        createContactsContainer();
    }

    private void createContactsContainer() {
        scrollContainer = new ScrollView(this);
        contactsContainer = new LinearLayout(this);

        contactsContainer.setLayoutParams(new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contactsContainer.setOrientation(LinearLayout.VERTICAL);
        contactsContainer.setBackgroundColor(Color.WHITE);

        downloadingText = new TextView(this);
        downloadingText.setText("Loading...");
        downloadingText.setTextSize(textSize);
        contactsContainer.addView(downloadingText);

        scrollContainer.addView(contactsContainer);

        setContentView(scrollContainer);
    }

    public void createListOfContacts() {
        contactsContainer.removeView(downloadingText);
        Drawable forMale = ContextCompat.getDrawable(this, R.drawable.star);
        Drawable forFemale = ContextCompat.getDrawable(this, R.drawable.heart);

        for(int i = 0; i < Contacts.size(); i++) {
            String[] fields = getFields(Contacts.get(i));

            LinearLayout layForContact = new LinearLayout(this);
            layForContact.setLayoutParams(new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layForContact.setOrientation(LinearLayout.HORIZONTAL);

            TextView contact = new TextView(this);
            contact.setText(fields[1] + " " + fields[2] + " " + fields[3]);
            contact.setTextSize(textSize);

            try {
                ImageView img = new ImageView(this);
                Drawable image = (Drawable) Contacts.get(i).get(imgSize);
                img.setImageDrawable((fields[0].equals("male")? forMale: forFemale));
                img.setBackground(image);
                layForContact.addView(img);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            layForContact.addView(contact);
            contactsContainer.addView(layForContact);
        }
    }

    private String[] getFields(JSONObject JSONContact) {
        String[] fields = new String[paramsForContacts.length];

        for(int i = 0; i < paramsForContacts.length; i++) {
            try {
                if(paramsForContacts[i] != imgSize)
                    fields[i] = (String) JSONContact.get(paramsForContacts[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return fields;
    }

    public JSONObject parseToJSONObj(String[] params, String Text) throws JSONException {
        JSONObject js = new JSONObject();

        for(int i = 0; i < params.length; i++) {
            String value;

            int ind = Text.indexOf(params[i]);
            ind = Text.indexOf("\"", Text.indexOf(":", ind));
            value = Text.substring(ind + 1, Text.indexOf("\"", ind + 1));

            js.put(params[i], value);
        }

        return js;
    }

    private class createContacts extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... params) {
            String[] allContacts = HttpGet(params[0], false).toString().split("\\}\\}\\,");

            for (int i = 0; i < allContacts.length; i++) {
                try {
                    JSONObject contact = parseToJSONObj(paramsForContacts, allContacts[i]);
                    contact.put(imgSize, HttpGet(contact.getString(imgSize), true));
                    Contacts.add(contact);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return "";
        }

        protected void onPostExecute(String s) {
            createListOfContacts();
        }

        private Object HttpGet(String url, Boolean img) {

            Object result = new Object();

            try {

                HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();

                http.setRequestMethod("GET");
                http.setUseCaches(false);
                http.setConnectTimeout(300);
                http.setReadTimeout(300);

                http.connect();

                StringBuilder sb = new StringBuilder();

                if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    if (!img) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                        String line = reader.readLine();
                        while (line != null) {
                            sb.append(line + "\n");
                            line = reader.readLine();
                        }

                        result = sb.toString();
                    }
                    else {
                        Drawable image = new BitmapDrawable(res, BitmapFactory.decodeStream(http.getInputStream()));
                        result = image;
                    }
                }

            } catch(Throwable cause) {
                cause.printStackTrace();
            }

            return result;
        }
    }

}