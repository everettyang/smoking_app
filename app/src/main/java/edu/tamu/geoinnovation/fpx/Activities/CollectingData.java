package edu.tamu.geoinnovation.fpx.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.tamu.geoinnovation.fpx.R;

public class CollectingData extends AppCompatActivity {

    public boolean cigInHand = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collecting_data);
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

    public void stopDataCollection(View v)
    {
        Intent intent = new Intent(CollectingData.this, HomeActivity.class);
        // Start Data Collecting Here <>
        startActivity(intent);
        finish();
    }
}
