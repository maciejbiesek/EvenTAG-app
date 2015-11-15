package com.example.maciej.eventag.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.Models.CircleGroup;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.maciej.eventag.Models.Constants.*;


public class UserProfileActivity extends FragmentActivity {

    private static final String USER_PREFERENCES = "RememberUserPreferences";
    private static final String IS_PUBLIC_POLICY = "policy_setting";
    private Boolean publicPolicy;
    private ArrayList<CircleGroup> arrayOfCircles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        TextView circles = (TextView) findViewById(R.id.circles);
        TextView events = (TextView) findViewById(R.id.events);
        final Fragment firstFragment = new Fragment();
        final Fragment secondFragment = new Fragment();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        circles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.fragment, firstFragment);
                Log.i("FRAGMENT", "FRAGMENT first");
                Log.i("TEST", "TEST: " + arrayOfCircles);
            }
        });

        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.fragment, secondFragment);
                Log.i("FRAGMENT", "FRAGMENT second");
                CircleGroupAdapter adapter = new CircleGroupAdapter(UserProfileActivity.this, arrayOfCircles);
                GridView circlesView = (GridView) findViewById(R.id.gridView);
                circlesView.setAdapter(adapter);
            }
        });


        Log.i("TEST", "TEST #0 " + arrayOfCircles);
        getCircles();
        Log.i("TEST", "TEST #2 " + arrayOfCircles);

        getUser();

    }

    @Override
    protected void onStop(){
        super.onStop();


    }

    public class CircleGroupAdapter extends ArrayAdapter<CircleGroup> {

        public CircleGroupAdapter(Context context, ArrayList<CircleGroup> items) {
            super(context, 0, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            CircleGroup circleGroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.circle_button, parent, false);
            }

            TextView cirName = (TextView) convertView.findViewById(R.id.buttonNowy);
            cirName.setText(circleGroup.getName());

            return convertView;
        }

        public CircleGroup getItem(int position) {
            return null;
        }

        public final int getCount() {
            return 9;
        }

//        public final Family getItem(int position) {
//            return mFams.get(position);
//        }

        public final long getItemId(int position) {
            return position;
        }
    }

    private void getUser(){
        NetworkProvider networkProvider = new NetworkProvider(this);
        networkProvider.getUserId();
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);

        networkProvider.getUser(myId);
        String userName = prefs.getString(USER_NAME, "Jan Kowalski");
        String userAvatar = prefs.getString(USER_AVATAR, null);

        final ImageView userAvatarView = (ImageView) findViewById(R.id.user_profile_picture);
        final TextView userNameView = (TextView) findViewById(R.id.user_name);
        userNameView.setText(userName);
        userAvatarView.setImageBitmap(null);
        Picasso.with(this).load(userAvatar).into(userAvatarView);

    }

    private void getCircles(){
        NetworkProvider networkProvider = new NetworkProvider(this);
        networkProvider.getUserId();
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
        networkProvider.getCircles(5, arrayOfCircles);
        Log.i("TEST", "TEST #1 " + arrayOfCircles);
    }

}