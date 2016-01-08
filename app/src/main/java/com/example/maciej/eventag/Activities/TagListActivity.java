package com.example.maciej.eventag.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.maciej.eventag.R;
import com.example.maciej.eventag.models.Tag;
import com.example.maciej.eventag.Adapters.TagAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.maciej.eventag.models.Constants.*;


public class TagListActivity extends ActionBarActivity {
    private TagAdapter adapter;
    private List<Tag> tagList;
    private boolean isSorted = false;
    private ViewAnimator viewAnimator;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list);
        showActionBar();

        Intent i = getIntent();
        tagList = (ArrayList<Tag>) i.getSerializableExtra(TAGS_LIST);
        latitude = i.getStringExtra(LAT);
        longitude = i.getStringExtra(LNG);

        sortTagList();

        viewAnimator = (ViewAnimator) findViewById(R.id.animator2);
        
        if (isSorted) {
            viewAnimator.setDisplayedChild(1);
            initializeList();
        }
    }

    private void sortTagList() {
        viewAnimator = (ViewAnimator) findViewById(R.id.animator2);
        isSorted = false;
        viewAnimator.setDisplayedChild(0);
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date todayDate = new Date();
        Collections.sort(tagList, new Comparator<Tag>() {
            public int compare(Tag t1, Tag t2) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = df.parse(t1.getShutdownTime());
                    date2 = df.parse(t2.getShutdownTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date2.compareTo(date1);
            }
        });
        ArrayList tmpList1 = new ArrayList<Tag>();
        ArrayList tmpList2 = new ArrayList<Tag>();
        for (Tag tag : tagList) {
            Date tagDate = null;
            try {
                tagDate = df.parse(tag.getShutdownTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (todayDate.before(tagDate)) {
                tmpList1.add(tag);
            }
            else {
                tmpList2.add(tag);
            }
        }
        Collections.sort(tmpList1, new Comparator<Tag>() {
            public int compare(Tag t1, Tag t2) {
                return t1.getDistance().compareTo(t2.getDistance());
            }
        });
        ArrayList tmpList3 = new ArrayList<Tag>();
        tmpList3.addAll(tmpList1);
        tmpList3.addAll(tmpList2);
        tagList.clear();
        tagList.addAll(tmpList3);
        isSorted = true;
    }

    private void initializeList() {
        ListView list = (ListView) findViewById(R.id.tag_list);
        adapter = new TagAdapter(this);

        adapter.setTags(tagList);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tag tag = adapter.getItem(position);
                showTag(tag);
            }
        });
    }

    private void showTag(Tag tag) {
        Intent i = new Intent(this, TagDetailsActivity.class);

        i.putExtra(TAG_KEY, tag);
        startActivityForResult(i, TAG_DETAILS_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
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
        View cView = getLayoutInflater().inflate(R.layout.custom_tag_list_menu, null);
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
            case R.id.add_new: {
                Intent intent = new Intent(TagListActivity.this, AddTagActivity.class);
                intent.putExtra(LAT, latitude);
                intent.putExtra(LNG, longitude);
                startActivityForResult(intent, TAG_RESULT);
                overridePendingTransition(R.anim.slide_bottom_out, R.anim.slide_bottom_in);
                finish();
                break;
            }
        }
    }

}
