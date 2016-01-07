package com.example.maciej.eventag.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.google.android.gms.maps.model.Circle;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.maciej.eventag.Models.Constants.CIRCLE_ID;
import static com.example.maciej.eventag.Models.Constants.CIRCLE_NAME;
import static com.example.maciej.eventag.Models.Constants.KEYS;
import static com.example.maciej.eventag.Models.Constants.USER_ID;

public class CircleDetailsActivity extends ActionBarActivity {
    public ArrayList<User> arrayOfFriends = new ArrayList<>();
    public ArrayList<User> usersInCircle = new ArrayList<>();
    public String[] friends;
    String users = "";
    String circleName;
    Integer circleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_details);
        getDetails();
        Intent intent = getIntent();
        arrayOfFriends = (ArrayList<User>) intent.getSerializableExtra("friends");
        friends = new String[arrayOfFriends.size()];

        for ( int i = 0; i < arrayOfFriends.size(); i++ ) {
            friends[i] = arrayOfFriends.get(i).getName();
        }
        final Button btn = (Button) findViewById(R.id.buttonRaz);
        final Button btn2 = (Button) findViewById(R.id.buttonDwa);
        final Button btn3 = (Button) findViewById(R.id.buttonTrzy);

        btn.setText("Zaladuj dane");
        btn2.setText("Usun krag");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails();
                btn.setVisibility(View.INVISIBLE);
                btn2.setVisibility(View.INVISIBLE);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CircleDetailsActivity.this);
                builder.setTitle("Czy na pewno usunąć krąg?");


                // Set up the buttons
                builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCircle();
                        finish();
                        Toast.makeText(CircleDetailsActivity.this, "Usunięto", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("NIE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", "TEST: " + arrayOfFriends);
                final ArrayList<Integer> selectedItemsIndexList = new ArrayList();
                AlertDialog.Builder dialog = new AlertDialog.Builder(CircleDetailsActivity.this);
                dialog.setTitle("Wybierz znajomych")
                        .setMultiChoiceItems(friends, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selectedItemsIndexList.add(which);
                                } else if (selectedItemsIndexList.contains(which)){
                                    selectedItemsIndexList.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        .setPositiveButton("DODAJ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                for(int i = 0; i < selectedItemsIndexList.size(); i++){
                                    if (!usersInCircle.contains(arrayOfFriends.get(selectedItemsIndexList.get(i)))) {
                                        usersInCircle.add(arrayOfFriends.get(selectedItemsIndexList.get(i)));
                                    }
                                }
                                updateUser();
                                showDetails();
                            }
                        })

                        .setNegativeButton("ANULUJ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                dialog.create();
                dialog.show();
            }
        });
    }

    public class CircleDetailsAdapter extends ArrayAdapter<User> {

        public CircleDetailsAdapter(Context context, ArrayList<User> items) {
            super(context, 0, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final User user = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.circle_details, parent, false);
            }

            final ImageView memberImage = (ImageView) convertView.findViewById(R.id.memberAvatar);
            final TextView memberName = (TextView) convertView.findViewById(R.id.memberName);

            Button cirName = (Button) convertView.findViewById(R.id.deleteButton);
            cirName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CircleDetailsActivity.this);
                    builder.setTitle("Czy na pewno usunąć " + user.getName() + "?");


                    // Set up the buttons
                    builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            usersInCircle.remove(user);
                            deleteUser();
                            finish();
                            startActivity(getIntent());
                            Toast.makeText(CircleDetailsActivity.this, "Usunięto", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("NIE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });

            memberImage.setImageBitmap(null);
            Picasso.with(CircleDetailsActivity.this).load(user.getAvatarUrl()).into(memberImage);

            memberName.setText(user.getName());


            return convertView;
        }

        public User getItem(int position) {
            return usersInCircle.get(position);
        }

        public final int getCount() {
            return usersInCircle.size();
        }

        public final long getItemId(int position) {
            return position;
        }
    }

    private void deleteUser(){
        NetworkProvider networkProvider = new NetworkProvider(CircleDetailsActivity.this);
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
        Intent i = getIntent();
        circleName = i.getStringExtra(CIRCLE_NAME);
        circleId = i.getIntExtra(CIRCLE_ID, 0);
        for (User user : usersInCircle) {
            users = users + "," + user.getId();
        }
        try {
            networkProvider.updateCircle(myId, circleId, circleName, users);
        } catch (JSONException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Toast.makeText(CircleDetailsActivity.this, "Usunieto!", Toast.LENGTH_SHORT).show();
    }

    private void updateUser(){
        NetworkProvider networkProvider = new NetworkProvider(CircleDetailsActivity.this);
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
        Intent i = getIntent();
        circleName = i.getStringExtra(CIRCLE_NAME);
        circleId = i.getIntExtra(CIRCLE_ID, 0);
        users = "";


        for ( int j = 0; j < usersInCircle.size(); j++ ) {
            if ( j == 0 ) {
                users = users + usersInCircle.get(j).getId();
            } else {
                users = users + "," + usersInCircle.get(j).getId();

            }
        }

        try {
            networkProvider.updateCircle(myId, circleId, circleName, users);
        } catch (JSONException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Toast.makeText(CircleDetailsActivity.this, "Dodano!", Toast.LENGTH_SHORT).show();
    }

    private void getDetails(){
        Intent i = getIntent();
        circleName = i.getStringExtra(CIRCLE_NAME);
        circleId = i.getIntExtra(CIRCLE_ID, 0);

        NetworkProvider networkProvider = new NetworkProvider(this);
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);

        networkProvider.getCircleDetails(myId, circleId, usersInCircle);

        //showDetails();
    }

    private void showDetails(){
        CircleDetailsAdapter adapter = new CircleDetailsAdapter(CircleDetailsActivity.this, usersInCircle);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    private void deleteCircle(){
        Intent i = getIntent();
        circleId = i.getIntExtra(CIRCLE_ID, 0);
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
        NetworkProvider networkProvider = new NetworkProvider(this);
        networkProvider.deleteCircle(myId, circleId);
    }
}
