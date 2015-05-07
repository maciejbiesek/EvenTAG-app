package com.example.maciej.eventag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TagAdapter extends BaseAdapter {

    private TagProvider provider;
    private Context context;

    public TagAdapter(Context context) {
        this.context = context;
        this.provider = new TagProvider(context);
    }

    @Override
    public int getCount() {
        return provider.getTagsNumber();
    }

    @Override
    public Tag getItem(int position) {
        return provider.getTag(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View recipeView;

        if (convertView == null) {
            recipeView = LayoutInflater.from(context).inflate(R.layout.tag_row, parent, false);
        } else {
            recipeView = convertView;
        }

        bindRecipeToView(getItem(position), recipeView);

        return recipeView;
    }

    private void bindRecipeToView(Tag tag, View tagView) {
        ImageView tagPhoto = (ImageView) tagView.findViewById(R.id.tag_photo);
        tagPhoto.setImageResource(tag.getPhotoId());

        TextView tagLabel = (TextView) tagView.findViewById(R.id.tag_label);
        tagLabel.setText(tag.getNameId());

        TextView tagDate = (TextView) tagView.findViewById(R.id.tag_date);
        tagDate.setText(tag.getDateId());
    }
}
