package com.example.maciej.eventag.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.maciej.eventag.R;
import com.example.maciej.eventag.Adapters.CommentAdapter;
import com.example.maciej.eventag.Adapters.ImageAdapter;
import com.example.maciej.eventag.Views.ExpandableHeightGridView;
import com.example.maciej.eventag.Helpers.CommunicationHelper;
import com.example.maciej.eventag.Helpers.NetworkProvider;

import com.example.maciej.eventag.Views.ExpandableHeightListView;
import com.example.maciej.eventag.models.Comment;
import com.example.maciej.eventag.models.Tag;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.maciej.eventag.models.Constants.*;

public class TagDetailsActivity extends BaseActivity {

    private Tag tag;
    private int myId;
    private NetworkProvider networkProvider;
    private CommunicationHelper comHelper;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_details);
        actBar = getSupportActionBar();
        showActionBar(actBar);

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
        ImageButton navigateTo = (ImageButton) findViewById(R.id.navigate_to);
        ExpandableHeightGridView attendersGrid = (ExpandableHeightGridView) findViewById(R.id.attenders);
        ExpandableHeightListView commentsListView = (ExpandableHeightListView) findViewById(R.id.comments_listview);

        commentAdapter = new CommentAdapter(this);
        commentsListView.setAdapter(commentAdapter);
        commentsListView.setExpanded(true);

        commentsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Comment comment = commentAdapter.getItem(position);
                if (comment.getUserId() == myId) {
                    showPopUpComments(view, comment);
                }
                return false;
            }
        });

        final ImageAdapter attendersAdapter = new ImageAdapter(this, myId, tag);
        networkProvider.getAttenders(tag, attendersAdapter);
        attendersGrid.setAdapter(attendersAdapter);
        attendersGrid.setExpanded(true);
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
                    } else {
                        if (attendersAdapter.getItem(position).getId() == myId) {
                            showResignDialog(attendersAdapter);
                        }
                    }
                }
            }
        });

        TextView commentsLabel = (TextView) findViewById(R.id.comments);
        commentsLabel.setOnClickListener(onClickListener);

        networkProvider.getComments(tag, commentAdapter);

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
        navigateTo.setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.to_map: {
                    showMap();
                    break;
                }
                case R.id.more: {
                    showPopUp(view);
                    break;
                }
                case R.id.navigate_to: {
                    navigateTo();
                    break;
                }
                case R.id.comments: {
                    addNewComment();
                    break;
                }
            }
        }
    };

    private void addNewComment() {
        RelativeLayout relative = (RelativeLayout) findViewById(R.id.min_details_rel);
        showAddCommentsDialog(relative);
    }

    private void navigateTo() {
        String uri = "google.navigation:q=" + String.valueOf(tag.getLat() + "," + String.valueOf(tag.getLng()));
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mapsIntent);
    }

    private void showMap() {
        Intent i = new Intent(this, MapActivity.class);
        i.putExtra(LAT, tag.getLat());
        i.putExtra(LNG, tag.getLng());
        i.putExtra(TAG_KEY, tag);
        startActivity(i);
    }

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

    private void deleteComment(Comment comment) throws UnsupportedEncodingException, JSONException {
        networkProvider.deleteComment(tag, comment, commentAdapter);
    }

    private void editComment(Comment comment) throws IOException, JSONException {
        RelativeLayout relative = (RelativeLayout) findViewById(R.id.min_details_rel);
        showEditCommentsDialog(relative, comment);
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

    private void showAddCommentsDialog(final RelativeLayout view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText code = new EditText(this);

        builder.setMessage(R.string.add_comment_msg)
                .setTitle(R.string.add_comment_title)
                .setView(code)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String commentContent = code.getText().toString();
                        try {
                            networkProvider.sendComment(tag, commentContent, commentAdapter);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    private void showEditCommentsDialog(final RelativeLayout view, final Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText code = new EditText(this);
        code.setText(comment.getComment());
        code.setSelection(comment.getComment().length());

        builder.setMessage(R.string.edit_comment_msg)
                .setTitle(R.string.edit_comment_title)
                .setView(code)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String commentContent = code.getText().toString();
                        comment.setComment(commentContent);
                        try {
                            networkProvider.editComment(tag, comment, commentAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    private void showDeleteCommentDialog(final Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_tag_dialog_message)
                .setTitle(R.string.delete_tag_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            deleteComment(comment);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    private void showPopUpComments(View v, final Comment comment) {
        PopupMenu popup = new PopupMenu(this, v);

        popup.getMenuInflater().inflate(R.menu.popup_comments, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_comment: {
                        try {
                            editComment(comment);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case R.id.delete_comment: {
                        showDeleteCommentDialog(comment);
                        break;
                    }
                }
                return true;
            }
        });
        popup.show();
    }

    private void showPopUpHostComments(View v, final Comment comment) {
        PopupMenu popup = new PopupMenu(this, v);

        popup.getMenuInflater().inflate(R.menu.popup_host_comments, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_comment: {
                        showDeleteCommentDialog(comment);
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


}
