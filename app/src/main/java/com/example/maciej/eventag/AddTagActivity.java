package com.example.maciej.eventag;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class AddTagActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag);

        final EditText nameEditText = (EditText) findViewById(R.id.new_tag_name);
        final EditText descriptionEditText = (EditText) findViewById(R.id.new_tag_description);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioList);

        Intent i = getIntent();
        final int index = i.getIntExtra("index", 0) + 1;

        User tempUser = new User(100, "Maciej Biesek", "Maciej", "Biesek", "male", "");

        Button addTag = (Button) findViewById(R.id.new_tag_save);
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                String shutdownTime = null;

                int selectedId = radioGroup.getCheckedRadioButtonId();
                final RadioButton checkedRatio = (RadioButton) findViewById(selectedId);
                int which = radioGroup.indexOfChild(checkedRatio);

                switch (which) {
                    case 1:
                        shutdownTime = addTime(15);
                        break;
                    case 2:
                        shutdownTime = addTime(30);
                        break;
                    case 3:
                        shutdownTime = addTime(60);
                        break;
                    case 4:
                        shutdownTime = addTime(120);
                        break;
                }

                if (!name.isEmpty()) {
                    Toast.makeText(AddTagActivity.this, "" + index + ", " + name + ", " + description + ", " +
                            ", " + shutdownTime, Toast.LENGTH_LONG).show();
                    finish();
                }
                else Toast.makeText(AddTagActivity.this, "Najpierw podaj tytuł!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public String addTime(int minutes) {
        Calendar date = Calendar.getInstance();
        Calendar finalDate = date;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        finalDate.add(date.MINUTE, minutes);
        return df.format(finalDate.getTime());
    }
}


// na koniec finish()
