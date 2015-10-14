package com.example.maciej.eventag.Helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CommunicationHelper {

    private Context context;

    public CommunicationHelper(Context context) {
        this.context = context;
    }

    public void showUserDialog(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(msg)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }
}
