package com.example.maciej.eventag;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.squareup.picasso.Picasso;

public class TagAdapter extends BaseAdapter {

    private List<Tag> tagList = new ArrayList<>();
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
            tagView = LayoutInflater.from(context).inflate(R.layout.tag_row, parent, false);
        } else {
            tagView = convertView;
        }

        bindTagToView(getItem(position), tagView);
        loadImage(getItem(position), tagView);

        return tagView;
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

        TextView tagLocalisation = (TextView) tagView.findViewById(R.id.tag_shutdown);
        tagLocalisation.setText(tag.getShutdownTime());
    }
}
