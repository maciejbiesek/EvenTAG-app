package com.example.maciej.eventag.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.example.maciej.eventag.Models.Constants.KEYS;
import static com.example.maciej.eventag.Models.Constants.USER_ID;

public class FriendsActivity extends ActionBarActivity{
    ArrayList<User> userList;
    private String m_Text = "";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Intent i = getIntent();
        userList = (ArrayList<User>) i.getSerializableExtra("friends");
        publish();
    }

    public void publish(){
        FriendsAdapter adapter = new FriendsAdapter(FriendsActivity.this, userList);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        final Button button = (Button) findViewById(R.id.addFriend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                builder.setTitle("Podaj e-mail znajomego:");

                // Set up the input
                final EditText input = new EditText(FriendsActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        postFriend(m_Text);
                        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
                        finish();
                        startActivity(getIntent());
                        Toast.makeText(FriendsActivity.this, "Dodano znajomego", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private void postFriend(String friendEmail){
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
//        to do after server's update
//        NetworkProvider networkProvider = new NetworkProvider(this);
//        try {
//            networkProvider.postFriend(myId, friendEmail);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    public class FriendsAdapter extends ArrayAdapter<User> {

        public FriendsAdapter(Context context, ArrayList<User> items) {
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
                    Log.i("TEST", "PROBOWALES USUNAC GOSCIA");
                    Toast.makeText(getApplicationContext(), "PROBOWALES USUNAC GOSCIA!", Toast.LENGTH_SHORT).show();
                }
            });

            memberImage.setImageBitmap(null);
            Picasso.with(FriendsActivity.this).load(user.getAvatarUrl()).into(memberImage);

            memberName.setText(user.getName());


            return convertView;
        }

        public User getItem(int position) {
            return userList.get(position);
        }

        public final int getCount() {
            return userList.size();
        }

        public final long getItemId(int position) {
            return position;
        }
    }

}
