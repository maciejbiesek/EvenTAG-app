package com.example.maciej.eventag.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maciej.eventag.Adapters.ImageAdapter;
import com.example.maciej.eventag.ExpandableHeightGridView;
import com.example.maciej.eventag.Helpers.CommunicationHelper;
import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.R;
import com.example.maciej.eventag.Models.Tag;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.maciej.eventag.Models.Constants.*;

public class TagDetailsActivity extends ActionBarActivity {

    private Tag tag;
    private int myId;
    private NetworkProvider networkProvider;
    private CommunicationHelper comHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_details);
        showActionBar();

        Intent i = getIntent();
        tag = (Tag) i.getExtras().getSerializable(TAG_KEY);

        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        myId = prefs.getInt(USER_ID, 0);

        networkProvider = new NetworkProvider(this);
        comHelper = new CommunicationHelper(this);

        showTag();
    }

    private void showTag() {
        TextView name = (TextView) findViewById(R.id.name);
        TextView description = (TextView) findViewById(R.id.description);
        TextView shutdown = (TextView) findViewById(R.id.shutdown);
        TextView localisation = (TextView) findViewById(R.id.localisation);
        TextView members = (TextView) findViewById(R.id.members);
        ImageButton toMap = (ImageButton) findViewById(R.id.to_map);
        ImageButton more = (ImageButton) findViewById(R.id.more);
        ExpandableHeightGridView attendersGrid = (ExpandableHeightGridView) findViewById(R.id.attenders);

        final ImageAdapter attendersAdapter = new ImageAdapter(this, myId);
        networkProvider.getAttenders(tag, attendersAdapter);
        attendersGrid.setAdapter(attendersAdapter);
        attendersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TextView attenderLabel = (TextView) view.findViewById(R.id.attender_label);
                if (!attenderLabel.getText().toString().isEmpty()) {
                    if (attenderLabel.getVisibility() == View.VISIBLE) {
                        attenderLabel.setVisibility(View.INVISIBLE);
                    } else attenderLabel.setVisibility(View.VISIBLE);
                }

                if (tag.getUserId() != myId) {
                    if (position == attendersAdapter.getLast()) {
                        networkProvider.attend(tag, attendersAdapter);
                    }

                    else {
                        if (attendersAdapter.getItem(position).getId() == myId) {
                            showResignDialog(attendersAdapter);
                        }
                    }
                }
            }
        });

        if (tag.getUserId() == myId) {
            more.setVisibility(View.VISIBLE);
        }
        else {
            more.setVisibility(View.INVISIBLE);
        }

        name.setText(tag.getName());
        description.setText(tag.getDescription());
        localisation.setText(tag.getAddress());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date shutdownDate = null;

        try {
            shutdownDate = df.parse(tag.getShutdownTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateInfo = getTimeDiff(shutdownDate);
        shutdown.setText(dateInfo);

        if (dateInfo.toLowerCase().contains(getString(R.string.expired).toLowerCase())) {
            members.setText(getString(R.string.members_past));
        }
        else members.setText(getString(R.string.members_present));

        toMap.setOnClickListener(onClickListener);
        more.setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.to_map: {
                    finish();
                    break;
                }
                case R.id.more: {
                    showPopUp(view);
                    break;
                }
            }
        }
    };

    private void deleteTag() {
        try {
            networkProvider.deleteTag(tag);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setResult(1);
        finish();
    }

    private void editTag() {
        if (tag.getIsActive()) {
            Intent i = new Intent(this, EditTagActivity.class);
            i.putExtra(TAG_KEY, tag);
            startActivityForResult(i, TAG_EDIT_RESULT);
        }
        else comHelper.showUserDialog(getString(R.string.edit_tag), getString(R.string.edit_inactive_tag));
    }

    private String getTimeDiff(Date shutdownDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeDiff = "";
        Date todayDate = new Date();
        if (todayDate.before(shutdownDate)) {
            long diff = shutdownDate.getTime() - todayDate.getTime();
            long minutes = diff / (1000 * 60);
            if (minutes > 60) {
                long hours = minutes / 60;
                minutes = minutes % 60;
                if (minutes == 0) {
                    timeDiff += hours + " h";
                }
                else {
                    timeDiff += hours + " h " + minutes + " min";
                }

            }
            else {
                timeDiff += minutes + " min";
            }
            timeDiff += " " + getString(R.string.times_left);
        }
        else {
            timeDiff += getString(R.string.expired) + " " + df.format(shutdownDate);
        }
        return timeDiff;

    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_tag_dialog_message)
                .setTitle(R.string.delete_tag_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTag();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    private void showResignDialog(final ImageAdapter imgAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.resign_tag_dialog_message)
                .setTitle(R.string.resign_tag_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        networkProvider.resign(tag, imgAdapter);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    private void showPopUp(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_tag_item: {
                        editTag();
                        break;
                    }
                    case R.id.delete_tag_item: {
                        showDeleteDialog();
                        break;
                    }
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            setResult(1);
            finish();
        }
    }



    // MENU

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View cView = getLayoutInflater().inflate(R.layout.custom_details_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actionBar.setCustomView(cView, layout);
    }

    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.back: {
                finish();
                break;
            }
            case R.id.logo: {
                Toast.makeText(this, getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }


}
