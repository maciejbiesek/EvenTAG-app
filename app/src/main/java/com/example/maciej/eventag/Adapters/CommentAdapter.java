package com.example.maciej.eventag.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.maciej.eventag.R;
import com.example.maciej.eventag.models.Comment;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by maciej on 07.01.16.
 */
public class CommentAdapter extends BaseAdapter {

    private List<Comment> commentList = new ArrayList<Comment>();
    private Context context;

    public CommentAdapter(Context context) {
        this.context = context;
    }

    public void setComments(Collection<Comment> comments) {
        commentList.clear();
        commentList.addAll(comments);
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Comment getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View commentView;

        if (convertView == null) {
            commentView = LayoutInflater.from(context).inflate(R.layout.comment_row, parent, false);
        } else {
            commentView = convertView;
        }

        bindCommentToView(getItem(position), commentView);
        loadImage(getItem(position), commentView);

        return commentView;
    }

    private void loadImage(Comment comment, View commentView) {
        final CircularImageView commentPhoto = (CircularImageView) commentView.findViewById(R.id.comment_owner);

        loadImageWithPicasso(comment, commentPhoto);
    }

    private void loadImageWithPicasso(Comment comment, ImageView commentPhoto) {
        Picasso.with(context).load(comment.getUser().getAvatarUrl()).into(commentPhoto);
    }

    private void bindCommentToView(Comment comment, View commentView) {
       // TextView ownerName = (TextView) commentView.findViewById(R.id.comment_owner_name);
       // ownerName.setText(comment.getUser().getName());

        TextView commentContent = (TextView) commentView.findViewById(R.id.comment_content);
        commentContent.setText(comment.getComment());
    }
}
