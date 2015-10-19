package com.example.maciej.eventag.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.example.maciej.eventag.R;
import com.example.maciej.eventag.Models.Tag;
import com.squareup.picasso.Picasso;

public class TagAdapter extends BaseAdapter {

    private List<Tag> tagList = new ArrayList<Tag>();
    private Context context;

    public TagAdapter(Context context) {
        this.context = context;
    }

    public void setTags(Collection<Tag> tags) {
        tagList.clear();
        tagList.addAll(tags);
    }

    @Override
    public int getCount() {
        return tagList.size();
    }

    @Override
    public Tag getItem(int position) {
        return tagList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View tagView;

        if (convertView == null) {
            tagView = LayoutInflater.from(context).inflate(getLayoutId(position), parent, false);
        } else {
            tagView = convertView;
        }

        bindTagToView(getItem(position), tagView);
        loadImage(getItem(position), tagView);

        return tagView;
    }

    private int getLayoutId(int position) {
        if (getItemViewType(position) == ViewType.LIVE.ordinal()) {
            return R.layout.tag_row;
        } else {
            return R.layout.tag_row_extinct;
        }
    }

    enum ViewType {
        LIVE, EXTINCT;
    }

    @Override
    public int getItemViewType(int position) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date shutdownDate = null;
        try {
            shutdownDate = df.parse(getItem(position).getShutdownTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date todayDate = new Date();
        return todayDate.before(shutdownDate) ? ViewType.LIVE.ordinal() : ViewType.EXTINCT.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    private void loadImage(Tag tag, View tagView) {
        final ImageView tagPhoto = (ImageView) tagView.findViewById(R.id.tag_photo);
        tagPhoto.setImageBitmap(null);

        loadImageWithPicasso(tag, tagPhoto);
    }

    private void loadImageWithPicasso(Tag tag, ImageView tagPhoto) {
        Picasso.with(context).load(tag.getOwner().getAvatarUrl()).into(tagPhoto);
    }

    private void bindTagToView(Tag tag, View tagView) {
        TextView tagLabel = (TextView) tagView.findViewById(R.id.tag_label);
        tagLabel.setText(tag.getName());

        TextView tagPlace = (TextView) tagView.findViewById(R.id.tag_place);
        tagPlace.setText(tag.getAddress());

        TextView tagShutdown = (TextView) tagView.findViewById(R.id.tag_shutdown);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date shutdownDate = null;

        try {
            shutdownDate = df.parse(tag.getShutdownTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateInfo = getTimeDiff(shutdownDate);

        tagShutdown.setText(dateInfo);
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
            timeDiff += " " + context.getString(R.string.times_left);
        }
        else {
            timeDiff += context.getString(R.string.expired) + " " + df.format(shutdownDate);
        }
        return timeDiff;

    }
}
