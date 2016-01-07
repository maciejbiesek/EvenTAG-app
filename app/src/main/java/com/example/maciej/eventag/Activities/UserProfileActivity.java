package com.example.maciej.eventag.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.Models.CircleGroup;
import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.example.maciej.eventag.Models.Constants.*;


public class UserProfileActivity extends FragmentActivity {

    private static final String USER_PREFERENCES = "RememberUserPreferences";
    private static final String IS_PUBLIC_POLICY = "policy_setting";
    private Boolean publicPolicy;
    private ArrayList<CircleGroup> arrayOfCircles = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final TextView circles = (TextView) findViewById(R.id.circles);
        final TextView friends = (TextView) findViewById(R.id.friends);
        final Fragment firstFragment = new Fragment();
        final Fragment secondFragment = new Fragment();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        circles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.fragment, firstFragment);
                showCircles();
                final TextView addCircle = (TextView) findViewById(R.id.addCircleTextView);
                addCircle.setText("Dodaj Krąg");
                addCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                        builder.setTitle("Podaj nazwę kręgu:");

                        // Set up the input
                        final EditText input = new EditText(UserProfileActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_Text = input.getText().toString();
                                postCircle(m_Text);
                                SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
                                finish();
                                startActivity(getIntent());
                                Toast.makeText(UserProfileActivity.this, "Dodano nowy krąg", Toast.LENGTH_SHORT).show();
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
                Log.i("FRAGMENT", "FRAGMENT first");
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, FriendsActivity.class);
                Log.i("test", "lista: " + userList);

                i.putExtra("friends", userList);
                startActivity(i);
            }
        });

        getCircles();
        getUser();
        getFriends();
    }

    @Override
    protected void onResume(){
        super.onResume();
        getCircles();
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
            final CircleGroup circleGroup = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.circle_button, parent, false);
            }

            final Button cirName = (Button) convertView.findViewById(R.id.buttonNowy);
            cirName.setText(circleGroup.getName());
            cirName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserProfileActivity.this, CircleDetailsActivity.class);
                    intent.putExtra(CIRCLE_NAME, cirName.getText());
                    intent.putExtra(CIRCLE_ID, circleGroup.getId());
                    intent.putExtra("friends", userList);
                    startActivity(intent);
                }
            });



            return convertView;
        }

        public CircleGroup getItem(int position) {
            return arrayOfCircles.get(position);
        }

        public final int getCount() {
            return arrayOfCircles.size();
        }

        public final long getItemId(int position) {
            return position;
        }
    }

    private void postCircle(String circleName){
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
        NetworkProvider networkProvider = new NetworkProvider(this);
        try {
            networkProvider.postCircle(myId, circleName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
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

    private void getFriends(){
        NetworkProvider networkProvider = new NetworkProvider(this);
        userList = networkProvider.getFriends(userList);
    }

    private void getCircles(){
        NetworkProvider networkProvider = new NetworkProvider(this);
        networkProvider.getUserId();
        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int myId = prefs.getInt(USER_ID, 0);
        networkProvider.getCircles(myId, arrayOfCircles);
    }

    private void showCircles(){
        CircleGroupAdapter adapter = new CircleGroupAdapter(UserProfileActivity.this, arrayOfCircles);
        GridView circlesView = (GridView) findViewById(R.id.gridView);
        circlesView.setAdapter(adapter);
    }

    private void delCircles(){
        GridView circlesView = (GridView) findViewById(R.id.gridView);
        circlesView.setAdapter(null);
    }

}