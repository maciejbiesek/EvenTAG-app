package com.example.maciej.eventag.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.friends_details, parent, false);
            }

            final ImageView memberImage = (ImageView) convertView.findViewById(R.id.memberAvatar);
            final TextView memberName = (TextView) convertView.findViewById(R.id.memberName);

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
