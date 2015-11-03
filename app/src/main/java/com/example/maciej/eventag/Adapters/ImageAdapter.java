package com.example.maciej.eventag.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<User> users = new ArrayList<User>();
    private int myId;

    public ImageAdapter(Context context, int myId) {
        this.context = context;
        this.myId = myId;
    }

    public void addUsers(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
    }

    @Override
    public int getCount() {
        return users.size() + 1;
    }

    public int getLast() { return users.size(); }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View imageView;

        if (convertView == null) {
            imageView = LayoutInflater.from(context).inflate(R.layout.tag_attender, parent, false);
        } else {
            imageView = convertView;
        }

        bindImageToView(position, imageView);

        return imageView;
    }


    private void bindImageToView(int position, View imageView) {
        ImageView image = (ImageView) imageView.findViewById(R.id.attender);

        if (position == users.size()) {
            image.setImageResource(R.drawable.join);
        }
        else {
            User user = getItem(position);
            Picasso.with(context).load(user.getAvatarUrl()).into(image);
        }
    }
}