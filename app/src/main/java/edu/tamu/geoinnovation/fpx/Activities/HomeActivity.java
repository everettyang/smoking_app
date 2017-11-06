package edu.tamu.geoinnovation.fpx.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import edu.tamu.geoinnovation.fpx.R;

public class HomeActivity extends AppCompatActivity {

    private final String ADD_NEW_USER = "Add New User";

    public boolean pebbleOnRight = false;
    public boolean cigInHand = false;
    private ArrayList<String> spinnerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        spinnerList = new ArrayList<>();
        spinnerList.add("");
        spinnerList.add(ADD_NEW_USER);

        // Populate Spinner
        Spinner userSpinner = (Spinner) findViewById(R.id.user_id);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerList);
        userSpinner.setAdapter(adapter);

        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
        {
            if(adapterView.getItemAtPosition(i).toString().equals(ADD_NEW_USER))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(ADD_NEW_USER);
                final EditText input = new EditText(HomeActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addUser(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        }
        public void onNothingSelected(AdapterView<?> adapterView)
        {
            return;
        }

    });
    }

    public void addUser(String id)
    {
        if(spinnerList.get(0).equals(""))
        {
            spinnerList.remove(0);
            spinnerList.remove(0);
        }
        else
        {
            spinnerList.remove(spinnerList.size() - 1);
        }
        spinnerList.add("User ID: " + id);
        spinnerList.add(ADD_NEW_USER);
        Spinner userSpinner = (Spinner) findViewById(R.id.user_id);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerList);
        userSpinner.setAdapter(adapter);
        userSpinner.setSelection(spinnerList.size() - 2);
    }

    public void onRightClicked(View v)
    {
        Button right = (Button) findViewById(R.id.right);
        Button left = (Button) findViewById(R.id.left);
        left.setBackgroundResource(R.drawable.button_bg_unselected);
        right.setBackgroundResource(R.drawable.button_bg_selected);
        pebbleOnRight = true;
    }

    public void onLeftClicked(View v)
    {
        Button right = (Button) findViewById(R.id.right);
        Button left = (Button) findViewById(R.id.left);
        right.setBackgroundResource(R.drawable.button_bg_unselected);
        left.setBackgroundResource(R.drawable.button_bg_selected);
        pebbleOnRight = false;
    }

    public void onNoClicked(View v)
    {
        Button yes = (Button) findViewById(R.id.yes);
        Button no = (Button) findViewById(R.id.no);
        yes.setBackgroundResource(R.drawable.button_bg_unselected);
        no.setBackgroundResource(R.drawable.button_bg_selected);
        cigInHand = false;
    }

    public void onYesClicked(View v)
    {
        Button yes = (Button) findViewById(R.id.yes);
        Button no = (Button) findViewById(R.id.no);
        yes.setBackgroundResource(R.drawable.button_bg_selected);
        no.setBackgroundResource(R.drawable.button_bg_unselected);
        cigInHand = false;
    }

    public void startDataCollection(View v)
    {
        Intent intent = new Intent(HomeActivity.this, CollectingData.class);
        // Start Data Collecting Here <>
        startActivity(intent);
        finish();
    }


}
